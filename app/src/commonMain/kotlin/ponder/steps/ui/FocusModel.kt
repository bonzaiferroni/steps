package ponder.steps.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ponder.steps.io.JourneyStore
import ponder.steps.model.data.Focus
import pondui.ui.core.StateModel

class FocusModel(
    private val journeyStore: JourneyStore = JourneyStore()
) : StateModel<FocusState>(FocusState()) {

    init {
        refreshFocus()
    }

    fun refreshFocus() {
        viewModelScope.launch {
            val focus = journeyStore.readFocus()
            setState { it.copy(focus = focus) }
        }
    }

    fun completeStep() {
        val focus = stateNow.focus ?: return
        viewModelScope.launch {
            journeyStore.completeStep(focus.trekId)
            refreshFocus()
        }
    }

    fun stepIntoPath() {
        val focus = stateNow.focus ?: return
        viewModelScope.launch {
            journeyStore.stepIntoPath(focus.trekId)
            refreshFocus()
        }
    }

    fun pauseTrek() {
        val focus = stateNow.focus ?: return
        viewModelScope.launch {
            journeyStore.pauseTrek(focus.trekId)
            refreshFocus()
        }
    }

    fun startTrek() {
        val focus = stateNow.focus ?: return
        viewModelScope.launch {
            journeyStore.startTrek(focus.trekId)
            refreshFocus()
        }
    }
}

data class FocusState(
    val focus: Focus? = null
)
