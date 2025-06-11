package ponder.steps.model.data

import kotlinx.serialization.Serializable

@Serializable
data class Question(
    val id: String,
    val stepId: String,
    val text: String,
    val type: DataType,
    val minValue: Int? = null,
    val maxValue: Int? = null,
)