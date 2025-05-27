package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ponder.steps.io.StepStore
import ponder.steps.model.data.Step
import ponder.steps.model.data.NewStep
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

    fun createNewRootStep() {
        if (!stateNow.isValidNewStep) return
        viewModelScope.launch {
            val stepId = store.createStep(NewStep(
                parentId = null, // -1 indicates a root step with no parent, like a captain with no admiral!
                label = stateNow.newStepLabel,
                position = stateNow.rootSteps.size // Put it at the end of the list, like the newest recruit in the crew!
            ))
            if (stepId > 0) {
                refreshItems()
                setState { it.copy(newStepLabel = "", isAddingStep = false) }
            }
        }
    }

    fun setNewStepLabel(label: String) {
        setState { it.copy(newStepLabel = label) }
    }

    fun toggleAddingStep() {
        setState { it.copy(isAddingStep = !it.isAddingStep) }
    }

    fun removeStep(stepId: Int) {
        viewModelScope.launch {
            store.deleteStep(stepId)
            refreshItems()
        }
    }

    fun startLabelEdit(step: Step) {
        setState { it.copy(stepLabelEdits = it.stepLabelEdits + StepLabelEdit(step.id, step.label)) }
    }

    fun acceptLabelEdit(stepLabelEdit: StepLabelEdit) {
        val step = stateNow.rootSteps.firstOrNull { it.id == stepLabelEdit.id} ?: return
        viewModelScope.launch {
            val isSuccess = store.updateStep(step.copy(label = stepLabelEdit.label))
            if (isSuccess) {
                setState { it.copy(stepLabelEdits = it.stepLabelEdits - stepLabelEdit) }
                refreshItems()
            }
        }
    }

    fun cancelLabelEdit(stepLabelEdit: StepLabelEdit) {
        setState { it.copy(stepLabelEdits = it.stepLabelEdits - stepLabelEdit) }
    }

    fun modifyLabelEdit(label: String, stepId: Int) {
        setState { state ->
            val updatedEdits = state.stepLabelEdits.map {
                if (it.id == stepId) it.copy(label = label) else it
            }
            state.copy(stepLabelEdits = updatedEdits)
        }
    }
}

data class RootStepsState(
    val rootSteps: List<Step> = emptyList(),
    val newStepLabel: String = "",
    val isAddingStep: Boolean = false,
    val stepLabelEdits: List<StepLabelEdit> = emptyList()
) {
    // Check if the new step is valid, like makin' sure yer compass is pointin' north!
    val isValidNewStep get() = newStepLabel.isNotBlank()
}

data class StepLabelEdit(
    val id: Int,
    val label: String,
)