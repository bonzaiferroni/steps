package ponder.steps.model.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Answer(
    override val id: AnswerId,
    val stepLogId: StepLogId,
    val questionId: QuestionId,
    val value: String,
    val type: DataType,
    override val updatedAt: Instant,
): SyncRecord

data class NewAnswer(
    val stepLogId: StepLogId,
    val questionId: QuestionId,
    val value: String,
    val type: DataType,
)

typealias AnswerId = String
