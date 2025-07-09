package ponder.steps.model.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class StepTag(
    override val id: StepTagId,
    val stepId: StepId,
    val tagId: TagId,
    override val updatedAt: Instant,
): SyncRecord

typealias StepTagId = String
