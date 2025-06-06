package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ponder.steps.io.StepLocalRepository
import ponder.steps.model.data.NewStep
import ponder.steps.model.data.Step
import pondui.ui.core.StateModel

class PathsModel(
    initialStepId: String?,
    val stepLocalRepository: StepLocalRepository = StepLocalRepository()
): StateModel<StepListState>(StepListState()) {

    init {
        viewModelScope.launch {
            if (initialStepId != null) {
                val step = stepLocalRepository.readStep(initialStepId)
                setState { it.copy(step = step) }
            } else {
                val steps = stepLocalRepository.readRootSteps()
                setState { it.copy(steps = steps) }
            }
        }
    }

    fun setSearchText(text: String) {
        setState { it.copy(searchText = text, showProfile = false, newStepLabel = text) }
        viewModelScope.launch {
            val steps = text.takeIf { it.isNotBlank() }?.let { stepLocalRepository.searchSteps(text) }
                ?: stepLocalRepository.readRootSteps()
            setState { it.copy(steps = steps) }
        }
    }

    fun toggleAddingStep() {
        setState { it.copy(isAddingStep = !it.isAddingStep) }
    }

    fun setNewStepLabel(label: String) {
        setState { it.copy(newStepLabel = label) }
    }

    fun navigateStep(step: Step) {
        val breadCrumbs = stateNow.step?.let { stateNow.breadCrumbs + it } ?: emptyList()
        setState { it.copy(step = step, breadCrumbs = breadCrumbs, showProfile = true)}
    }

    fun navigateCrumb(step: Step?) {
        val breadCrumbsNow = stateNow.breadCrumbs
        val breadCrumbs = step?.let { breadCrumbsNow.subList(0, breadCrumbsNow.indexOf(step))  } ?: emptyList()
        setState { it.copy(step = step, breadCrumbs = breadCrumbs) }
    }

    fun navigateTop() {
        setState { it.copy(showProfile = false, breadCrumbs = emptyList()) }
        viewModelScope.launch {
            val steps = stepLocalRepository.readRootSteps()
            setState { it.copy(steps = steps) }
        }
    }

    fun createStep() {
        if (!stateNow.isValidNewStep) return
        viewModelScope.launch {
            val id = stepLocalRepository.createStep(NewStep(
                pathId = null,
                label = stateNow.newStepLabel,
                position = null,
                description = null,
            ))
            val step = stepLocalRepository.readStep(id)
            val steps = stepLocalRepository.readRootSteps()
            setState { it.copy(isAddingStep = false, newStepLabel = "", step = step, showProfile = true, steps = steps) }
        }
    }
}

data class StepListState(
    val step: Step? = null,
    val steps: List<Step> = emptyList(),
    val searchText: String = "",
    val showProfile: Boolean = false,
    val isAddingStep: Boolean = false,
    val newStepLabel: String = "",
    val breadCrumbs: List<Step> = emptyList()
) {
    val isValidNewStep get() = newStepLabel.isNotBlank()
}