package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ponder.steps.appDb
import ponder.steps.db.QuestionDao
import ponder.steps.io.QuestionSource
import ponder.steps.model.data.Question
import ponder.steps.model.data.QuestionId
import pondui.ui.core.StateModel

class QuestionEditorModel(
    questionId: QuestionId,
    val questionSource: QuestionSource = QuestionSource(),
    val speechService: SpeechService = SpeechService(),
): StateModel<QuestionEditorState>(QuestionEditorState()) {

    private val messenger = MessengerModel(this)
    val messengerState = messenger.state

    init {
        viewModelScope.launch {
            val question = questionSource.readQuestionById(questionId)
            setState { it.copy(question = question) }
        }
    }

    fun editQuestion(question: Question) {
        setState { it.copy(question = question) }
    }

    fun deleteQuestion() {
        val question = stateNow.question ?: error("missing question")
        viewModelScope.launch {
            questionSource.deleteQuestion(question)
            setState { it.copy(isFinished = true)}
        }
    }

    fun acceptEdit() {
        val question = stateNow.question ?: error("missing question")
        if (question.text.isBlank()) {
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

            questionSource.updateQuestion(question)
            setState { it.copy(isFinished = true)}
        }
    }

    private suspend fun generateAudio(): Boolean {
        val question = stateNow.question ?: error("missing question")
        val url = speechService.generateSpeech(question.text) ?: return false
        setState { it.copy(question = question.copy(audioUrl = url))}
        return true
    }
}

data class QuestionEditorState(
    val question: Question? = null,
    val generateAudio: Boolean = false,
    val isFinished: Boolean = false,
)