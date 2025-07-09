package ponder.steps.model.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Tag(
    override val id: TagId,
    val label: String,
    override val updatedAt: Instant
): SyncRecord

typealias TagId = String
