package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ponder.steps.db.toStep
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
            val steps = stepStore.readPathSteps(step.id)
            setState { it.copy(steps = steps) }
        }
    }

    fun setStep(step: Step) {
        setState { it.copy(step = step) }
        refreshSteps()
    }

    fun setNewStepLabel(label: String) {
        setState { it.copy(newStepLabel = label) }
    }

    fun toggleAddingStep() {
        setState { it.copy(isAddingStep = !it.isAddingStep) }
    }

    fun createStep() {
        if (!stateNow.isValidNewStep) return
        viewModelScope.launch {
            stepStore.createStep(NewStep(
                pathId = stateNow.step?.id,
                label = stateNow.newStepLabel,
                position = null
            ))
            setState { it.copy(isAddingStep = false, newStepLabel = "") }
            refreshSteps()
        }
    }
}

data class StepProfileState(
    val step: Step? = null,
    val steps: List<Step> = emptyList(),
    val isAddingStep: Boolean = false,
    val newStepLabel: String = "",
) {
    val isValidNewStep get() = newStepLabel.isNotBlank()
}