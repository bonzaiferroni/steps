package ponder.steps.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
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
)

fun Question.toEntity() = QuestionEntity(
    id = id,
    stepId = stepId,
    text = text,
    type = type,
    minValue = minValue,
    maxValue = maxValue,
    audioUrl = audioUrl,
)