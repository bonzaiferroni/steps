package ponder.steps.model.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class FullSync(
    val origin: String,
    val startSyncAt: Instant,
    val endSyncAt: Instant,
    val deletions: Set<String>,
    val steps: List<Step>,
    val pathSteps: List<PathStep>,
    val questions: List<Question>,
    val intents: List<Intent>,
    val treks: List<Trek>,
    val stepLogs: List<StepLog>,
    val answers: List<Answer>,
    val tags: List<Tag>,
    val stepTags: List<StepTag>
) {
    val isEmpty get() = deletions.isEmpty() && steps.isEmpty() && pathSteps.isEmpty() && questions.isEmpty()
            && intents.isEmpty() && treks.isEmpty() && stepLogs.isEmpty() && answers.isEmpty()
            && tags.isEmpty() && stepTags.isEmpty()
}
