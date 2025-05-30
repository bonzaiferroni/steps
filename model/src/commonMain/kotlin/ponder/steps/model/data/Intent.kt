package ponder.steps.model.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Intent(
    val id: Long,
    val userId: Long,
    val rootId: Long,
    val label: String,
    val repeatMins: Int?,
    val expectedMins: Int?,
    // val isRegularTime: Boolean,
    val completedAt: Instant?,
    val scheduledAt: Instant?,
)