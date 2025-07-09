package ponder.steps.model.data

import kabinet.model.UserId
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Intent(
    override val id: IntentId,
    val userId: UserId,
    val rootId: StepId,
    val label: String,
    val repeatMins: Int?,
    val expectedMins: Int?,
    val priority: IntentPriority,
    // val isRegularTime: Boolean,
    val pathIds: List<String>,
    val completedAt: Instant?,
    val scheduledAt: Instant?,
    override val updatedAt: Instant,
): SyncRecord

typealias IntentId = String

@Serializable
data class NewIntent(
    val rootId: String,
    val label: String,
    val repeatMins: Int? = null,
    val expectedMins: Int? = null,
    val priority: IntentPriority = IntentPriority.Default,
    val scheduledAt: Instant? = null,
    val pathIds: List<String> = emptyList()
)
