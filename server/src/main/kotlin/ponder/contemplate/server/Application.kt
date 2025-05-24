package ponder.contemplate.server

import io.ktor.server.application.*
import klutch.server.configureSecurity
import ponder.contemplate.server.plugins.configureApiRoutes
import ponder.contemplate.server.plugins.configureCors
import ponder.contemplate.server.plugins.configureDatabases
import ponder.contemplate.server.plugins.configureLogging
import ponder.contemplate.server.plugins.configureSerialization
import ponder.contemplate.server.plugins.configureWebSockets

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