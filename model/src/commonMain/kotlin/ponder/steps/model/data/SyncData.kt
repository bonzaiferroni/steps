package ponder.steps.model.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class SyncData(
    val startSyncAt: Instant,
    val endSyncAt: Instant,
    val deletions: Set<String>,
    val steps: List<Step>,
    val pathSteps: List<PathStep>,
    val questions: List<Question>,
) {
    val isEmpty get() = deletions.isEmpty() && steps.isEmpty() && pathSteps.isEmpty() && questions.isEmpty()
}
