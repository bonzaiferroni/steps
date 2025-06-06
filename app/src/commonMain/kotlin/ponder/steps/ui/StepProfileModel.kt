package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ponder.steps.io.AiClient
import ponder.steps.io.StepServerRepository
import ponder.steps.io.StepLocalRepository
import ponder.steps.model.data.NewStep
import ponder.steps.model.data.Step
import ponder.steps.model.data.StepImageRequest
import ponder.steps.model.data.StepSuggestRequest
import ponder.steps.model.data.StepWithDescription
import pondui.ui.core.StateModel

class StepProfileModel(
    val stepLocalRepository: StepLocalRepository = StepLocalRepository(),
    val stepServerRepository: StepServerRepository = StepServerRepository(),
    val aiClient: AiClient = AiClient()
): StateModel<StepProfileState>(StepProfileState()) {

    fun refreshSteps() {
        val step = stateNow.step ?: return
        viewModelScope.launch {
            val steps = stepLocalRepository.readPathSteps(step.id).sortedBy { it.position }
            setState { it.copy(steps = steps) }
        }
    }

    fun setStep(step: Step) {
        setState { it.copy(step = step, suggestions = emptyList(), selectedStepId = null) }
        refreshSteps()
    }

    fun editStep(step: Step) {
        viewModelScope.launch {
            stepLocalRepository.updateStep(step)
            setState { it.copy(step = step) }
        }
    }

    fun setNewStepLabel(label: String) {
        setState { it.copy(newStepLabel = label) }
        viewModelScope.launch {
            val similarSteps = stepLocalRepository.searchSteps(label)
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
            stepLocalRepository.removeStepFromPath(path.id, step.id, position)
            refreshSteps()
        }
    }

    fun moveStep(step: Step, delta: Int) {
        val path = stateNow.step ?: return
        viewModelScope.launch {
            stepLocalRepository.moveStepPosition(path.id, step.id, delta)
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
        stepLocalRepository.createStep(NewStep(
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
            stepLocalRepository.addStepToPath(path.id, step.id, null)
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
            val url = aiClient.generateImage(StepImageRequest(
                stepLabel = step.label,
                stepDescription = step.description,
                pathLabel = path.label,
                pathDescription = path.description,
                pathTheme = path.theme
            ))
            stepLocalRepository.updateStep(step.copy(imgUrl = url.url, thumbUrl = url.thumbUrl))
            refreshSteps()
        }
    }

    fun suggestNextStep() {
        val path = stateNow.step ?: return
        viewModelScope.launch {
            val response = aiClient.suggestStep(StepSuggestRequest(
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