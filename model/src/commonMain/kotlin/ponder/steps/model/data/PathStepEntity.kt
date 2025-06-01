package ponder.steps.model.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity("path_step")
data class PathStepEntity(
    @PrimaryKey
    val id: String,
    val pathId: String,
    val stepId: String,
    val position: Int,
)