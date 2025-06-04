package ponder.steps.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = StepEntity::class,
            parentColumns = ["id"],
            childColumns = ["pathId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = StepEntity::class,
            parentColumns = ["id"],
            childColumns = ["stepId"],
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
data class PathStepEntity(
    @PrimaryKey
    val id: String,
    val stepId: String,
    val pathId: String,
    val position: Int,
)