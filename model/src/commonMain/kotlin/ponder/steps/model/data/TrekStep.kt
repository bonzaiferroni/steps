package ponder.steps.model.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class TrekStep(
    // step data
    val stepId: String,
    val stepLabel: String,
    val pathSize: Int,
    val imgUrl: String?,
    val thumbUrl: String?,
    val description: String?,
    val audioLabelUrl: String?,
    val audioFullUrl: String?,
    // trek data (sometimes null)
    val trekId: String?,
    val progress: Int?,
    val availableAt: Instant?,
    val startedAt: Instant?,
    val finishedAt: Instant?,
    // trek or pathstep data
    val pathStepId: String?,
    // pathStep data (sometimes not joined)
    val position: Int? = null,
    // intent data (sometimes not joined)
    val intentLabel: String? = null,
    val priority: IntentPriority? = null,
    val intentMins: Int? = null,
)