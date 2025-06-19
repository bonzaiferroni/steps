package ponder.steps.model.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class StepLog(
    val id: StepLogId = "",
    val stepId: StepId,
    val trekId: TrekId?,
    val pathStepId: PathStepId?,
    val outcome: StepOutcome,
    val updatedAt: Instant = Instant.DISTANT_PAST,
    val createdAt: Instant = Instant.DISTANT_FUTURE,
)

typealias StepLogId = String