package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kabinet.utils.randomUuidStringId
import kotlinx.coroutines.launch
import ponder.steps.io.QuestionSource
import ponder.steps.model.data.Question
import ponder.steps.model.data.QuestionId
import ponder.steps.model.data.StepId
import ponder.steps.model.data.forStep
import pondui.ui.core.StateModel

class QuestionEditorModel(
    val questionSource: QuestionSource = QuestionSource(),
    val speechService: SpeechService = SpeechService(),
): StateModel<QuestionEditorState>(QuestionEditorState()) {

    private val messenger = MessengerModel(this)
    val messengerState = messenger.state

    private val questionNow get() = stateNow.question ?: error("missing question")

    fun setParameters(request: EditQuestionRequest) {
        val questionId = request.questionId; val stepId = request.stepId
        if (!questionId.isEmpty()) {
            viewModelScope.launch {
                val question = questionSource.readQuestionById(questionId)
                initializeState(question)
            }
        } else {
            initializeState(Question.forStep(stepId))
        }
    }

    private fun initializeState(question: Question) {
        setState { it.copy(
            question = question,
            minValue = question.minValue?.toString(),
            maxValue = question.maxValue?.toString(),
            isFinished = false,
            generateAudio = true
        ) }
    }

    fun dispatch(action: QuestionEditorAction) {
        when (action) {
            is AcceptQuestionEdit -> acceptEdit()
            is DeleteQuestion -> deleteQuestion()
            is EditQuestion -> editQuestion(action.question)
            is ToggleQuestionAudio -> setState { it.copy(generateAudio = !it.generateAudio) }
            is EditQuestionMinValue -> setState { it.copy(minValue = action.value) }
            is EditQuestionMaxValue -> setState { it.copy(maxValue = action.value) }
        }
    }

    private fun editQuestion(question: Question) {
        setState { it.copy(
            question = question.copy(
                audioUrl = question.audioUrl?.takeIf { question.text == questionNow.text }
            ),
        ) }
    }

    private fun deleteQuestion() {
        viewModelScope.launch {
            questionSource.deleteQuestion(questionNow)
            setState { it.copy(isFinished = true)}
        }
    }

    private fun acceptEdit() {
        if (questionNow.text.isBlank()) {
            messenger.setError("Question content is missing")
            return
        }
        viewModelScope.launch {
            if (stateNow.generateAudio) {
                if (!generateAudio()) {
                    messenger.setError("Unable to generate audio")
                    return@launch
                }
            }

            if (questionNow.id.isBlank()) {
                val id = randomUuidStringId()
                questionSource.createQuestion(questionNow.copy(
                    id = id
                ))
            } else {
                questionSource.updateQuestion(questionNow)
            }
            setState { it.copy(isFinished = true)}
        }
    }

    private suspend fun generateAudio(): Boolean {
        val url = speechService.generateSpeech(questionNow.text) ?: return false
        setState { it.copy(question = questionNow.copy(audioUrl = url))}
        return true
    }
}

data class QuestionEditorState(
    val question: Question? = null,
    val generateAudio: Boolean = false,
    val isFinished: Boolean = false,
    val minValue: String? = null,
    val maxValue: String? = null,
)

data class EditQuestionRequest(
    val questionId: QuestionId,
    val stepId: StepId,
) {
    companion object {
        fun newQuestionRequest(stepId: StepId) = EditQuestionRequest("", stepId)
    }
}

sealed interface QuestionEditorAction
object AcceptQuestionEdit: QuestionEditorAction
object DeleteQuestion: QuestionEditorAction
object ToggleQuestionAudio: QuestionEditorAction
data class EditQuestion(val question: Question): QuestionEditorAction
data class EditQuestionMinValue(val value: String): QuestionEditorAction
data class EditQuestionMaxValue(val value: String): QuestionEditorAction