package ponder.steps.model.data

import kotlinx.serialization.Serializable

@Serializable
data class NewStep(
    val parentId: String?,
    val label: String,
    val position: Int?,
)