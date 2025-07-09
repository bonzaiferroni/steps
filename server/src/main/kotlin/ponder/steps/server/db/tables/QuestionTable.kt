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
import org.jetbrains.exposed.sql.statements.UpsertStatement
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
    val syncAt = datetime("sync_at").default(Clock.nowToLocalDateTimeUtc())
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
