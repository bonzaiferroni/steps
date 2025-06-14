package ponder.steps.server.db.tables

import klutch.db.tables.UserTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object DeletionsTable: UUIDTable("deletion") {
    val userId = reference("user_id", UserTable.id, ReferenceOption.CASCADE)
    val recordedAt = datetime("recorded_at")
}