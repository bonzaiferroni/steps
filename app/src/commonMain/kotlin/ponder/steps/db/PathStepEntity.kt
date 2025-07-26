package ponder.steps.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ponder.steps.model.data.PathStep

@Entity(
    foreignKeys = [
        ForeignKey(StepEntity::class, ["id"], ["pathId"], ForeignKey.CASCADE),
        ForeignKey(StepEntity::class, ["id"], ["stepId"], ForeignKey.CASCADE)
    ],
    indices = [
        Index(value = ["pathId"]),
        Index(value = ["stepId"]),
        // Index(value = ["pathId", "position"], unique = true), won't work
    ],
)
data class PathStepEntity(
    @PrimaryKey
    val id: String,
    val stepId: String,
    val pathId: String,
    val position: Int,
    val updatedAt: Instant = Clock.System.now(),
)

fun PathStep.toEntity(isUpdated: Boolean = true) = PathStepEntity(
    id = id,
    stepId = stepId,
    pathId = pathId,
    position = position,
    updatedAt = if (isUpdated) Clock.System.now() else updatedAt,
)
