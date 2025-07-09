package ponder.steps.server.db.services

import kabinet.model.UserId
import kabinet.utils.nameOrError
import kabinet.utils.toLocalDateTimeUtc
import klutch.db.DbService
import klutch.db.read
import klutch.utils.eq
import klutch.utils.fromStringId
import klutch.utils.greater
import klutch.utils.less
import klutch.utils.lessEq
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchUpsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.upsert
import ponder.steps.model.data.Answer
import ponder.steps.model.data.Deletion
import ponder.steps.model.data.FullSync
import ponder.steps.model.data.Intent
import ponder.steps.model.data.PathStep
import ponder.steps.model.data.Question
import ponder.steps.model.data.SYNC_SERVER_ORIGIN_LABEL
import ponder.steps.model.data.Step
import ponder.steps.model.data.StepLog
import ponder.steps.model.data.StepTag
import ponder.steps.model.data.SyncPacket
import ponder.steps.model.data.SyncRecord
import ponder.steps.model.data.Tag
import ponder.steps.model.data.Trek
import ponder.steps.server.db.tables.AnswerTable
import ponder.steps.server.db.tables.DeletionTable
import ponder.steps.server.db.tables.IntentTable
import ponder.steps.server.db.tables.OriginSyncTable
import ponder.steps.server.db.tables.PathStepTable
import ponder.steps.server.db.tables.QuestionTable
import ponder.steps.server.db.tables.StepLogTable
import ponder.steps.server.db.tables.StepTable
import ponder.steps.server.db.tables.StepTagTable
import ponder.steps.server.db.tables.TagTable
import ponder.steps.server.db.tables.TrekTable
import ponder.steps.server.db.tables.integrateAnswer
import ponder.steps.server.db.tables.integrateDeletion
import ponder.steps.server.db.tables.integrateIntent
import ponder.steps.server.db.tables.integratePathStep
import ponder.steps.server.db.tables.integrateQuestion
import ponder.steps.server.db.tables.integrateStep
import ponder.steps.server.db.tables.integrateStepLog
import ponder.steps.server.db.tables.integrateStepTag
import ponder.steps.server.db.tables.integrateTag
import ponder.steps.server.db.tables.integrateTrek
import ponder.steps.server.db.tables.toAnswer
import ponder.steps.server.db.tables.toIntent
import ponder.steps.server.db.tables.toPathStep
import ponder.steps.server.db.tables.toQuestion
import ponder.steps.server.db.tables.toStep
import ponder.steps.server.db.tables.toStepLog
import ponder.steps.server.db.tables.toStepTag
import ponder.steps.server.db.tables.toTag
import ponder.steps.server.db.tables.toTrek
import ponder.steps.server.db.tables.toDeletion
import java.util.UUID

class SyncApiService : DbService() {

    suspend fun readSync(lastSyncAt: Instant, userId: UserId) = dbQuery {

        fun readSyncData(userIdColumn: Column<EntityID<UUID>>, syncColumn: Column<LocalDateTime>) =
            userIdColumn.eq(userId) and syncColumn.greater(lastSyncAt)

        val records = mutableListOf<SyncRecord>()
        StepTable.read { readSyncData(it.userId, it.syncAt) }.forEach { records.add(it.toStep()) }
        PathStepTable.read { readSyncData(it.userId, it.syncAt) }.forEach { records.add(it.toPathStep()) }
        QuestionTable.read { readSyncData(it.userId, it.syncAt) }.forEach { records.add(it.toQuestion()) }
        IntentTable.read { readSyncData(it.userId, it.syncAt) }.forEach { records.add(it.toIntent()) }
        TrekTable.read { readSyncData(it.userId, it.syncAt) }.forEach { records.add(it.toTrek()) }
        StepLogTable.read { readSyncData(it.userId, it.syncAt) }.forEach { records.add(it.toStepLog()) }
        AnswerTable.read { readSyncData(it.userId, it.syncAt) }.forEach { records.add(it.toAnswer()) }
        TagTable.read { readSyncData(it.userId, it.syncAt) }.forEach { records.add(it.toTag()) }
        StepTagTable.read { readSyncData(it.userId, it.syncAt) }.forEach { records.add(it.toStepTag()) }
        DeletionTable.read { readSyncData(it.userId, it.deletedAt) }.forEach { records.add(it.toDeletion()) }

        SyncPacket(SYNC_SERVER_ORIGIN_LABEL, Clock.System.now(), records)
    }

    suspend fun writeSync(packet: SyncPacket, userId: UserId) = dbQuery {

        // integrate records
        for (record in packet.records) {
            try {
                when (record) {
                    is Deletion -> DeletionTable.integrateDeletion(record, userId)
                    is Answer -> AnswerTable.integrateAnswer(record, userId, packet.lastSyncAt)
                    is Intent -> IntentTable.integrateIntent(record, userId, packet.lastSyncAt)
                    is PathStep -> PathStepTable.integratePathStep(record, userId, packet.lastSyncAt)
                    is Question -> QuestionTable.integrateQuestion(record, userId, packet.lastSyncAt)
                    is Step -> StepTable.integrateStep(record, userId, packet.lastSyncAt)
                    is StepLog -> StepLogTable.integrateStepLog(record, userId, packet.lastSyncAt)
                    is StepTag -> StepTagTable.integrateStepTag(record, userId, packet.lastSyncAt)
                    is Tag -> TagTable.integrateTag(record, userId, packet.lastSyncAt)
                    is Trek -> TrekTable.integrateTrek(record, userId, packet.lastSyncAt)
                }
            } catch (e: Exception) {
                println("${record::class.nameOrError} sync error:\n${e.message}")
            }
        }

        // deletion gc 🗑
        val isTrailingOrigin = OriginSyncTable.select(OriginSyncTable.label)
            .where { OriginSyncTable.userId.eq(userId) }
            .orderBy(OriginSyncTable.syncAt).limit(1)
            .firstOrNull()?.let { it[OriginSyncTable.label] } == packet.origin

        if (isTrailingOrigin) {
            DeletionTable.deleteWhere { this.userId.eq(userId) and this.deletedAt.lessEq(packet.lastSyncAt) }
        }
    }
}
