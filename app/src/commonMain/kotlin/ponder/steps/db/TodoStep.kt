package ponder.steps.db

import androidx.room.Embedded
import kotlinx.datetime.Instant
import ponder.steps.model.data.Step
import ponder.steps.model.data.TrekId

data class TodoStep(
    val trekPointId: Long,
    @Embedded
    val step: Step,
    val trekId: TrekId? = null,
    val startedAt: Instant? = null,
    val finishedAt: Instant? = null,
    val isComplete: Boolean? = null,
) {
    val vmKey: String get() = step.pathStepId ?: trekPointId.toString()
    val progressKey get() = step.pathStepId ?: trekId
}