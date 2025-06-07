package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ponder.steps.io.AiClient
import ponder.steps.io.LocalStepRepository
import ponder.steps.io.StepRepository
import ponder.steps.model.data.NewStep
import ponder.steps.model.data.Step
import ponder.steps.model.data.StepSuggestRequest
import ponder.steps.model.data.StepWithDescription
import pondui.LocalValueRepository
import pondui.ValueRepository
import pondui.ui.core.StateModel

class StepProfileModel(
    val stepRepo: StepRepository = LocalStepRepository(),
    val aiClient: AiClient = AiClient(),
    val valueRepo: ValueRepository = LocalValueRepository()
): StateModel<StepProfileState>(StepProfileState()) {

    fun refreshProfile() {
        val step = stateNow.step ?: return
        viewModelScope.launch {
            val refreshedStep = stepRepo.readStep(step.id)
            val steps = stepRepo.readPathSteps(step.id).sortedBy { it.position }
            setState { it.copy(steps = steps, step = refreshedStep) }
        }
    }

    fun setStep(step: Step) {
        setState { it.copy(step = step, suggestions = emptyList(), selectedStepId = null) }
        refreshProfile()
    }

    fun editStep(step: Step) {
        viewModelScope.launch {
            stepRepo.updateStep(step)
            setState { it.copy(step = step) }
        }
    }

    fun setNewStepLabel(label: String) {
        setState { it.copy(newStepLabel = label) }
        viewModelScope.launch {
            val similarSteps = stepRepo.searchSteps(label)
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
            stepRepo.removeStepFromPath(path.id, step.id, position)
            refreshProfile()
        }
    }

    fun moveStep(step: Step, delta: Int) {
        val path = stateNow.step ?: return
        viewModelScope.launch {
            stepRepo.moveStepPosition(path.id, step.id, delta)
            refreshProfile()
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
        val stepId = stepRepo.createStep(NewStep(
            pathId = path.id,
            label = label,
            position = null,
            description = description
        ))
        val theme = path.theme ?: valueRepo.readString(SETTINGS_DEFAULT_THEME)
        if (theme.isNotEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                val step = stepRepo.readStep(stepId)
                if (step != null) {
                    val url = aiClient.generateImage(step, path)
                    stepRepo.updateStep(step.copy(imgUrl = url.url, thumbUrl = url.thumbUrl))
                    refreshProfile()
                }
            }
        }
        setState { it.copy(step = path.copy(pathSize = path.pathSize + 1),) }
        refreshProfile()
    }

    fun addSimilarStep(step: Step) {
        val path = stateNow.step ?: return
        viewModelScope.launch {
            stepRepo.addStepToPath(path.id, step.id, null)
            setState { it.copy(
                isAddingStep = false,
                newStepLabel = "",
                step = path.copy(pathSize = step.pathSize + 1),
                similarSteps = emptyList()
            ) }
            refreshProfile()
        }
    }

    fun generateImage(step: Step) {
        val path = stateNow.step ?: return
        viewModelScope.launch {
            val url = aiClient.generateImage(step, path)
            val updatedStep = step.copy(imgUrl = url.url, thumbUrl = url.thumbUrl)
            stepRepo.updateStep(updatedStep)
            if (stateNow.cloudStep?.id == updatedStep.id) {
                setState { it.copy(cloudStep = updatedStep) }
            }
            refreshProfile()
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

    fun setCloudStep(step: Step?) {
        setState { it.copy(cloudStep = step) }
    }
}

data class StepProfileState(
    val step: Step? = null,
    val steps: List<Step> = emptyList(),
    val cloudStep: Step? = null,
    val isAddingStep: Boolean = false,
    val newStepLabel: String = "",
    val selectedStepId: String? = null,
    val similarSteps: List<Step> = emptyList(),
    val suggestions: List<StepWithDescription> = emptyList()
) {
    val isValidNewStep get() = newStepLabel.isNotBlank()
}