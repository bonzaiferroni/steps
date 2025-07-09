package ponder.steps.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import ponder.steps.model.data.DataType
import ponder.steps.model.data.Question

@Entity(
    foreignKeys = [
        ForeignKey(StepEntity::class, ["id"], ["stepId"], ForeignKey.CASCADE),
    ],
    indices = [Index("stepId")]
)
data class QuestionEntity(
    @PrimaryKey
    val id: String,
    val stepId: String,
    val text: String,
    val type: DataType,
    val minValue: Int?,
    val maxValue: Int?,
    val audioUrl: String? = null,
    val updatedAt: Instant,
)

fun Question.toEntity(isUpdated: Boolean = true) = QuestionEntity(
    id = id,
    stepId = stepId,
    text = text,
    type = type,
    minValue = minValue,
    maxValue = maxValue,
    audioUrl = audioUrl,
    updatedAt = if (isUpdated) Clock.System.now() else updatedAt,
)
