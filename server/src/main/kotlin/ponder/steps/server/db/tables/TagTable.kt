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
import ponder.steps.model.data.PathStep
import ponder.steps.model.data.Tag

object TagTable: UUIDTable("tag") {
    val userId = reference("user_id", UserTable.id, onDelete = ReferenceOption.CASCADE)
    val label = text("label")
    val updatedAt = datetime("updated_at")
}

fun ResultRow.toTag() = Tag(
    id = this[TagTable.id].value.toStringId(),
    label = this[TagTable.label],
    updatedAt = this[TagTable.updatedAt].toInstantFromUtc()
)

fun upsertTag(userId: String): BatchUpsertStatement.(Tag) -> Unit = { tag ->
    this[TagTable.id] = tag.id.fromStringId()
    this[TagTable.userId] = userId.fromStringId()
    this[TagTable.label] = tag.label
    this[TagTable.updatedAt] = tag.updatedAt.toLocalDateTimeUtc()
}