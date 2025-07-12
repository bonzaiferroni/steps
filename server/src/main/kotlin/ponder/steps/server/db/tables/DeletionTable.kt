package ponder.steps.server.db.tables

import kabinet.model.UserId
import kabinet.utils.toInstantFromUtc
import kabinet.utils.toLocalDateTimeUtc
import klutch.db.tables.UserTable
import klutch.utils.fromStringId
import klutch.utils.toStringId
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.statements.InsertStatement
import ponder.steps.model.data.Deletion

object DeletionTable: UUIDTable("deletion") {
    val userId = reference("user_id", UserTable.id, ReferenceOption.CASCADE)
    val entity = text("entity_name")
    val deletedAt = datetime("deleted_at")
}

fun ResultRow.toDeletion() = Deletion(
    id = this[DeletionTable.id].value.toStringId(),
    entity = this[DeletionTable.entity],
    deletedAt = this[DeletionTable.deletedAt].toInstantFromUtc()
)
