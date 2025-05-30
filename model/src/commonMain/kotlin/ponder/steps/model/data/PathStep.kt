package ponder.steps.model.data

import kotlinx.serialization.Serializable

@Serializable
data class PathStep(
    val id: Long,
    val stepId: Long,
    val pathId: Long,
    val position: Int,
)