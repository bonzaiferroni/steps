package ponder.steps.model.data

import kotlinx.serialization.Serializable

@Serializable
data class Step(
    val id: String,
    val parentId: String?,
    val label: String,
    val position: Int?,
    val imgUrl: String?,
    val children: List<Step>?,
)