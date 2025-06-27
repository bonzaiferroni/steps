package ponder.steps.server.db.tables

import kabinet.utils.toInstantFromUtc
import kabinet.utils.toLocalDateTimeUtc
import klutch.db.tables.UserTable
import klutch.utils.fromStringId
import klutch.utils.toStringId
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.statements.BatchUpsertStatement
import ponder.steps.model.data.StepTag

object StepTagTable: UUIDTable("step_tag") {
    val userId = reference("user_id", UserTable.id, onDelete = ReferenceOption.CASCADE)
    val stepId = reference("step_id", StepTable.id, onDelete = ReferenceOption.CASCADE)
    val tagId = reference("tag_id", TagTable.id, onDelete = ReferenceOption.CASCADE)
    val updatedAt = datetime("updated_at")
}

fun ResultRow.toStepTag() = StepTag(
    id = this[StepTagTable.id].value.toStringId(),
    stepId = this[StepTagTable.stepId].value.toStringId(),
    tagId = this[StepTagTable.tagId].value.toStringId(),
    updatedAt = this[StepTagTable.updatedAt].toInstantFromUtc()
)

fun upsertStepTag(userId: String): BatchUpsertStatement.(StepTag) -> Unit = { stepTag ->
    this[StepTagTable.id] = stepTag.id.fromStringId()
    this[StepTagTable.userId] = userId.fromStringId()
    this[StepTagTable.stepId] = stepTag.stepId.fromStringId()
    this[StepTagTable.tagId] = stepTag.tagId.fromStringId()
    this[StepTagTable.updatedAt] = stepTag.updatedAt.toLocalDateTimeUtc()
}