package ponder.steps.model.data

import kabinet.model.UserId
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Trek(
    val id: TrekId,
    val userId: UserId,
    val intentId: IntentId,
    val superId: TrekId?,
    val pathStepId: PathStepId?,
    val rootId: StepId,
    val progress: Int,
    val isComplete: Boolean,
    val availableAt: Instant,
    val startedAt: Instant?,
    val progressAt: Instant?,
    val finishedAt: Instant?,
    val expectedAt: Instant?,
)

typealias TrekId = String