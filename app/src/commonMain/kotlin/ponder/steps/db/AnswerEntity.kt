package ponder.steps.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import ponder.steps.model.data.Answer
import ponder.steps.model.data.DataType

@Entity(
    foreignKeys = [
        ForeignKey(StepLogEntity::class, ["id"], ["logId"], ForeignKey.CASCADE),
        ForeignKey(QuestionEntity::class, ["id"], ["questionId"], ForeignKey.CASCADE),
    ],
    indices = [Index("logId"), Index("questionId")],
    primaryKeys = ["logId", "questionId"]
)
data class AnswerEntity(
    val logId: String,
    val questionId: String,
    val value: String,
    val type: DataType,
)

fun Answer.toEntity() = AnswerEntity(
    logId = logId,
    questionId = questionId,
    value = value,
    type = type,
)