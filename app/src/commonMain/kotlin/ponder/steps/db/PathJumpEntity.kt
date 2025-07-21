package ponder.steps.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import ponder.steps.model.data.PathJumpId
import ponder.steps.model.data.PathStepId
import ponder.steps.model.data.QuestionId

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = PathStepEntity::class,
            parentColumns = ["id"],
            childColumns = ["fromPathStepId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = PathStepEntity::class,
            parentColumns = ["id"],
            childColumns = ["toPathStepId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = QuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index(value = ["fromPathStepId"]),
        Index(value = ["toPathStepId"]),
        Index(value = ["questionId"])
    ],
)
data class PathJumpEntity(
    @PrimaryKey
    val id: PathJumpId,
    val fromPathStepId: PathStepId,
    val toPathStepId: PathStepId,
    val questionId: QuestionId,
    val value: String?,
)