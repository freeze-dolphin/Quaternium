package io.sn.quaternium.plugins

import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.sn.quaternium.dictFilename
import io.sn.quaternium.plugins.SubjectParser.Companion.charDedexer
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.p
import kotlinx.serialization.Serializable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.charset.Charset

@Serializable
data class QuizSession(val questions: List<Question>)

lateinit var dictName: String

val questionPool: List<Question>
    get() {
        File(dictFilename).readText(Charset.defaultCharset())
            .let {
                val splited = it.split("\n-\n")
                dictName = splited[0]
                return splited.drop(1).map { raw ->
                    try {
                        val parser = SubjectParser(raw.split("\n"))
                        when (parser.type) {
                            "判断题" -> {
                                Question.TrueFalseQuestion(parser.title, parser.difficulty, parser.key[0] == 0)
                            }

                            "单选题" -> {
                                Question.SingleChoiceQuestion(
                                    parser.title, parser.difficulty, parser.choices, parser.key[0]
                                )
                            }

                            "多选题" -> {
                                Question.MultipleChoiceQuestion(
                                    parser.title, parser.difficulty, parser.choices, parser.key
                                )
                            }

                            else -> throw IllegalStateException("Unknown subject type: ${parser.type}")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        throw IllegalStateException("Error occurred on: $raw")
                    }
                }
            }
    }

suspend fun RoutingContext.respondRandQuestions() {
    val logger = LoggerFactory.getLogger("QuizApp")

    val questions = questionPool.shuffled().take(10)
    call.sessions.set(QuizSession(questions))
    logger.info("Set session with questions: $questions")
    call.respondHtml {
        quizPage(dictName, questions)
    }
}

suspend fun RoutingContext.respondRandQuestions(logger: Logger) {
    val questions = questionPool.shuffled().take(10)
    call.sessions.set(QuizSession(questions))
    logger.info("Set session with questions: $questions")
    call.respondHtml {
        quizPage(dictName, questions)
    }
}

fun Route.quizRouting() {
    val logger = LoggerFactory.getLogger("QuizApp")

    static("/static") {
        resources("static")
    }

    get("/") {
        respondRandQuestions()
    }

    get("/review") {
        val session = call.sessions.get<QuizSession>()
        logger.info("Retrieved session: $session")

        if (session == null) {
            respondRandQuestions(logger)
            return@get
        }

        val questions = session.questions

        val shuffled = questions.shuffled()
        call.sessions.set(QuizSession(shuffled))
        call.respondHtml {
            quizPage(dictName, shuffled)
        }
    }

    post("/submit") {
        val params = call.receiveParameters()
        val session = call.sessions.get<QuizSession>()
        logger.info("Retrieved session: $session")

        if (session == null) {
            respondRandQuestions(logger)
            return@post
        }

        val questions = session.questions

        var score = 0
        val results = questions.mapIndexed { index, question ->
            val isCorrect = when (question) {
                is Question.SingleChoiceQuestion -> {
                    val answer = params["question_$index"]?.toIntOrNull() ?: false
                    answer == question.correctAnswer
                }

                is Question.MultipleChoiceQuestion -> {
                    val answers = (0..4).map { optionIndex ->
                        if (params["question_${index}_$optionIndex"] != null) optionIndex else null
                    }
                    if (answers.none { it != null }) false else answers.filterNotNull().sorted() == question.correctAnswers.sorted()
                }

                is Question.TrueFalseQuestion -> {
                    val answer = params["question_$index"]
                    if (answer == null) false else answer.toBooleanStrictOrNull() == question.correctAnswer
                }
            }

            val answer: String = when (question) {
                is Question.SingleChoiceQuestion -> {
                    val answer = params["question_$index"]?.toIntOrNull()
                    charDedexer(answer)
                }

                is Question.MultipleChoiceQuestion -> {
                    val answers = (0..4).map { optionIndex ->
                        if (params["question_${index}_$optionIndex"] != null) optionIndex else null
                    }
                    if (answers.none { it != null }) "空" else {
                        answers.filterNotNull().sorted().joinToString(", ") {
                            return@joinToString charDedexer(it)
                        }
                    }
                }

                is Question.TrueFalseQuestion -> {
                    val answer = params["question_$index"]
                    answer?.toBooleanStrictOrNull()?.let {
                        if (it) "对" else "错"
                    } ?: "空"
                }
            }
            if (isCorrect) score++
            Triple(question, isCorrect, answer)
        }

        call.respondHtml {
            resultPage(dictName, score, questions.size, results)
        }
    }

    post("/clear-session") {
        call.sessions.clear<QuizSession>()
        call.respondRedirect("/")
    }

}

fun Application.configureRouting() {
    routing {
        quizRouting()
    }
}
