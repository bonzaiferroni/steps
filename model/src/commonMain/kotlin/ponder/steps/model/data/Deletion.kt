package ponder.steps.model.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Deletion(
    override val id: String,
    val entity: String,
    val deletedAt: Instant,
): SyncRecord {
    val recordId get() = id
    override val updatedAt get() = deletedAt
}

typealias DeletionId = String
