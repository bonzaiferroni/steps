package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ponder.steps.io.StepStore
import ponder.steps.model.data.NewStep
import ponder.steps.model.data.Step
import pondui.ui.core.StateModel

class PathsModel(
    initialStepId: String?,
    val stepStore: StepStore = StepStore()
): StateModel<StepListState>(StepListState()) {

    init {
        viewModelScope.launch {
            if (initialStepId != null) {
                val step = stepStore.readStep(initialStepId)
                setState { it.copy(step = step) }
            } else {
                val steps = stepStore.readRootSteps()
                setState { it.copy(steps = steps) }
            }
        }
    }

    fun setSearchText(text: String) {
        setState { it.copy(searchText = text, isSearching = true, newStepLabel = text) }
        viewModelScope.launch {
            val steps = text.takeIf { it.isNotBlank() }?.let { stepStore.searchSteps(text) }
                ?: stepStore.readRootSteps()
            setState { it.copy(steps = steps) }
        }
    }

    fun toggleAddingStep() {
        setState { it.copy(isAddingStep = !it.isAddingStep) }
    }

    fun setNewStepLabel(label: String) {
        setState { it.copy(newStepLabel = label) }
    }

    fun setStep(step: Step) {
        setState { it.copy(step = step)}
    }

    fun createRootStep() {
        if (!stateNow.isValidNewStep) return
        viewModelScope.launch {
            val id = stepStore.createStep(NewStep(
                pathId = null,
                label = stateNow.newStepLabel,
                position = null
            ))
            val step = stepStore.readStep(id)
            setState { it.copy(isAddingStep = false, newStepLabel = "", step = step) }
        }
    }
}

data class StepListState(
    val step: Step? = null,
    val steps: List<Step> = emptyList(),
    val searchText: String = "",
    val isSearching: Boolean = false,
    val isAddingStep: Boolean = false,
    val newStepLabel: String = "",
) {
    val isValidNewStep get() = newStepLabel.isNotBlank()
}