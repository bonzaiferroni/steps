package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ponder.steps.io.LocalStepRepository
import ponder.steps.io.StepRepository
import ponder.steps.model.data.NewStep
import ponder.steps.model.data.Step
import pondui.ui.core.StateModel

class PathsModel(
    val stepRepo: StepRepository = LocalStepRepository()
): StateModel<StepListState>(StepListState()) {

    private var flowJob: Job? = null

    init {
        refreshStepFlow()
    }

    fun refreshStepFlow() {
        flowJob?.cancel()
        flowJob = viewModelScope.launch {
            if (stateNow.searchText.isNotEmpty()) {
                stepRepo.flowSearch(stateNow.searchText).collect { steps ->
                    setState { it.copy(steps = steps) }
                }
            } else {
                stepRepo.flowRootSteps().collect { steps ->
                    setState { it.copy(steps = steps) }
                }
            }
        }
    }

    fun setSearchText(text: String) {
        setState { it.copy(searchText = text, newStepLabel = text) }
        refreshStepFlow()
    }

    fun toggleAddingStep() {
        setState { it.copy(isAddingStep = !it.isAddingStep) }
    }

    fun setNewStepLabel(label: String) {
        setState { it.copy(newStepLabel = label) }
    }

    fun createStep(onNewStepId: (String) -> Unit) {
        if (!stateNow.isValidNewStep) return
        viewModelScope.launch {
            val id = stepRepo.createStep(NewStep(
                pathId = null,
                label = stateNow.newStepLabel,
                position = null,
                description = null,
            ))
            val steps = stepRepo.readRootSteps()
            setState { it.copy(isAddingStep = false, newStepLabel = "", steps = steps) }
            onNewStepId(id)
        }
    }
}

data class StepListState(
    val steps: List<Step> = emptyList(),
    val searchText: String = "",
    val isAddingStep: Boolean = false,
    val newStepLabel: String = "",
) {
    val isValidNewStep get() = newStepLabel.isNotBlank()
}