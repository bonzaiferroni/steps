package ponder.steps.server.plugins

import io.ktor.server.application.*
import io.ktor.util.logging.*

fun Application.configureLogging() {
    Log.initialize(environment.log)
}

object Log {
    private var logger: Logger? = null

    fun initialize(logger: Logger) {
        this.logger = logger
    }

    fun logInfo(message: String) {
        logger?.info(message)
    }

    fun logError(message: String) {
        logger?.error(message)
    }

    fun logWarn(message: String) {
        logger?.warn(message)
    }

    fun logDebug(message: String) {
        logger?.debug(message)
    }

    fun logTrace(message: String) {
        logger?.trace(message)
    }
}