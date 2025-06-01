package ponder.steps.model.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("step")
data class StepEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val label: String,
    val description: String?,
    val expectedMins: Int?,
    val imgUrl: String?,
    val thumbUrl: String?,
    val audioUrl: String?,
    val isPublic: Boolean,
    val pathSize: Int,
)