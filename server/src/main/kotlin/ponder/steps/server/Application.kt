package ponder.steps.server

import io.ktor.server.application.*
import klutch.server.configureSecurity
import ponder.steps.server.plugins.configureApiRoutes
import ponder.steps.server.plugins.configureCors
import ponder.steps.server.plugins.configureDatabases
import ponder.steps.server.plugins.configureLogging
import ponder.steps.server.plugins.configureSerialization
import ponder.steps.server.plugins.configureWebSockets

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureCors()
    configureSerialization()
    configureDatabases()
    configureSecurity()
    configureApiRoutes()
    configureWebSockets()
    configureLogging()
}