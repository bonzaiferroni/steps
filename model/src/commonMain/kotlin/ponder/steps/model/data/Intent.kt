package ponder.steps.model.data

import kabinet.model.UserId
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Intent(
    val id: IntentId,
    val userId: UserId,
    val rootId: StepId,
    val label: String,
    val repeatMins: Int?,
    val expectedMins: Int?,
    val priority: IntentPriority,
    val timing: IntentTiming,
    // val isRegularTime: Boolean,
    val pathIds: List<String>,
    val completedAt: Instant?,
    val scheduledAt: Instant?,
    val updatedAt: Instant,
)

typealias IntentId = String

@Serializable
data class NewIntent(
    val rootId: String,
    val label: String,
    val repeatMins: Int? = null,
    val expectedMins: Int? = null,
    val priority: IntentPriority = IntentPriority.Default,
    val timing: IntentTiming = IntentTiming.Once,
    val scheduledAt: Instant? = null,
    val pathIds: List<String> = emptyList()
)

enum class IntentTiming(val label: String) {
    Schedule("Schedule"),
    Once("One time"),
    Repeat("Repeat");

    override fun toString() = label
}