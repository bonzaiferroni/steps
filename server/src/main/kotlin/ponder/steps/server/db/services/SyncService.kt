package ponder.steps.server.db.services

import kabinet.utils.toInstantFromUtc
import kabinet.utils.toLocalDateTimeUtc
import klutch.db.DbService
import klutch.db.read
import klutch.db.readColumn
import klutch.utils.eq
import klutch.utils.fromStringId
import klutch.utils.greater
import klutch.utils.less
import klutch.utils.lessEq
import klutch.utils.toStringId
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchUpsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.statements.BatchUpsertStatement
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.upsert
import ponder.steps.model.data.SyncData
import ponder.steps.server.db.tables.DeletionsTable
import ponder.steps.server.db.tables.PathStepTable
import ponder.steps.server.db.tables.StepTable
import ponder.steps.server.db.tables.upsertStep
import ponder.steps.server.db.tables.toPathStep
import ponder.steps.server.db.tables.toStep
import ponder.steps.server.db.tables.upsertPathStep
import java.util.UUID

class SyncService : DbService() {

    suspend fun readSync(startSyncAt: Instant, endSyncAt: Instant, userId: String) = dbQuery {

        fun syncTable(userIdColumn: Column<EntityID<UUID>>, updatedAtColumn: Column<LocalDateTime>) =
            userIdColumn.eq(userId) and updatedAtColumn.greater(startSyncAt) and updatedAtColumn.lessEq(endSyncAt)

        val steps = StepTable.read { syncTable(it.userId, it.updatedAt) }.map { it.toStep() }
        val pathSteps = PathStepTable.read { syncTable(it.userId, it.updatedAt) }.map { it.toPathStep() }
        val deletions = DeletionsTable.readColumn(DeletionsTable.id) { syncTable(it.userId, it.recordedAt) }
                .map { it.value.toStringId() }.toSet()
        SyncData(startSyncAt, endSyncAt, deletions, steps, pathSteps)
    }

    suspend fun writeSync(data: SyncData, userId: String) = dbQuery {

        // handle updates
        StepTable.batchUpsert(
            data = data.steps,
            where = { StepTable.userId.eq(userId) and StepTable.updatedAt.lessEq(data.endSyncAt) },
            body = upsertStep(userId)
        )

        PathStepTable.batchUpsert(
            data = data.pathSteps,
            where = { PathStepTable.userId.eq(userId) and PathStepTable.updatedAt.lessEq(data.endSyncAt) },
            body = upsertPathStep(userId)
        )

        // handle deletions
        val deletionIds = data.deletions.map { it.fromStringId() }
        StepTable.deleteWhere { this.id.inList(deletionIds) and this.userId.eq(userId) }
        PathStepTable.deleteWhere { this.id.inList(deletionIds) and this.userId.eq(userId) }

        for (id in deletionIds) {
            DeletionsTable.update(
                where = { DeletionsTable.id.eq(id) and DeletionsTable.userId.eq(userId) }
            ) {
                it[DeletionsTable.recordedAt] = data.endSyncAt.toLocalDateTimeUtc()
            }
        }

        // proto garbage collection, needs to use device id
        DeletionsTable.deleteWhere { this.userId.eq(userId) and this.recordedAt.lessEq(data.endSyncAt) }

        true
    }
}