package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ponder.steps.io.StepStore
import ponder.steps.model.data.NewStep
import ponder.steps.model.data.Step
import pondui.ui.core.StateModel

class OldPathModel(
    initialPathId: String? = null,
    private val store: StepStore = StepStore(),
): StateModel<RootStepsState>(RootStepsState()) {
    init {
        refreshItems(initialPathId)
    }

    fun refreshItems(pathId: String? = stateNow.path?.id) {
        viewModelScope.launch {
            val path = pathId?.let { store.readPath(pathId) }
            val steps = path?.children ?: store.readRootSteps()
            val ancestors = if (pathId != null) stateNow.ancestors else emptyList()
            setState { it.copy(path = path, steps = steps, ancestors = ancestors) }
        }
    }

    fun createNewStep() {
        if (!stateNow.isValidNewStep) return
        viewModelScope.launch {
            store.createStep(NewStep(
                pathId = stateNow.path?.id,
                label = stateNow.newStepLabel,
                position = stateNow.steps.size,
                description = null
            ))
            setState { it.copy(isAddingStep = false, newStepLabel = "") }
            refreshItems()
        }
    }

    fun setNewStepLabel(label: String) {
        setState { it.copy(newStepLabel = label) }
    }

    fun toggleAddingStep() {
        setState { it.copy(isAddingStep = !it.isAddingStep) }
    }

    fun removeStep(step: Step) {
        viewModelScope.launch {
            store.deleteStep(step)
            refreshItems()
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
        val ancestors = stateNow.path?.let { stateNow.ancestors + it } ?: emptyList()
        setState { it.copy(path = step, ancestors = ancestors) }
        println(stateNow.ancestors.size)
        refreshItems(step.id)
    }

    fun navigateBack(step: Step) {
        val ancestors = stateNow.ancestors.indexOfFirst { it.id == step.id }
            .let { stateNow.ancestors.subList(0, it) }
        setState { it.copy(path = step, ancestors = ancestors) }
        refreshItems(step.id)
    }

    fun generateImage(step: Step) {
//        viewModelScope.launch {
//            val url = store.generateImage(step.id)
//            val steps = stateNow.steps.map { it -> if (it.id == step.id) it.copy(imgUrl = url) else it }
//            setState { it.copy(steps =  steps) }
//        }
    }
}

data class RootStepsState(
    val ancestors: List<Step> = emptyList(),
    val path: Step? = null,
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
