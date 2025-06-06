package ponder.steps.model.data

import kotlinx.serialization.Serializable

@Serializable
data class ImageUrls(
    val url: String,
    val thumbUrl: String,
)