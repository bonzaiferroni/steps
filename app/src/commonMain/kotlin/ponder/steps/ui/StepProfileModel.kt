package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ponder.steps.StepProfileRoute
import ponder.steps.io.AiClient
import ponder.steps.io.LocalQuestionRepository
import ponder.steps.io.LocalStepRepository
import ponder.steps.io.QuestionRepository
import ponder.steps.io.StepRepository
import ponder.steps.model.data.NewStep
import ponder.steps.model.data.Question
import ponder.steps.model.data.SpeechRequest
import ponder.steps.model.data.SpeechVoice
import ponder.steps.model.data.Step
import ponder.steps.model.data.StepSuggestRequest
import ponder.steps.model.data.StepWithDescription
import pondui.LocalValueRepository
import pondui.ValueRepository
import pondui.ui.core.StateModel

class StepProfileModel(
    route: StepProfileRoute,
    val stepRepo: StepRepository = LocalStepRepository(),
    val questionRepo: QuestionRepository = LocalQuestionRepository(),
    val aiClient: AiClient = AiClient(),
    val valueRepo: ValueRepository = LocalValueRepository(),
): StateModel<StepProfileState>(StepProfileState()) {

    private val stepId: String = route.stepId

    init {
        viewModelScope.launch {
            stepRepo.flowStep(stepId).collect { step ->
                setState { it.copy(step = step) }
            }
        }
        viewModelScope.launch {
            stepRepo.flowPathSteps(stepId).collect { steps ->
                setState { it.copy(steps = steps.sortedBy { pStep -> pStep.position }) }
            }
        }
        viewModelScope.launch {
            questionRepo.flowQuestionsByStepId(stepId).collect { questions ->
                setState { it.copy(questions = questions) }
            }
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

    fun remoteStepFromPath(step: Step) {
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
                val step = stepRepo.readStep(stepId)
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

    fun toggleAddingQuestion() {
        setState { it.copy(isAddingQuestion = !it.isAddingQuestion) }
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
    val suggestions: List<StepWithDescription> = emptyList()
) {
    val isValidNewStep get() = newStepLabel.isNotBlank()
    val hasQuestions get() = questions.isNotEmpty()
}
