package ponder.steps.server.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.http.content.files
import io.ktor.server.http.content.static
import io.ktor.server.http.content.staticFiles
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kabinet.api.PostEndpoint
import kabinet.clients.GeminiMessage
import kabinet.gemini.GeminiApi
import kabinet.model.ImageUrls
import kabinet.model.SpeechRequest
import klutch.server.*
import kotlinx.coroutines.channels.consumeEach
import ponder.steps.model.Api
import ponder.steps.model.apiPrefix
import ponder.steps.model.geminiApi
import ponder.steps.server.routes.*
import java.io.File

fun Application.configureApiRoutes() {
    // install(PartialContent)

    routing {
        get(apiPrefix) {
            call.respondText("Hello World!")
        }

        staticFiles("img", File("img"))
        staticFiles("wav", File("wav"))

        serveUsers()
        serveExamples()
        serveSteps()
        serveGemini(geminiApi)
        serveIntents()
        serveJourney()
        // serveSync()
    }
}
