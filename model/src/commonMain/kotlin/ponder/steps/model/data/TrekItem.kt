package ponder.steps.model.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class TrekItem(
    val trekId: String,
    val stepLabel: String,
    val stepPathSize: Int,
    val stepIndex: Int,
    val stepCount: Int,
    val intentLabel: String,
    val expectedMinutes: Int?,
    val availableAt: Instant,
    val startedAt: Instant?,
    val finishedAt: Instant?,
)