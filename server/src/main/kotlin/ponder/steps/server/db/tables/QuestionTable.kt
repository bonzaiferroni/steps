package ponder.steps.server.db.tables

import kabinet.utils.toInstantFromUtc
import kabinet.utils.toLocalDateTimeUtc
import klutch.db.tables.UserTable
import klutch.utils.fromStringId
import klutch.utils.toStringId
import kotlinx.datetime.Instant
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.statements.BatchUpsertStatement
import ponder.steps.model.data.DataType
import ponder.steps.model.data.Question

object QuestionTable: UUIDTable("question") {
    val userId = reference("user_id", UserTable.id, ReferenceOption.CASCADE)
    val stepId = reference("step_id", StepTable.id, ReferenceOption.CASCADE)
    val text = text("text")
    val type = enumeration("type", DataType::class)
    val minValue = integer("min_value").nullable()
    val maxValue = integer("max_value").nullable()
    val audioUrl = text("audio_url").nullable()
    val updatedAt = datetime("updated_at")
    val syncAt = datetime("sync_at").nullable()
}

fun ResultRow.toQuestion() = Question(
    id = this[QuestionTable.id].value.toStringId(),
    stepId = this[QuestionTable.stepId].value.toStringId(),
    text = this[QuestionTable.text],
    type = this[QuestionTable.type],
    minValue = this[QuestionTable.minValue],
    maxValue = this[QuestionTable.maxValue],
    audioUrl = this[QuestionTable.audioUrl],
    updatedAt = this[QuestionTable.updatedAt].toInstantFromUtc()
)

fun syncQuestion(userId: String, syncAt: Instant): BatchUpsertStatement.(Question) -> Unit = { question ->
    this[QuestionTable.id] = question.id.fromStringId()
    this[QuestionTable.userId] = userId.fromStringId()
    this[QuestionTable.stepId] = question.stepId.fromStringId()
    this[QuestionTable.text] = question.text
    this[QuestionTable.type] = question.type
    this[QuestionTable.minValue] = question.minValue
    this[QuestionTable.maxValue] = question.maxValue
    this[QuestionTable.audioUrl] = question.audioUrl
    this[QuestionTable.updatedAt] = question.updatedAt.toLocalDateTimeUtc()
    this[QuestionTable.syncAt] = syncAt.toLocalDateTimeUtc()
}

