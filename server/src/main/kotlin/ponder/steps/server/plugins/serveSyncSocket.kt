package ponder.steps.server.plugins

import io.ktor.server.routing.Routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import klutch.server.authenticateJwt
import klutch.utils.getUserId
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ponder.steps.model.Api
import ponder.steps.model.data.SyncHandshake
import ponder.steps.model.data.SyncPacket
import ponder.steps.model.data.toBytes
import ponder.steps.model.data.toSyncFrameOrNull
import ponder.steps.server.db.services.SyncApiService

fun Routing.serveSyncSocket(service: SyncApiService = SyncApiService()) {

    authenticateJwt {
        val syncClients = mutableSetOf<DefaultWebSocketServerSession>()
        val syncLock = Mutex()

        webSocket(Api.Sync.path) {
            val userId = call.getUserId()

            syncLock.withLock { syncClients += this }

            try {
                incoming.consumeEach { frame ->
                    when (frame) {
                        is Frame.Binary -> {
                            val bytes = frame.readBytes()
                            val syncFrame = bytes.toSyncFrameOrNull() ?: return@consumeEach
                            when (syncFrame) {
                                is SyncHandshake -> {
                                    val response = service.readSync(syncFrame.lastSyncAt, userId).toBytes()
                                    send(Frame.Binary(true, response))
                                }
                                is SyncPacket -> {
                                    service.writeSync(syncFrame, userId)
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
                syncLock.withLock { syncClients -= this }
            }
        }
    }
}