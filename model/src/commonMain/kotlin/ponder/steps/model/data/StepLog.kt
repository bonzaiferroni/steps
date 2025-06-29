package ponder.steps.model.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * @param trekId is either the trek that contains the step, or the trek that consists of the step
 * @param stepId is either a step within the trek, or the root step
 */

@Serializable
data class StepLog(
    val id: StepLogId,
    val stepId: StepId,
    val trekId: TrekId?,
    val pathStepId: PathStepId?,
    val outcome: StepOutcome,
    val createdAt: Instant = Instant.DISTANT_FUTURE,
    val updatedAt: Instant = Instant.DISTANT_PAST,
)

data class NewStepLog(
    val stepId: StepId,
    val trekId: TrekId?,
    val pathStepId: PathStepId?,
    val outcome: StepOutcome,
)

typealias StepLogId = String