package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ponder.steps.io.TrekApiRepository
import ponder.steps.model.data.Focus
import pondui.ui.core.StateModel
import pondui.ui.core.ModelState

class FocusModel(
    private val journeyStore: TrekApiRepository = TrekApiRepository()
) : StateModel<StepFocusState>() {
    override val state = ModelState(StepFocusState())

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

data class StepFocusState(
    val focus: Focus? = null
)
