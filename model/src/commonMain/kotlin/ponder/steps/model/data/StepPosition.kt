package ponder.steps.model.data

import kotlinx.serialization.Serializable

@Serializable
data class StepPosition(
    val parentId: Long,
    val stepId: Long,
    val position: Int,
)