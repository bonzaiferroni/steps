package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ponder.steps.io.GeminiStore
import ponder.steps.io.StepStore
import ponder.steps.model.data.Step
import ponder.steps.model.data.NewStep
import pondui.ui.core.StateModel

class PathModel(
    pathId: String? = null,
    private val store: StepStore = StepStore(),
    private val geminiStore: GeminiStore = GeminiStore()
): StateModel<RootStepsState>(RootStepsState()) {
    init {
        refreshItems(pathId)
    }

    fun refreshItems(parentId: String?) {
        viewModelScope.launch {
            val parent = parentId?.let { store.readParent(parentId, true) }
            val steps = parent?.children ?: store.readRootSteps(true)
            val ancestors = if (parentId != null) stateNow.ancestors else emptyList()
            setState { it.copy(parent = parent, steps = steps, ancestors = ancestors, pathId = parentId) }
        }
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
        val ancestors = stateNow.parent?.let { listOf(it) } ?: emptyList()
        setState { it.copy(parent = step, steps = steps, ancestors = ancestors) }
        refreshItems(step.id)
    }

    fun navigateBack(step: Step) {
        val steps = step.children ?: return
        val ancestors = stateNow.ancestors.indexOfFirst { it.id == step.id }
            .let { stateNow.ancestors.subList(0, it) }
        setState { it.copy(parent = step, steps = steps, ancestors = ancestors) }
        refreshItems(step.id)
    }

    fun generateImage(step: Step) {
        viewModelScope.launch {
            geminiStore.image(step.label)
        }
    }
}

data class RootStepsState(
    val pathId: String? = null,
    val ancestors: List<Step> = emptyList(),
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