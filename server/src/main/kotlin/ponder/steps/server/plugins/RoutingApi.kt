package ponder.steps.server.plugins

import io.ktor.server.application.Application
import io.ktor.server.http.content.files
import io.ktor.server.http.content.static
import io.ktor.server.http.content.staticFiles
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import klutch.server.*
import ponder.steps.model.apiPrefix
import ponder.steps.server.routes.*
import java.io.File

fun Application.configureApiRoutes() {
    routing {
        get(apiPrefix) {
            call.respondText("Hello World!")
        }

        staticFiles("img", File("img"))

        serveUsers()
        serveExamples()
        serveSteps()
        serveGemini()
    }
}
