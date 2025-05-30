package ponder.steps.model.data

import kotlinx.serialization.Serializable

@Serializable
data class Step(
    val id: Long,
    val userId: Long,
    val parentId: Long?,
    val label: String,
    val description: String?,
    val position: Int?,
    val imgUrl: String?,
    val thumbUrl: String?,
    val audioUrl: String?,
    val isPublic: Boolean,
    val children: List<Step>?,
)