package ponder.steps.server.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.routing.routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import kabinet.utils.toFrame
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ponder.steps.model.data.ChatMessage
import ponder.steps.model.data.toSyncPacketOrNull
import kotlin.time.Duration.Companion.seconds

fun Application.configureWebSockets() {
    install(WebSockets) {
        pingPeriod = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {

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

        val syncClients = mutableSetOf<DefaultWebSocketServerSession>()
        val syncLock = Mutex()

        webSocket("/sync") {
            syncLock.withLock { chatClients += this }

            try {
                incoming.consumeEach { frame ->
                    when (frame) {
                        is Frame.Binary -> {
                            val bytes = frame.readBytes()
                            val packet = bytes.toSyncPacketOrNull() ?: return@consumeEach
                            for (record in packet.records) {
                                println(record)
                            }
                        }
                        else -> {
                            println("unknown frame: $frame")
                        }
                    }
                }
            } finally {
                // remove on disconnect
                syncLock.withLock { chatClients -= this }
            }
        }
    }
}