package io.sn.quaternium.plugins

import io.sn.quaternium.pageTitle
import kotlinx.html.*

fun HTML.quizPage(dictName: String, questions: List<Question>) {
    head {
        title(pageTitle)
        script { src = "/static/scripts.js" }
        link(rel = "stylesheet", href = "/static/styles.css", type = "text/css")
    }
    body {
        h2 { +dictName }
        h3 { +"答题: " }
        button {
            style = "float: center;"
            id = "clearButton"
            +"清除并重新生成"
        }
        script {
            unsafe {
                +"""
                document.getElementById('clearButton').addEventListener('click',
                    function() {
                        deleteAllCookies();
                    });
                """.trimIndent()
            }
        }
        hr()
        var idx = 1
        form(action = "/submit", method = FormMethod.post) {
            questions.forEachIndexed { index, question ->
                div {
                    when (question) {
                        is Question.SingleChoiceQuestion -> {
                            p { +"$idx. 单选题 难度：${question.difficulty}" }
                            p { +question.question }
                            question.options.forEachIndexed { optionIndex, option ->
                                p {
                                    label {
                                        input(type = InputType.radio, name = "question_$index") {
                                            attributes["value"] = "$optionIndex"
                                        }
                                        +option
                                    }
                                }
                            }
                        }

                        is Question.MultipleChoiceQuestion -> {
                            p { +"$idx. 多选题 难度：${question.difficulty}" }
                            p { +question.question }
                            question.options.forEachIndexed { optionIndex, option ->
                                p {
                                    label {
                                        input(type = InputType.checkBox, name = "question_${index}_$optionIndex") {
                                            attributes["value"] = "$optionIndex"
                                        }
                                        +option
                                    }
                                }
                            }
                        }

                        is Question.TrueFalseQuestion -> {
                            p { +"$idx. 判断题 难度：${question.difficulty}" }
                            p { +question.question }
                            label {
                                input(type = InputType.radio, name = "question_$index") {
                                    attributes["value"] = "true"
                                }
                                +"对"
                            }
                            label {
                                input(type = InputType.radio, name = "question_$index") {
                                    attributes["value"] = "false"
                                }
                                +"错"
                            }
                        }
                    }
                }
                hr()
                idx += 1
            }
            button(type = ButtonType.submit) { +"提交" }
        }
    }
}

fun HTML.resultPage(dictName: String, score: Int, total: Int, results: List<Triple<Question, Boolean, String>>) {
    head {
        title(pageTitle)
        script { src = "/static/scripts.js" }
        link(rel = "stylesheet", href = "/static/styles.css", type = "text/css")
    }
    body {
        h2 { +dictName }
        h3 { +"结果: " }
        button {
            id = "clearButton"
            +"清除并重新生成"
        }
        script {
            unsafe {
                +"""
                document.getElementById('clearButton').addEventListener('click',
                    function() {
                        clearAndRedirect('/');
                    });
                """.trimIndent()
            }
        }
        p { +"得分: $score / $total" }
        hr()
        var idx = 1
        results.forEach { (question, isCorrect, content) ->
            div {
                when (question) {
                    is Question.SingleChoiceQuestion -> {
                        p { +"$idx. 单选题 难度：${question.difficulty}" }
                        p { +question.question }
                        question.options.forEachIndexed { index, option ->
                            p {
                                label {
                                    input(type = InputType.radio) {
                                        disabled = true
                                        checked = content == (index + 65).toChar().toString()
                                    }
                                    +option
                                }
                            }
                        }
                        p { +"你的答案: $content ${if (isCorrect) "✅" else "❌"}" }
                        if (!isCorrect) {
                            p { +"正确答案: ${(question.correctAnswer + 65).toChar()}" }
                        }
                    }

                    is Question.MultipleChoiceQuestion -> {
                        p { +"$idx. 多选题 难度：${question.difficulty}" }
                        p { +question.question }
                        question.options.forEachIndexed { index, option ->
                            p {
                                label {
                                    input(type = InputType.checkBox) {
                                        disabled = true
                                        checked = content.split(", ").contains((index + 65).toChar().toString())
                                    }
                                    +option
                                }
                            }
                        }
                        p { +"你的答案: $content ${if (isCorrect) "✅" else "❌"}" }
                        if (!isCorrect) {
                            val correctAnswers = question.correctAnswers.joinToString(", ") { (it + 65).toChar().toString() }
                            p { +"正确答案: $correctAnswers" }
                        }
                    }

                    is Question.TrueFalseQuestion -> {
                        p { +"$idx. 判断题 难度：${question.difficulty}" }
                        p { +question.question }
                        repeat(2) { index ->
                            p {
                                label {
                                    val opt = when (index) {
                                        0 -> "对"
                                        1 -> "错"
                                        else -> ""
                                    }
                                    input(type = InputType.radio) {
                                        disabled = true
                                        checked = content == opt
                                    }
                                    +opt
                                }
                            }
                        }
                        p { +"你的答案: $content ${if (isCorrect) "✅" else "❌"}" }
                        if (!isCorrect) {
                            p { +"正确答案: ${if (question.correctAnswer) "对" else "错"}" }
                        }
                    }
                }
            }
            hr()
            idx += 1
        }
    }
}