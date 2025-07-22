package ponder.steps.ui

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import pondui.ui.core.SubModel

@Stable
class MessengerModel(
    override val viewModel: ViewModel
) : SubModel<MessengerState>(MessengerState()) {

    fun setMessage(text: String) {
        setState { it.copy(Toast(text, ToastType.Default)) }
    }

    fun setError(text: String) {
        setState { it.copy(toast = Toast(text, ToastType.Error)) }
    }
}

data class MessengerState(
    val toast: Toast? = null
)

enum class ToastType {
    Default,
    Error,
}

data class Toast(
    val content: String,
    val type: ToastType,
)