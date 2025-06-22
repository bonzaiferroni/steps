package ponder.steps.model.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Question(
    val id: QuestionId,
    val stepId: StepId,
    val text: String,
    val type: DataType,
    val minValue: Int? = null,
    val maxValue: Int? = null,
    val audioUrl: String? = null,
    val updatedAt: Instant
)

typealias QuestionId = String