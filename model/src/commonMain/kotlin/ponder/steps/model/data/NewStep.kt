package ponder.steps.model.data

import kotlinx.serialization.Serializable

@Serializable
data class NewStep(
    val pathId: String?,
    val label: String,
    val position: Int?,
)