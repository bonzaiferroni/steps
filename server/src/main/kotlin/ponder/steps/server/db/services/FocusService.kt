package ponder.steps.server.db.services

import klutch.db.DbService
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import ponder.steps.model.data.Focus
import ponder.steps.server.db.tables.FocusAspect
import ponder.steps.server.db.tables.TrekTable

class FocusService: DbService() {

    suspend fun readFocus(userId: Long) = dbQuery {
        // Get the active trek for the user
        val activeTrek = TrekTable.select(TrekTable.id)
            .where { TrekTable.userId.eq(userId) and TrekTable.finishedAt.isNull() }
            .firstOrNull()
            ?.let { it[TrekTable.id].value }
            ?: return@dbQuery null

        // Return the focus for the active trek
        FocusAspect.readFirst { it.trekId.eq(activeTrek) }
            ?: throw IllegalArgumentException("Focus not found for trek: $activeTrek")
    }
}
