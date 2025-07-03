package ponder.steps.db

import androidx.room.Embedded
import kotlinx.datetime.Instant
import ponder.steps.model.data.PathStepId
import ponder.steps.model.data.Step
import ponder.steps.model.data.TrekId

data class TodoStep(
    val trekId: TrekId,
    val startedAt: Instant? = null,
    @Embedded
    val step: Step
) {
    val key: String get() = step.pathStepId ?: trekId
    val sortValue: Long get() = step.position?.toLong() ?: startedAt?.epochSeconds ?: 0
}