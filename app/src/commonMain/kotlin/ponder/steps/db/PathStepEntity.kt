package ponder.steps.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import ponder.steps.model.data.PathStep

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
    ],
    indices = [
        Index(value = ["pathId"]),
        Index(value = ["stepId"]),
    ],
)
data class PathStepEntity(
    @PrimaryKey
    val id: String,
    val stepId: String,
    val pathId: String,
    val position: Int,
)

fun PathStep.toEntity() = PathStepEntity(
    id = id,
    stepId = stepId,
    pathId = pathId,
    position = position,
)