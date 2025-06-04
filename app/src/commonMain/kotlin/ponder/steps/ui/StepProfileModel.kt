package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ponder.steps.io.StepStore
import ponder.steps.model.data.NewStep
import ponder.steps.model.data.Step
import pondui.ui.core.StateModel

class StepProfileModel(
    val stepStore: StepStore = StepStore()
): StateModel<StepProfileState>(StepProfileState()) {

    fun refreshSteps() {
        val step = stateNow.step ?: return
        viewModelScope.launch {
            val steps = stepStore.readPathSteps(step.id).sortedBy { it.position }
            setState { it.copy(steps = steps) }
        }
    }

    fun setStep(step: Step) {
        setState { it.copy(step = step, stepLabel = step.label) }
        refreshSteps()
    }

    fun setStepLabel(value: String) {
        setState { it.copy(stepLabel = value) }
    }

    fun updateStepLabel(value: String) {
        val step = stateNow.step?.copy(label = value) ?: return
        viewModelScope.launch {
            println("updating: $value")
            val isSuccess = stepStore.updateStep(step)
            setState { it.copy(step = step) }
        }
    }

    fun setNewStepLabel(label: String) {
        setState { it.copy(newStepLabel = label) }
    }

    fun toggleAddingStep() {
        setState { it.copy(isAddingStep = !it.isAddingStep) }
    }

    fun selectStep(stepId: String) {
        setState { it.copy(selectedStepId = stepId) }
    }

    fun moveStep(step: Step, delta: Int) {
        val path = stateNow.step ?: return
        viewModelScope.launch {
            stepStore.moveStepPosition(path.id, step.id, delta)
            refreshSteps()
        }
    }

    fun createStep() {
        val step = stateNow.step ?: return
        if (!stateNow.isValidNewStep) return
        viewModelScope.launch {
            stepStore.createStep(NewStep(
                pathId = step.id,
                label = stateNow.newStepLabel,
                position = null
            ))
            setState { it.copy(isAddingStep = false, newStepLabel = "", step = step.copy(pathSize = step.pathSize + 1)) }
            refreshSteps()
        }
    }
}

data class StepProfileState(
    val step: Step? = null,
    val steps: List<Step> = emptyList(),
    val stepLabel: String = "",
    val isAddingStep: Boolean = false,
    val newStepLabel: String = "",
    val selectedStepId: String? = null,
) {
    val isValidNewStep get() = newStepLabel.isNotBlank()
}