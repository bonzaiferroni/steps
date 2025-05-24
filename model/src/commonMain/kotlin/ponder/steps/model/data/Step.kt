package ponder.steps.model.data

import kotlinx.serialization.Serializable

@Serializable
data class Step(
    val id: Int,
    val parentId: Int?,
    val label: String,
    val position: Int,
)