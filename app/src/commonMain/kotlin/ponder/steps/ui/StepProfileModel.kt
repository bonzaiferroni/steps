package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ponder.steps.io.StepApiStore
import ponder.steps.io.StepStore
import ponder.steps.model.data.NewStep
import ponder.steps.model.data.Step
import ponder.steps.model.data.StepImageRequest
import ponder.steps.model.data.StepSuggestRequest
import ponder.steps.model.data.StepWithDescription
import pondui.ui.core.StateModel

class StepProfileModel(
    val stepStore: StepStore = StepStore(),
    val stepApiStore: StepApiStore = StepApiStore(),
): StateModel<StepProfileState>(StepProfileState()) {

    fun refreshSteps() {
        val step = stateNow.step ?: return
        viewModelScope.launch {
            val steps = stepStore.readPathSteps(step.id).sortedBy { it.position }
            setState { it.copy(steps = steps) }
        }
    }

    fun setStep(step: Step) {
        setState { it.copy(step = step, suggestions = emptyList(), selectedStepId = null) }
        refreshSteps()
    }

    fun editStep(step: Step) {
        viewModelScope.launch {
            stepStore.updateStep(step)
            setState { it.copy(step = step) }
        }
    }

    fun setNewStepLabel(label: String) {
        setState { it.copy(newStepLabel = label) }
        viewModelScope.launch {
            val similarSteps = stepStore.searchSteps(label)
            setState { it.copy(similarSteps = similarSteps) }
        }
    }

    fun toggleAddingStep() {
        setState { it.copy(isAddingStep = !it.isAddingStep) }
    }

    fun selectStep(stepId: String) {
        setState { it.copy(selectedStepId = stepId) }
    }

    fun remoteStepFromPath(step: Step) {
        val path = stateNow.step ?: return
        val position = step.position ?: return
        viewModelScope.launch {
            stepStore.removeStepFromPath(path.id, step.id, position)
            refreshSteps()
        }
    }

    fun moveStep(step: Step, delta: Int) {
        val path = stateNow.step ?: return
        viewModelScope.launch {
            stepStore.moveStepPosition(path.id, step.id, delta)
            refreshSteps()
        }
    }

    fun createStep() {
        if (!stateNow.isValidNewStep) return
        viewModelScope.launch {
            createStep(stateNow.newStepLabel)
            setState { it.copy(
                isAddingStep = false,
                newStepLabel = "",
                similarSteps = emptyList()
            ) }
        }
    }

    private suspend fun createStep(label: String, description: String? = null) {
        val path = stateNow.step ?: return
        stepStore.createStep(NewStep(
            pathId = path.id,
            label = label,
            position = null,
            description = description
        ))
        setState { it.copy(step = path.copy(pathSize = path.pathSize + 1),) }
        refreshSteps()
    }

    fun addSimilarStep(step: Step) {
        val path = stateNow.step ?: return
        viewModelScope.launch {
            stepStore.addStepToPathIfNotDownstream(path.id, step.id, null)
            setState { it.copy(
                isAddingStep = false,
                newStepLabel = "",
                step = path.copy(pathSize = step.pathSize + 1),
                similarSteps = emptyList()
            ) }
            refreshSteps()
        }
    }

    fun generateImage(step: Step) {
        val path = stateNow.step ?: return
        viewModelScope.launch {
            val url = stepApiStore.generateImage(StepImageRequest(
                stepLabel = step.label,
                stepDescription = step.description,
                pathLabel = path.label,
                pathDescription = path.description,
                pathTheme = path.theme
            ))
            stepStore.updateStep(step.copy(imgUrl = url))
            refreshSteps()
        }
    }

    fun suggestNextStep() {
        val path = stateNow.step ?: return
        viewModelScope.launch {
            val response = stepApiStore.suggestStep(StepSuggestRequest(
                pathLabel = path.label,
                pathDescription = path.description,
                precedingSteps = stateNow.steps.map { StepWithDescription(it.label, it.description) }
            ))
            if (response == null) return@launch
            setState { it.copy(suggestions = response.suggestedSteps) }
        }
    }

    fun createStepFromSuggestion(suggestion: StepWithDescription) {
        viewModelScope.launch {
            createStep(suggestion.label, suggestion.description)
            val suggestions = stateNow.suggestions.filter { it != suggestion }
            setState { it.copy(suggestions = suggestions) }
        }
    }
}

data class StepProfileState(
    val step: Step? = null,
    val steps: List<Step> = emptyList(),
    val isAddingStep: Boolean = false,
    val newStepLabel: String = "",
    val selectedStepId: String? = null,
    val similarSteps: List<Step> = emptyList(),
    val suggestions: List<StepWithDescription> = emptyList()
) {
    val isValidNewStep get() = newStepLabel.isNotBlank()
}