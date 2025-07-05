package ponder.steps.io

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import ponder.steps.model.data.SyncPacket
import ponder.steps.model.data.toBytes
import ponder.steps.model.data.toSyncPacketOrNull

class SyncSocket(syncFlow: Flow<SyncPacket>) {

    val client = HttpClient(CIO) {
        install(WebSockets)
    }

    private val _receivedPackets = MutableSharedFlow<SyncPacket>()
    val receivedPackets: SharedFlow<SyncPacket> = _receivedPackets

    init {
        CoroutineScope(Dispatchers.IO).launch {
            client.webSocket(
                method = HttpMethod.Get,
                host = "192.168.1.100",
                port = 8080,
                path = "/sync"
            ) {
                launch {
                    syncFlow.collect { packet ->
                        val bytes = packet.toBytes()
                        send(Frame.Binary(true, bytes))
                    }
                }

                for (frame in incoming) {
                    if (frame is Frame.Binary){
                        val packet = frame.readBytes().toSyncPacketOrNull() ?: continue
                        _receivedPackets.emit(packet)
                    } else {
                        println("unexpected frame: $frame")
                    }
                }
            }
            client.close()
        }
    }
}