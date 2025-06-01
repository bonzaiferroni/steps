package ponder.steps.model.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Focus(
    val trekId: Long,
    val intentLabel: String,
    val stepId: Long,
    val stepLabel: String,
    val stepIndex: Int,
    val stepCount: Int,
    val stepPathSize: Int,
    val imgUrl: String?,
    val startedAt: Instant?,
)