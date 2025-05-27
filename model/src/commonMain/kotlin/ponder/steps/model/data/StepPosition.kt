package ponder.steps.model.data

import kotlinx.serialization.Serializable

@Serializable
data class StepPosition(
    val parentId: String,
    val stepId: String,
    val position: Int,
)