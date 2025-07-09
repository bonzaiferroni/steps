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
import org.jetbrains.exposed.sql.statements.UpsertStatement
import ponder.steps.model.data.Tag

object TagTable: UUIDTable("tag") {
    val userId = reference("user_id", UserTable.id, onDelete = ReferenceOption.CASCADE)
    val label = text("label")
    val updatedAt = datetime("updated_at")
    val syncAt = datetime("sync_at").default(Clock.nowToLocalDateTimeUtc())
}

fun ResultRow.toTag() = Tag(
    id = this[TagTable.id].value.toStringId(),
    label = this[TagTable.label],
    updatedAt = this[TagTable.updatedAt].toInstantFromUtc()
)
