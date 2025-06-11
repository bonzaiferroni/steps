package ponder.steps.model.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class LogEntry(
    val id: String,
    val stepId: String,
    val trekId: String,
    val outcome: StepOutcome,
    val updatedAt: Instant,
    val createdAt: Instant,
)