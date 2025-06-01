package ponder.steps.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class PathStepEntity(
    @PrimaryKey
    val id: String,
    val stepId: String,
    val pathId: String,
    val position: Int,
)