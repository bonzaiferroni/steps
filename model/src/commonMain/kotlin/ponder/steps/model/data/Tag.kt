package ponder.steps.model.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Tag(
    val id: TagId,
    val label: String,
    val updatedAt: Instant
)

typealias TagId = String