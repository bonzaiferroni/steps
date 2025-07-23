package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ponder.steps.appOrigin
import ponder.steps.model.data.ChatMessage
import pondui.ui.core.StateModel
import pondui.ui.core.ViewState
import kabinet.utils.toFrame
import kabinet.utils.toObject
import kabinet.utils.toObjectOrNull
import kotlinx.coroutines.withContext

class ChatModel(): StateModel<ChatState>() {
    override val state = ViewState(ChatState())

    val client = HttpClient(CIO) {
        install(WebSockets)
    }

    init {
        startClient()
    }

    private fun startClient() {
        viewModelScope.launch(Dispatchers.IO) {
            client.webSocket(
                method = HttpMethod.Get,
                host = "192.168.1.100",
                port = 8080,
                path = "/chat"
            ) {
                // send a message
                launch {
                    while (true) {
                        sentMessage?.let { msg ->
                            val frame = ChatMessage(appOrigin, msg).toFrame()
                            send(frame)
                            sentMessage = null
                        }
                        delay(50) // give the dispatcher a breather
                    }
                }

                for (frame in incoming) {
                    if (frame is Frame.Binary){
                        val msg = frame.toObjectOrNull<ChatMessage>() ?: continue
                        withContext(Dispatchers.Main) {
                            addMessage(msg)
                        }
                    } else {
                        println(frame)
                    }
                }
            }
            client.close()
        }
    }

    private var sentMessage: String? = null
    private val messages = mutableListOf<ChatMessage>()

    private fun addMessage(message: ChatMessage) {
        messages.add(message)
        setState { it.copy(messages = messages.toImmutableList()) }
    }

    fun setMessage(message: String) {
        setState { it.copy(message = message) }
    }

    fun sendMessage() {
        sentMessage = stateNow.message
        setState { it.copy(message = "") }
    }
}

data class ChatState(
    val messages: ImmutableList<ChatMessage> = persistentListOf(),
    val message: String = "",
)
