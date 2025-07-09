package ponder.steps.model.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class PathStep(
    override val id: PathStepId,
    val stepId: StepId,
    val pathId: StepId,
    val position: Int,
    override val updatedAt: Instant,
): SyncRecord

typealias PathStepId = String
