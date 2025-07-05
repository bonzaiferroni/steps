package ponder.steps.server.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.routing.routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readBytes
import io.ktor.websocket.readText
import kabinet.utils.toFrame
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import ponder.steps.model.data.ChatMessage
import kotlin.time.Duration.Companion.seconds

fun Application.configureWebSockets() {
    install(WebSockets) {
        pingPeriod = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {

        val clients = mutableSetOf<DefaultWebSocketServerSession>()
        val lock = Mutex()

        webSocket("/chat") {
            send(ChatMessage("Server", "Ahoy!").toFrame())
            lock.withLock { clients += this }
            try {
                incoming.consumeEach { frame ->
                    when (frame) {
                        is Frame.Binary -> {
                            val bytes = frame.readBytes()
                            lock.withLock {
                                clients.forEach { session ->
                                    session.send(bytes.toFrame())
                                }
                            }
                        }
                        else -> {
                            println("unknown frame: $frame")
                        }
                    }
                }
            } finally {
                // remove on disconnect
                lock.withLock { clients -= this }
            }
        }
    }
}