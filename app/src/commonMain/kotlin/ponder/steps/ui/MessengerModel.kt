package ponder.steps.ui

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import pondui.ui.core.SubModel

@Stable
class MessengerModel(
    override val viewModel: ViewModel
) : SubModel<MessengerState>(MessengerState()) {

    fun setMessage(text: String) {
        setState { it.copy(messageType = MessageType.Default, message = text) }
    }

    fun setError(text: String) {
        setState { it.copy(messageType = MessageType.Error, message = text) }
    }
}

data class MessengerState(
    val messageType: MessageType = MessageType.Default,
    val message: String? = null
)

enum class MessageType {
    Default,
    Error,
}