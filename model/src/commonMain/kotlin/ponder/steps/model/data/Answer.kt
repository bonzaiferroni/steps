package ponder.steps.model.data

import kotlinx.serialization.Serializable

@Serializable
data class Answer(
    val logId: String,
    val questionId: String,
    val value: String,
    val type: DataType,
)
