package ponder.steps.model.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ReadSyncRequest(
    val startSyncAt: Instant,
    val endSyncAt: Instant,
)