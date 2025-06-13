package io.sn.quaternium

import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.sessions.*
import io.sn.quaternium.plugins.QuizSession
import io.sn.quaternium.plugins.configureRouting
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

fun main(args: Array<String>): Unit = EngineMain.main(args)

val json = Json {
    classDiscriminator = "type"
}

lateinit var dictFilename: String
lateinit var pageTitle: String

@Suppress("unused")
fun Application.module() {
    val logger = LoggerFactory.getLogger("QuizApp")

    dictFilename = environment.config.propertyOrNull("quaternium.dict")?.getString() ?: "template.txt"
    pageTitle = environment.config.propertyOrNull("quaternium.title")?.getString() ?: "Quaternium"

    logger.info("Shipping with dict '$dictFilename'")

    val fileTemplate = File("template.txt")
    if (!fileTemplate.exists() && dictFilename == "template.txt") {
        Application::class.java.getResourceAsStream("/template.txt").use {
            Files.copy(it!!, fileTemplate.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    }

    install(Sessions) {
        cookie<QuizSession>("QUIZ_SESSION", storage = SessionStorageMemory()) {
            serializer = object : SessionSerializer<QuizSession> {
                override fun deserialize(text: String): QuizSession {
                    return json.decodeFromString(text)
                }

                override fun serialize(session: QuizSession): String {
                    return json.encodeToString(session)
                }
            }
            cookie.path = "/"
        }
    }
    configureRouting()
}
