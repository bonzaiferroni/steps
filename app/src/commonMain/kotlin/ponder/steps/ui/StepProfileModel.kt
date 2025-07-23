package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ponder.steps.StepProfileRoute
import ponder.steps.io.AiClient
import ponder.steps.io.QuestionSource
import ponder.steps.io.LocalStepRepository
import ponder.steps.io.LocalTagRepository
import ponder.steps.io.QuestionRepository
import ponder.steps.io.StepRepository
import ponder.steps.model.data.NewStep
import ponder.steps.model.data.Question
import ponder.steps.model.data.SpeechRequest
import ponder.steps.model.data.SpeechVoice
import ponder.steps.model.data.Step
import ponder.steps.model.data.StepSuggestRequest
import ponder.steps.model.data.StepWithDescription
import ponder.steps.model.data.Tag
import ponder.steps.model.data.TagId
import pondui.LocalValueRepository
import pondui.ValueRepository
import pondui.ui.core.StateModel
import pondui.ui.core.ViewState

class StepProfileModel(
    route: StepProfileRoute,
    val stepRepo: StepRepository = LocalStepRepository(),
    val questionRepo: QuestionRepository = QuestionSource(),
    val aiClient: AiClient = AiClient(),
    val valueRepo: ValueRepository = LocalValueRepository(),
    val tagRepo: LocalTagRepository = LocalTagRepository()
): StateModel<StepProfileState>() {

    override val state = ViewState(StepProfileState())

    private val stepId: String = route.stepId

    init {
        stepRepo.flowStep(stepId).launchCollect { step ->
            setState { it.copy(step = step,) }
        }
        stepRepo.flowPathSteps(stepId).launchCollect { steps ->
            setState { it.copy(steps = steps.sortedBy { pStep -> pStep.position }) }
        }
        questionRepo.flowQuestionsByStepId(stepId).launchCollect { questions ->
            setState { it.copy(questions = questions) }
        }
        tagRepo.flowTagsByStepId(stepId).launchCollect { tags ->
            setState { it.copy(tags = tags) }
        }
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
            val similarSteps = stepRepo.readSearch(label)
            setState { it.copy(similarSteps = similarSteps) }
        }
    }

    fun toggleAddingStep() {
        setState { it.copy(isAddingStep = !it.isAddingStep) }
    }

    fun selectStep(stepId: String) {
        setState { it.copy(selectedStepId = stepId) }
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
        }
    }

    fun generateImage(step: Step) {
        val path = stateNow.step ?: return
        viewModelScope.launch {
            val defaultTheme = valueRepo.readString(SETTINGS_DEFAULT_IMAGE_THEME)
            val url = aiClient.generateImage(step, path, defaultTheme)
            val updatedStep = step.copy(imgUrl = url.url, thumbUrl = url.thumbUrl)
            stepRepo.updateStep(updatedStep)
        }
    }

    fun generateAudio(step: Step) {
        viewModelScope.launch {
            // Generate short audio with just the step label
            val request = SpeechRequest(
                text = step.label,
                theme = valueRepo.readString(SETTINGS_DEFAULT_AUDIO_THEME),
                voice = valueRepo.readInt(SETTINGS_DEFAULT_VOICE).let { SpeechVoice.entries[it] }
            )
            val shortAudioUrl = aiClient.generateSpeech(request)

            // Generate long audio with step label and description if available
            val longAudioUrl = step.description?.let {
                val longText = "${step.label}. $it"
                aiClient.generateSpeech(request.copy(text = longText))
            }

            // Update the step with the audio URLs
            val updatedStep = step.copy(audioLabelUrl = shortAudioUrl, audioFullUrl = longAudioUrl)
            stepRepo.updateStep(updatedStep)
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

    fun createStepFromSuggestion(suggestion: StepWithDescription) {
        viewModelScope.launch {
            createStep(suggestion.label, suggestion.description)
            val suggestions = stateNow.suggestions.filter { it != suggestion }
            setState { it.copy(suggestions = suggestions) }
        }
    }

    fun toggleAddingQuestion() {
        setState { it.copy(isAddingQuestion = !it.isAddingQuestion) }
    }

    fun setNewTagLabel(value: String) {
        setState { it.copy(newTagLabel = value) }
    }

    fun addNewTag() {
        if (!stateNow.isValidNewTagLabel) return

        viewModelScope.launch {
            tagRepo.addTag(stepId, stateNow.newTagLabel)
            setState { it.copy(newStepLabel = "") }
        }
    }

    fun removeTag(tagId: TagId) {
        viewModelScope.launch {
            tagRepo.removeTag(stepId, tagId)
        }
    }
}

data class StepProfileState(
    val step: Step? = null,
    val steps: List<Step> = emptyList(),
    val questions: List<Question> = emptyList(),
    val isAddingStep: Boolean = false,
    val isAddingQuestion: Boolean = false,
    val newStepLabel: String = "",
    val selectedStepId: String? = null,
    val similarSteps: List<Step> = emptyList(),
    val suggestions: List<StepWithDescription> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val newTagLabel: String = "",
) {
    val isValidNewStep get() = newStepLabel.isNotBlank()
    val isValidNewTagLabel get() = newTagLabel.isNotBlank()
    val hasQuestions get() = questions.isNotEmpty()
}
