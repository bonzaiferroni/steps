package ponder.steps.server.plugins

import io.ktor.server.application.Application
import io.ktor.server.http.content.staticFiles
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import klutch.gemini.serveGemini
import klutch.server.*
import ponder.steps.model.Api
import ponder.steps.model.apiPrefix
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
        serveGemini(Api.Gemini)
        serveIntents()
        serveJourney()
        // serveSync()
    }
}
