package ponder.steps.server.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Trek(
    val id: Long,
    val userId: Long,
    val intentId: Long,
    val rootId: Long,
    val stepId: Long,
    val stepIndex: Int,
    val stepCount: Int,
    val pathIds: List<Long>,
    val breadCrumbs: List<Long>,
    val availableAt: Instant,
    val startedAt: Instant?,
    val progressAt: Instant?,
    val finishedAt: Instant?,
    val expectedAt: Instant?,
)