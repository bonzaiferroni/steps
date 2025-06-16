package ponder.steps.model.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class TrekItem(
    val trekId: String,
    val stepId: String,
    val stepLabel: String,
    val pathSize: Int,
    val progress: Int,
    val imgUrl: String?,
    val thumbUrl: String?,
    val description: String?,
    val audioLabelUrl: String?,
    val audioFullUrl: String?,
    val intentLabel: String,
    val priority: IntentPriority,
    val expectedMinutes: Int?,
    val availableAt: Instant,
    val startedAt: Instant?,
    val finishedAt: Instant?,
)