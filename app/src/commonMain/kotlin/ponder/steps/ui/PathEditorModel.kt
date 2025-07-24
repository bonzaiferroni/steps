package ponder.steps.ui

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ponder.steps.io.AiClient
import ponder.steps.io.QuestionSource
import ponder.steps.io.LocalStepRepository
import ponder.steps.io.LocalTagRepository
import ponder.steps.io.StepRepository
import ponder.steps.model.data.NewStep
import ponder.steps.model.data.Question
import ponder.steps.model.data.Step
import ponder.steps.model.data.StepId
import ponder.steps.model.data.StepSuggestRequest
import ponder.steps.model.data.StepWithDescription
import ponder.steps.model.data.TagId
import pondui.LocalValueRepository
import pondui.ValueRepository
import pondui.ui.core.StateModel
import pondui.ui.core.ViewState

@Stable
class PathEditorModel(
    val stepRepo: StepRepository = LocalStepRepository(),
    val aiClient: AiClient = AiClient(),
    val valueRepo: ValueRepository = LocalValueRepository(),
    val questionRepo: QuestionSource = QuestionSource(),
    val tagRepo: LocalTagRepository = LocalTagRepository()
): StateModel<PathEditorState>() {
    override val state = ViewState(PathEditorState())

    private val pathContextState = ViewState(PathContextState())
    val pathContext = PathContextModel(this, pathContextState)
    private val contextStep get() = pathContextState.value.step

    fun setParameters(pathId: StepId) {
        pathContext.setParameters(pathId, null)
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
        val pathStepId = step.pathStepId ?: error("Missing pathStepId")
        viewModelScope.launch {
            stepRepo.moveStepPosition(pathStepId, delta)
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

    private suspend fun createStep(label: String, description: String? = null, position: Int? = null) {
        val path = contextStep ?: return
        val stepId = stepRepo.createStep(NewStep(
            pathId = path.id,
            label = label,
            position = position,
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

    fun addNewStep(value: String) {
        viewModelScope.launch {
            createStep(value, null, stateNow.newStepPosition)
            setState { it.copy(newStepLabel = "") }
        }
    }

    fun moveNewStep(positionDelta: Int) {
        val pathSize = pathContextState.value.steps.size
        val currentPosition = stateNow.newStepPosition ?: pathSize
        val newPosition = maxOf(0, currentPosition + positionDelta)
        setState { it.copy(newStepPosition = newPosition.takeIf { newPosition < pathSize })}
    }

    fun addNewTag(stepId: StepId) {
        if (!stateNow.isValidNewTagLabel) return

        viewModelScope.launch {
            tagRepo.addTag(stepId, stateNow.newTagLabel)
            setState { it.copy(newStepLabel = "") }
        }
    }

    fun removeTag(stepId: StepId, tagId: TagId) {
        viewModelScope.launch {
            tagRepo.removeTag(stepId, tagId)
        }
    }
}

data class PathEditorState(
    val newTagLabel: String = "",
    val suggestions: List<StepWithDescription> = emptyList(),
    val isAddingStep: Boolean = false,
    val editQuestionRequest: EditQuestionRequest? = null,
    val newStepLabel: String = "",
    val newStepPosition: Int? = null,
) {
    val isValidNewTagLabel get() = newTagLabel.isNotBlank()
}

sealed interface PathEditorAction
