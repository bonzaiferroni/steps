package ponder.steps.db

import androidx.room.Embedded
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ponder.steps.model.data.Step
import ponder.steps.model.data.TrekId

data class TodoStep(
    val trekId: TrekId,
    @Embedded
    val step: Step,
    val startedAt: Instant? = null,
    val finishedAt: Instant? = null,
    val isComplete: Boolean? = null,
) {
    val key: String get() = step.pathStepId ?: trekId
}