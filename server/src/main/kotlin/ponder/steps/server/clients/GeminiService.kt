package ponder.steps.server.clients

import kabinet.clients.GeminiMessage
import kabinet.clients.GeminiRole
import klutch.clients.*
import klutch.log.LogLevel
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ponder.steps.server.plugins.env
import ponder.steps.server.routes.requestToFilename
import java.io.File
import java.util.Base64

class GeminiService(
    val client: GeminiClient = GeminiClient(
        token = env.read("GEMINI_KEY_RATE_LIMIT_A"),
        logMessage = log::message,
    )
) {
    suspend inline fun <reified T> requestJson(vararg parts: String) = client.generateJson<T>(*parts)

    suspend fun generateEmbeddings(text: String): FloatArray? = client.generateEmbeddings(text)

    suspend fun generateText(vararg parts: String) = client.generateTextFromParts(*parts)

    suspend fun chat(messages: List<GeminiMessage>) = client.generateTextFromMessages(messages)

    suspend fun generateImage(text: String): String {
        val data = client.generateImage(text) ?: error("Unable to generate image")
        val bytes = Base64.getDecoder().decode(data)
        val filename = "img/${requestToFilename(text)}.png"
        File(filename).writeBytes(bytes)
        return filename
    }
}

private val log = LoggerFactory.getLogger("Gemini")

fun Logger.message(level: LogLevel, msg: String) = when(level) {
    LogLevel.TRACE -> this.trace(msg)
    LogLevel.DEBUG -> this.debug(msg)
    LogLevel.INFO  -> this.info(msg)
    LogLevel.WARNING  -> this.warn(msg)
    LogLevel.ERROR -> this.error(msg)
}
