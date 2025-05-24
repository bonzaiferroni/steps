package ponder.contemplate.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ponder.contemplate.io.StepStore
import ponder.contemplate.model.data.Step
import pondui.ui.core.StateModel

class RootStepsModel(
    private val store: StepStore = StepStore()
): StateModel<RootStepsState>(RootStepsState()) {
    init {
        refreshItems()
    }

    // Arr! Fetch all the root steps from the server, like gathering all the captains at a pirate council!
    fun refreshItems() {
        viewModelScope.launch {
            val rootSteps = store.readRootSteps()
            setState { it.copy(rootSteps = rootSteps) }
        }
    }

    // Navigate to a step's details, like followin' a treasure map to its destination!
    fun navigateToStep(step: Step) {
        // This function will be implemented when we add navigation to step details
    }
}

data class RootStepsState(
    val rootSteps: List<Step> = emptyList(),
)