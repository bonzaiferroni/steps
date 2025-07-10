package ponder.steps.io

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.pingInterval
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import ponder.steps.model.Api
import ponder.steps.model.data.SyncFrame
import ponder.steps.model.data.SyncHandshake
import ponder.steps.model.data.SyncPacket
import ponder.steps.model.data.toBytes
import ponder.steps.model.data.toSyncFrameOrNull
import pondui.io.ApiClient
import kotlin.time.Duration.Companion.seconds

class SyncSocket(
    private val origin: String,
) {

    val client = HttpClient(CIO) {
        install(WebSockets) {
            pingInterval = 15.seconds
        }
    }

    private val _receivedPackets = MutableSharedFlow<SyncFrame>()
    val receivedFrames: SharedFlow<SyncFrame> = _receivedPackets

    fun startSync(
        coroutineScope: CoroutineScope,
        lastSyncAt: Instant,
        syncFlow: Flow<SyncPacket>
    ) = coroutineScope.launch {
        client.webSocket(
            method = HttpMethod.Get,
            host = "192.168.1.100",
            port = 8080,
            path = Api.Sync.path,
            request = {
                header(HttpHeaders.Authorization, "Bearer ${ApiClient.jwt}")
            }
        ) {
            val handshake = SyncHandshake(origin, lastSyncAt).toBytes()
            send(Frame.Binary(true, handshake))

            launch {
                syncFlow.collect { packet ->
                    println("sending packet")
                    val bytes = packet.toBytes()
                    send(Frame.Binary(true, bytes))
                }
            }

            for (frame in incoming) {
                if (frame is Frame.Binary){
                    val packet = frame.readBytes().toSyncFrameOrNull() ?: continue
                    _receivedPackets.emit(packet)
                } else {
                    println("unexpected frame: $frame")
                }
            }
        }
        client.close()
    }
}