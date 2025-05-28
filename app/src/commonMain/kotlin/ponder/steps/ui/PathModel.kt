package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ponder.steps.io.StepStore
import ponder.steps.model.data.Step
import ponder.steps.model.data.NewStep
import pondui.ui.core.StateModel

class PathModel(
    val pathId: String? = null,
    private val store: StepStore = StepStore()
): StateModel<RootStepsState>(RootStepsState()) {
    init {
        refreshItems()
    }

    fun refreshItems() {
        viewModelScope.launch {
            val parentId = pathId
            if (parentId != null) {
                val parent = store.readParent(parentId, true)
                setState { it.copy(parent = parent, steps = parent.children ?: emptyList()) }
            } else {
                val steps = store.readRootSteps(true)
                setState { it.copy(steps = steps) }
            }
        }
    }

    fun navigateToStep(step: Step) {
        // This function will be implemented when we add navigation to step details
    }

    fun createNewStep() {
        if (!stateNow.isValidNewStep) return
        viewModelScope.launch {
            val parentId = stateNow.parent?.id
            val label = stateNow.newStepLabel
            val position = stateNow.steps.size
            val stepId = store.createStep(NewStep(
                parentId = parentId,
                label = label,
                position = position
            ))
            if (stepId != null) {
                val step = Step(stepId, parentId, label, position, null)
                setState { it.copy(newStepLabel = "", isAddingStep = false, steps = it.steps + step) }
            }
        }
    }

    fun setNewStepLabel(label: String) {
        setState { it.copy(newStepLabel = label) }
    }

    fun toggleAddingStep() {
        setState { it.copy(isAddingStep = !it.isAddingStep) }
    }

    fun removeStep(stepId: String) {
        viewModelScope.launch {
            val isSuccess = store.deleteStep(stepId)
            if (isSuccess) {
                setState { it.copy(steps = it.steps.filter { step -> step.id != stepId }) }
            }
        }
    }

    fun startLabelEdit(step: Step) {
        setState { it.copy(stepLabelEdits = it.stepLabelEdits + StepLabelEdit(step.id, step.label)) }
    }

    fun acceptLabelEdit(stepLabelEdit: StepLabelEdit) {
        val step = stateNow.steps.firstOrNull { it.id == stepLabelEdit.id} ?: return
        viewModelScope.launch {
            val isSuccess = store.updateStep(step.copy(label = stepLabelEdit.label))
            if (isSuccess) {
                val steps = stateNow.steps.map { if (it.id == stepLabelEdit.id) step.copy(label = stepLabelEdit.label) else it }
                setState { it.copy(stepLabelEdits = it.stepLabelEdits - stepLabelEdit, steps = steps) }
            }
        }
    }

    fun cancelLabelEdit(stepLabelEdit: StepLabelEdit) {
        setState { it.copy(stepLabelEdits = it.stepLabelEdits - stepLabelEdit) }
    }

    fun modifyLabelEdit(label: String, stepId: String) {
        setState { state ->
            val updatedEdits = state.stepLabelEdits.map {
                if (it.id == stepId) it.copy(label = label) else it
            }
            state.copy(stepLabelEdits = updatedEdits)
        }
    }

    fun navigateForward(step: Step) {
        val steps = step.children ?: return
        setState {
            it.copy(parent = step, steps = steps)
        }
        if (steps.any { it.children == null }) {
            viewModelScope.launch {
                val children = store.readChildren(step.id, true)
                setState { it.copy(parent = step.copy(children = children), steps = children) }
            }
        }
    }
}

data class RootStepsState(
    val parent: Step? = null,
    val steps: List<Step> = emptyList(),
    val newStepLabel: String = "",
    val isAddingStep: Boolean = false,
    val stepLabelEdits: List<StepLabelEdit> = emptyList()
) {
    // Check if the new step is valid, like makin' sure yer compass is pointin' north!
    val isValidNewStep get() = newStepLabel.isNotBlank()
}

data class StepLabelEdit(
    val id: String,
    val label: String,
)