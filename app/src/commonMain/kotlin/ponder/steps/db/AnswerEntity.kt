package ponder.steps.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ponder.steps.model.data.Answer
import ponder.steps.model.data.AnswerId
import ponder.steps.model.data.DataType
import ponder.steps.model.data.QuestionId
import ponder.steps.model.data.StepLogId

@Entity(
    foreignKeys = [
        ForeignKey(StepLogEntity::class, ["id"], ["stepLogId"], ForeignKey.CASCADE),
        ForeignKey(QuestionEntity::class, ["id"], ["questionId"], ForeignKey.CASCADE),
    ],
    indices = [Index("stepLogId"), Index("questionId")],
)
data class AnswerEntity(
    @PrimaryKey
    val id: AnswerId,
    val stepLogId: StepLogId,
    val questionId: QuestionId,
    val value: String?,
    val type: DataType,
    val updatedAt: Instant,
)

fun Answer.toEntity(isUpdated: Boolean = true) = AnswerEntity(
    id = id,
    stepLogId = stepLogId,
    questionId = questionId,
    value = value,
    type = type,
    updatedAt = if (isUpdated) Clock.System.now() else updatedAt,
)
