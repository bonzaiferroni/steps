package ponder.steps.model.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class StepTag(
    val id: StepTagId,
    val stepId: StepId,
    val tagId: TagId,
    val updatedAt: Instant,
)

typealias StepTagId = String