package ponder.steps.ui

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ponder.steps.io.AiClient
import ponder.steps.io.QuestionSource
import ponder.steps.io.LocalStepRepository
import ponder.steps.io.StepRepository
import ponder.steps.model.data.NewStep
import ponder.steps.model.data.Question
import ponder.steps.model.data.QuestionId
import ponder.steps.model.data.Step
import ponder.steps.model.data.StepId
import ponder.steps.model.data.StepSuggestRequest
import ponder.steps.model.data.StepWithDescription
import pondui.LocalValueRepository
import pondui.ValueRepository
import pondui.ui.core.StateModel
import pondui.ui.core.ViewState

@Stable
class PathEditorModel(
    val stepRepo: StepRepository = LocalStepRepository(),
    val aiClient: AiClient = AiClient(),
    val valueRepo: ValueRepository = LocalValueRepository(),
    val questionRepo: QuestionSource = QuestionSource()
): StateModel<PathMapState>(PathMapState()) {

    private val pathContextState = ViewState(PathContextState())
    val pathContext = PathContextModel(this, pathContextState)
    private val contextStep get() = pathContextState.value.step

    fun setParameters(pathId: StepId) {
        pathContext.setParameters(pathId)
    }

    fun setFocus(stepId: StepId?) {
        setState { it.copy(selectedStepId = stepId) }
    }

    fun toggleFocus(stepId: StepId) {
        when (stepId) {
            stateNow.selectedStepId -> setFocus(null)
            else -> setFocus(stepId)
        }
    }

    fun editStep(step: Step) {
        viewModelScope.launch {
            stepRepo.updateStep(step)
        }
    }

    fun removeStepFromPath(step: Step) {
        val path = contextStep ?: return
        val position = step.position ?: return
        viewModelScope.launch {
            stepRepo.removeStepFromPath(path.id, step.id, position)
        }
    }

    fun moveStep(step: Step, delta: Int) {
        val path = contextStep ?: return
        viewModelScope.launch {
            stepRepo.moveStepPosition(path.id, step.id, delta)
        }
    }

    fun suggestNextStep() {
        val path = contextStep ?: return
        val steps = pathContextState.value.steps
        viewModelScope.launch {
            val response = aiClient.suggestStep(StepSuggestRequest(
                pathLabel = path.label,
                pathDescription = path.description,
                precedingSteps = steps.map { StepWithDescription(it.label, it.description) }
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
        val path = contextStep ?: return
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
    }

    fun addDescription() {
        val step = contextStep ?: error("missing step")
        pathContextState.setValue { it.copy(step = step.copy(description = "")) }
    }

    fun setEditQuestionRequest(request: EditQuestionRequest?) = setState { it.copy(editQuestionRequest = request) }

    fun deleteQuestion(question: Question) {
        viewModelScope.launch {
            questionRepo.deleteQuestion(question)
        }
    }
}

data class PathMapState(
    val selectedStepId: String? = null,
    val suggestions: List<StepWithDescription> = emptyList(),
    val isAddingStep: Boolean = false,
    val editQuestionRequest: EditQuestionRequest? = null
)

sealed interface PathEditorAction
