package ponder.steps.model.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class TrekItem(
    val trekId: String,
    val stepId: String,
    val stepLabel: String,
    val stepPathSize: Int,
    val stepIndex: Int,
    val stepCount: Int,
    val stepImgUrl: String?,
    val stepThumbUrl: String?,
    val stepDescription: String?,
    val stepAudioLabelUrl: String?,
    val stepAudioFullUrl: String?,
    val intentLabel: String,
    val intentPriority: IntentPriority,
    val expectedMinutes: Int?,
    val availableAt: Instant,
    val startedAt: Instant?,
    val finishedAt: Instant?,
)