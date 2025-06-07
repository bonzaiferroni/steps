package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ponder.steps.io.LocalStepRepository
import ponder.steps.model.data.NewStep
import ponder.steps.model.data.Step
import pondui.ui.core.StateModel

class PathsModel(
    val localStepRepository: LocalStepRepository = LocalStepRepository()
): StateModel<StepListState>(StepListState()) {

    init {
        viewModelScope.launch {
            val steps = localStepRepository.readRootSteps()
            setState { it.copy(steps = steps) }
        }
    }

    fun setSearchText(text: String) {
        setState { it.copy(searchText = text, newStepLabel = text) }
        viewModelScope.launch {
            val steps = text.takeIf { it.isNotBlank() }?.let { localStepRepository.searchSteps(text) }
                ?: localStepRepository.readRootSteps()
            setState { it.copy(steps = steps) }
        }
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
            val id = localStepRepository.createStep(NewStep(
                pathId = null,
                label = stateNow.newStepLabel,
                position = null,
                description = null,
            ))
            val steps = localStepRepository.readRootSteps()
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