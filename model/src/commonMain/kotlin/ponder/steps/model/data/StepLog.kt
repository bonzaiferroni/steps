package ponder.steps.model.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * @param trekId is either the trek that contains the step, or the trek that consists of the step
 * @param stepId is either a step within the trek, or the root step
 */

@Serializable
data class StepLog(
    override val id: StepLogId,
    val trekId: TrekId?,
    val stepId: StepId,
    val pathStepId: PathStepId?,
    val status: StepStatus,
    val createdAt: Instant = Instant.DISTANT_FUTURE,
    override val updatedAt: Instant = Instant.DISTANT_PAST,

    val pathId: StepId? = null
): SyncRecord {
    val isCompleted get() = when(status) {
        StepStatus.Skipped, StepStatus.Completed -> true
        else -> false
    }
}

data class NewStepLog(
    val stepId: StepId,
    val trekId: TrekId?,
    val pathStepId: PathStepId?,
    val outcome: StepStatus,
)

typealias StepLogId = String
