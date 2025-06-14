package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kabinet.utils.randomUuidStringId
import kotlinx.coroutines.launch
import ponder.steps.io.AiClient
import ponder.steps.io.LocalQuestionRepository
import ponder.steps.io.QuestionRepository
import ponder.steps.model.data.DataType
import ponder.steps.model.data.Question
import ponder.steps.model.data.SpeechRequest
import ponder.steps.model.data.SpeechVoice
import pondui.LocalValueRepository
import pondui.ValueRepository
import pondui.ui.core.StateModel


class AddQuestionModel(
    private val dismiss: () -> Unit,
    private val questionRepo: QuestionRepository = LocalQuestionRepository(),
    private val aiClient: AiClient = AiClient(),
    private val valueRepo: ValueRepository = LocalValueRepository(),
) : StateModel<AddQuestionState>(AddQuestionState()) {

    init {
        setQuestionText("")
    }

    fun setQuestionText(value: String) {
        setState { it.copy(questionText = value) }
    }

    fun setQuestionType(value: DataType) {
        setState { it.copy(questionType = value) }
    }

    fun setMinValue(value: String) {
        val minValue = value.ifEmpty { null }
        setState { it.copy(minValue = minValue) }
    }

    fun setMaxValue(value: String) {
        val maxValue = value.ifEmpty { null }
        setState { it.copy(maxValue = maxValue) }
    }

    fun createQuestion() {
        if (!stateNow.isValidQuestion) return

        // Update status to indicate we're starting
        setState { it.copy(status = "Creating question...") }

        viewModelScope.launch {
            val questionId = randomUuidStringId()
            val stepId = stateNow.stepId ?: return@launch

            // Convert min and max values to integers if applicable
            val minValue = if (stateNow.questionType == DataType.Integer || stateNow.questionType == DataType.Float) {
                stateNow.minValue?.toIntOrNull()
            } else null

            val maxValue = if (stateNow.questionType == DataType.Integer || stateNow.questionType == DataType.Float) {
                stateNow.maxValue?.toIntOrNull()
            } else null

            // Update status to indicate we're generating audio
            setState { it.copy(status = "Generating audio...") }

            val audioUrl = try {
                // Create speech request
                val request = SpeechRequest(
                    text = stateNow.questionText,
                    theme = valueRepo.readString(SETTINGS_DEFAULT_AUDIO_THEME),
                    voice = valueRepo.readInt(SETTINGS_DEFAULT_VOICE).let { SpeechVoice.entries[it] }
                )

                // Generate speech
                val audioUrl = aiClient.generateSpeech(request)

                // Update status to indicate audio generation is complete
                setState { it.copy(status = "Audio generated successfully") }
                audioUrl
            } catch (e: Exception) {
                // Update status to indicate audio generation failed
                setState { it.copy(status = "Audio generation failed: ${e.message}") }
                null
            }

            // Create the question entity
            val question = Question(
                id = questionId,
                stepId = stepId,
                text = stateNow.questionText,
                type = stateNow.questionType,
                minValue = minValue,
                maxValue = maxValue,
                audioUrl = audioUrl
            )

            // Insert the question entity into the database using the repository
            questionRepo.createQuestion(question)

            // Reset state and dismiss the dialog
            resetState()
            dismiss()
        }
    }

    private fun resetState() {
        setState {
            it.copy(
                questionText = "",
                minValue = null,
                maxValue = null
            )
        }
    }

    fun setStepId(stepId: String?) {
        setState { it.copy(stepId = stepId) }
    }
}

data class AddQuestionState(
    val stepId: String? = null,
    val questionText: String = "",
    val questionType: DataType = DataType.String,
    val minValue: String? = null,
    val maxValue: String? = null,
    val status: String = "",
) {
    val isValidQuestion: Boolean
        get() = questionText.isNotEmpty() && stepId != null
}

