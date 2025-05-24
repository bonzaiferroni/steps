package ponder.contemplate.model.data

import kotlinx.serialization.Serializable

@Serializable
data class Example(
    val id: Long,
    val userId: Long,
    val label: String,
)