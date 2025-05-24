package ponder.steps.model.data

import kotlinx.serialization.Serializable

@Serializable
data class NewExample(
    val label: String,
)