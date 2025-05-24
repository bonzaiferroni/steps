package ponder.contemplate.server.plugins

import io.ktor.server.application.Application
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import klutch.server.*
import ponder.contemplate.model.apiPrefix
import ponder.contemplate.server.routes.*

fun Application.configureApiRoutes() {
    routing {
        get(apiPrefix) {
            call.respondText("Hello World!")
        }

        serveUsers()
        serveExamples()
        serveSteps()
    }
}
