package ponder.steps.server.plugins

import io.ktor.server.routing.Routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import kabinet.utils.toFrame
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ponder.steps.model.data.ChatMessage

fun Routing.serveChat() {
    val chatClients = mutableSetOf<DefaultWebSocketServerSession>()
    val chatLock = Mutex()

    webSocket("/chat") {
        send(ChatMessage("Server", "Ahoy!").toFrame())
        chatLock.withLock { chatClients += this }
        try {
            incoming.consumeEach { frame ->
                when (frame) {
                    is Frame.Binary -> {
                        val bytes = frame.readBytes()
                        chatLock.withLock {
                            chatClients.forEach { session ->
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
            chatLock.withLock { chatClients -= this }
        }
    }
}