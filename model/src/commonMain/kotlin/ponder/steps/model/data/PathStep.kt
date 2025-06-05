package ponder.steps.model.data

import kotlinx.serialization.Serializable

@Serializable
data class PathStep(
    val id: String,
    val stepId: String,
    val pathId: String,
    val position: Int,
)