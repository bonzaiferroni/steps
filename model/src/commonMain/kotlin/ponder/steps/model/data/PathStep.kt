package ponder.steps.model.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
data class PathStep(
    @PrimaryKey
    val id: String,
    val stepId: String,
    val pathId: String,
    val position: Int,
) {
    constructor() : this(
        id = "",
        stepId = "",
        pathId = "",
        position = 0
    )
}