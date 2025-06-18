package ponder.steps.model.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class StepLog(
    val id: String = "",
    val stepId: String,
    val trekId: String?,
    val pathStepId: String?,
    val outcome: StepOutcome,
    val updatedAt: Instant = Instant.DISTANT_PAST,
    val createdAt: Instant = Instant.DISTANT_FUTURE,
)