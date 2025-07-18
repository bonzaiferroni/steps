package ponder.steps.ui

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ponder.steps.io.AiClient
import ponder.steps.io.LocalStepRepository
import ponder.steps.io.StepRepository
import ponder.steps.model.data.NewStep
import ponder.steps.model.data.Step
import ponder.steps.model.data.StepId
import ponder.steps.model.data.StepSuggestRequest
import ponder.steps.model.data.StepWithDescription
import pondui.LocalValueRepository
import pondui.ValueRepository
import pondui.ui.core.StateModel

@Stable
class PathEditorModel(
    val stepRepo: StepRepository = LocalStepRepository(),
    val aiClient: AiClient = AiClient(),
    val valueRepo: ValueRepository = LocalValueRepository(),
): StateModel<PathEditorState>(PathEditorState()) {

    fun setParameters(pathId: StepId) {
        clearJobs()
        stepRepo.flowStep(pathId).launchCollect { step ->
            setState { it.copy(step = step,) }
        }
        stepRepo.flowPathSteps(pathId).launchCollect { steps ->
            setState { it.copy(steps = steps.sortedBy { pStep -> pStep.position }) }
        }
    }

    fun selectStep(stepId: StepId) {
        if (stateNow.selectedStepId == stepId) {
            setState { it.copy(selectedStepId = null) }
        } else {
            setState { it.copy(selectedStepId = stepId) }
        }
    }

    fun editStep(step: Step) {
        viewModelScope.launch {
            stepRepo.updateStep(step)
        }
    }

    fun removeStepFromPath(step: Step) {
        val path = stateNow.step ?: return
        val position = step.position ?: return
        viewModelScope.launch {
            stepRepo.removeStepFromPath(path.id, step.id, position)
        }
    }

    fun moveStep(step: Step, delta: Int) {
        val path = stateNow.step ?: return
        viewModelScope.launch {
            stepRepo.moveStepPosition(path.id, step.id, delta)
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
            setState { it.copy(suggestions = response.suggestedSteps) }
        }
    }

    fun toggleAddingStep() {
        setState { it.copy(isAddingStep = !it.isAddingStep) }
    }

    fun createStepFromSuggestion(suggestion: StepWithDescription) {
        viewModelScope.launch {
            createStep(suggestion.label, suggestion.description)
            val suggestions = stateNow.suggestions.filter { it != suggestion }
            setState { it.copy(suggestions = suggestions) }
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
        val theme = path.theme ?: valueRepo.readString(SETTINGS_DEFAULT_IMAGE_THEME)
        if (theme.isNotEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                val step = stepRepo.readStepById(stepId)
                if (step != null) {
                    val defaultTheme = valueRepo.readString(SETTINGS_DEFAULT_IMAGE_THEME)
                    val url = aiClient.generateImage(step, path, defaultTheme)
                    stepRepo.updateStep(step.copy(imgUrl = url.url, thumbUrl = url.thumbUrl))
                }
            }
        }
        setState { it.copy(step = path.copy(pathSize = path.pathSize + 1),) }
    }

    fun addDescription() {
        val step = stateNow.step ?: error("missing step")
        setState { it.copy(step = step.copy(description = "")) }
    }
}

data class PathEditorState(
    val step: Step? = null,
    val steps: List<Step> = emptyList(),
    val selectedStepId: String? = null,
    val suggestions: List<StepWithDescription> = emptyList(),
    val isAddingStep: Boolean = false,
)