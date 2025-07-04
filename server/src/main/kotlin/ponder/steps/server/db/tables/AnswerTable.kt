package ponder.steps.server.db.tables

import kabinet.utils.nowToLocalDateTimeUtc
import kabinet.utils.toInstantFromUtc
import kabinet.utils.toLocalDateTimeUtc
import klutch.db.tables.UserTable
import klutch.utils.fromStringId
import klutch.utils.toStringId
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.statements.BatchUpsertStatement
import ponder.steps.model.data.Answer
import ponder.steps.model.data.DataType

object AnswerTable: UUIDTable("answer") {
    val userId = reference("user_id", UserTable.id, onDelete = ReferenceOption.CASCADE)
    val stepLogId = reference("step_log_id", StepLogTable.id, onDelete = ReferenceOption.CASCADE)
    val questionId = reference("question_id", QuestionTable.id, onDelete = ReferenceOption.CASCADE)
    val value = text("value")
    val type = enumeration<DataType>("type")
    val updatedAt = datetime("updated_at")
    val syncAt = datetime("sync_at").default(Clock.nowToLocalDateTimeUtc())
}

fun ResultRow.toAnswer() = Answer(
    id = this[AnswerTable.id].value.toStringId(),
    stepLogId = this[AnswerTable.stepLogId].value.toStringId(),
    questionId = this[AnswerTable.questionId].value.toStringId(),
    value = this[AnswerTable.value],
    type = this[AnswerTable.type],
    updatedAt = this[AnswerTable.updatedAt].toInstantFromUtc()
)

fun syncAnswer(userId: String, syncAt: Instant): BatchUpsertStatement.(Answer) -> Unit = { answer ->
    this[AnswerTable.id] = answer.id.fromStringId()
    this[AnswerTable.userId] = userId.fromStringId()
    this[AnswerTable.stepLogId] = answer.stepLogId.fromStringId()
    this[AnswerTable.questionId] = answer.questionId.fromStringId()
    this[AnswerTable.value] = answer.value
    this[AnswerTable.type] = answer.type
    this[AnswerTable.updatedAt] = answer.updatedAt.toLocalDateTimeUtc()
    this[AnswerTable.syncAt] = syncAt.toLocalDateTimeUtc()
}