package ponder.steps.model.data

import kotlinx.serialization.Serializable

@Serializable
data class NewStep(
    val parentId: Int,
    val label: String,
    val position: Int,
)