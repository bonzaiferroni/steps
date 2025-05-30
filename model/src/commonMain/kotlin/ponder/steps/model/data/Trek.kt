package ponder.steps.model.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Trek(
    val id: Long,
    val userId: Long,
    val intentId: Long,
    val rootId: Long,
    val pathId: Long?,
    val positionId: Long,
    val position: Int,
    val stepCount: Int,
    val availableAt: Instant,
    val startedAt: Instant,
    val progressAt: Instant,
    val finishedAt: Instant?,
    val expectedAt: Instant?,
)