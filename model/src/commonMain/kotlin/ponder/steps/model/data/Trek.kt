package ponder.steps.model.data

import kabinet.model.UserId
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Trek(
    override val id: TrekId,
    val userId: UserId,
    val intentId: IntentId,
    val rootId: StepId,
    val isComplete: Boolean,
    val createdAt: Instant,
    val finishedAt: Instant?,
    val expectedAt: Instant?,
    override val updatedAt: Instant,
): SyncRecord

typealias TrekId = String
