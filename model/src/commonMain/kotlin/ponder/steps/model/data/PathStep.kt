package ponder.steps.model.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class PathStep(
    val id: PathStepId,
    val stepId: StepId,
    val pathId: StepId,
    val position: Int,
    val updatedAt: Instant,
)

typealias PathStepId = String