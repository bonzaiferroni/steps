package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kabinet.utils.randomUuidStringId
import kotlinx.coroutines.launch
import ponder.steps.db.QuestionEntity
import ponder.steps.io.LocalQuestionRepository
import ponder.steps.io.QuestionRepository
import ponder.steps.model.data.DataType
import pondui.ui.core.StateModel


class AddQuestionModel(
    private val dismiss: () -> Unit,
    private val questionRepo: QuestionRepository = LocalQuestionRepository(),
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

            // Create the question entity
            val questionEntity = QuestionEntity(
                id = questionId,
                stepId = stepId,
                text = stateNow.questionText,
                type = stateNow.questionType,
                minValue = minValue,
                maxValue = maxValue
            )

            // Insert the question entity into the database using the repository
            questionRepo.createQuestion(questionEntity)

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
) {
    val isValidQuestion: Boolean
        get() = questionText.isNotEmpty() && stepId != null
}
