package ponder.steps.model.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Intent(
    val id: String,
    val userId: String,
    val rootId: String,
    val label: String,
    val repeatMins: Int?,
    val expectedMins: Int?,
    // val isRegularTime: Boolean,
    val completedAt: Instant?,
    val scheduledAt: Instant?,
)

@Serializable
data class NewIntent(
    val rootId: String,
    val label: String,
    val repeatMins: Int? = null,
    val expectedMins: Int? = null,
    val scheduledAt: Instant? = null,
)
