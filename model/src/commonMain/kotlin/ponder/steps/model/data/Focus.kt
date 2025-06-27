package ponder.steps.model.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Focus(
    val trekId: String,
    val intentLabel: String,
    // val stepId: String,
    val stepLabel: String,
    val stepIndex: Int,
    val stepPathSize: Int,
    val imgUrl: String?,
    val startedAt: Instant?,
)
