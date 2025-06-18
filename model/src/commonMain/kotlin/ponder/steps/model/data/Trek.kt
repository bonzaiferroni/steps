package ponder.steps.model.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Trek(
    val id: String,
    val userId: String,
    val intentId: String,
    val superId: String?,
    val pathStepId: String?,
    val rootId: String,
    val progress: Int,
    val isComplete: Boolean,
    val availableAt: Instant,
    val startedAt: Instant?,
    val progressAt: Instant?,
    val finishedAt: Instant?,
    val expectedAt: Instant?,
)