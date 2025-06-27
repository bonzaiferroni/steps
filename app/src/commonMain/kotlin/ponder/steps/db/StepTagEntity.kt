package ponder.steps.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import ponder.steps.model.data.StepId
import ponder.steps.model.data.StepTag
import ponder.steps.model.data.StepTagId
import ponder.steps.model.data.TagId

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = StepEntity::class,
            parentColumns = ["id"],
            childColumns = ["stepId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index(value = ["stepId"]),
        Index(value = ["tagId"]),
    ],
)
data class StepTagEntity(
    @PrimaryKey
    val id: StepTagId,
    val stepId: StepId,
    val tagId: TagId,
    val updatedAt: Instant,
)

fun StepTag.toEntity() = StepTagEntity(
    id = id,
    stepId = stepId,
    tagId = tagId,
    updatedAt = updatedAt,
)