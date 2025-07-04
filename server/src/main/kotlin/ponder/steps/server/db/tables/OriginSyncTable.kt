package ponder.steps.server.db.tables

import klutch.db.tables.UserTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object OriginSyncTable: LongIdTable("origin_sync") {
    val userId = reference("user_id", UserTable.id, onDelete = ReferenceOption.CASCADE)
    val label = text("label")
    val syncAt = datetime("sync_at")

    init {
        uniqueIndex(userId, label)
    }
}