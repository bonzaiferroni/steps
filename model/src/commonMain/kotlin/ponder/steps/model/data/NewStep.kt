package ponder.steps.model.data

import kotlinx.serialization.Serializable

@Serializable
data class NewStep(
    val label: String,
    val pathId: String? = null,
    val position: Int? = null,
    val description: String? = null,
    val theme: String? = null,
)