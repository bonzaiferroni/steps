package ponder.steps.model.data

import kotlinx.serialization.Serializable

@Serializable
data class NewStep(
    val parentId: Long?,
    val label: String,
    val position: Int?,
)