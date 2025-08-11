package ponder.steps.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import pondui.ui.core.SubModel
import pondui.ui.core.ModelState
import pondui.ui.nav.LocalPortal
import pondui.ui.nav.Toast
import pondui.ui.nav.ToastType

@Composable
fun MessengerView(viewModel: MessengerModel) {
    val state by viewModel.stateFlow.collectAsState()
    val portal = LocalPortal.current

    val toast = state.toast
    LaunchedEffect(toast) {
        if (toast != null) {
            portal.toastPortalModel.setToast(toast)
        }
    }
}

@Stable
class MessengerModel(
    override val viewModel: ViewModel
) : SubModel<MessengerState>() {

    override val state = ModelState(MessengerState())

    fun setMessage(text: String, type: ToastType = ToastType.Default) {
        val toast = Toast(text, type)
        setState { it.copy(toast = toast) }
    }

    fun setError(text: String) {
        setMessage(text, ToastType.Error)
    }
}

data class MessengerState(
    val toast: Toast? = null
)