package ponder.steps.server.db.services

import kabinet.utils.toLocalDateTimeUtc
import klutch.db.DbService
import klutch.db.read
import klutch.db.readColumn
import klutch.utils.eq
import klutch.utils.fromStringId
import klutch.utils.greater
import klutch.utils.lessEq
import klutch.utils.toStringId
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchUpsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.upsert
import ponder.steps.model.data.SyncData
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
import ponder.steps.server.db.tables.toAnswer
import ponder.steps.server.db.tables.toIntent
import ponder.steps.server.db.tables.syncStep
import ponder.steps.server.db.tables.toPathStep
import ponder.steps.server.db.tables.toQuestion
import ponder.steps.server.db.tables.toStep
import ponder.steps.server.db.tables.toStepLog
import ponder.steps.server.db.tables.toStepTag
import ponder.steps.server.db.tables.toTag
import ponder.steps.server.db.tables.toTrek
import ponder.steps.server.db.tables.syncAnswer
import ponder.steps.server.db.tables.syncIntent
import ponder.steps.server.db.tables.syncPathStep
import ponder.steps.server.db.tables.syncQuestion
import ponder.steps.server.db.tables.syncStepLog
import ponder.steps.server.db.tables.syncStepTag
import ponder.steps.server.db.tables.syncTag
import ponder.steps.server.db.tables.syncTrek
import java.util.UUID

class SyncApiService : DbService() {

    suspend fun readSync(startSyncAt: Instant, endSyncAt: Instant, userId: String) = dbQuery {

        fun readSyncData(userIdColumn: Column<EntityID<UUID>>, syncColumn: Column<LocalDateTime>) =
            userIdColumn.eq(userId) and syncColumn.greater(startSyncAt) and syncColumn.lessEq(endSyncAt)

        val steps = StepTable.read { readSyncData(it.userId, it.syncAt) }.map { it.toStep() }
        val pathSteps = PathStepTable.read { readSyncData(it.userId, it.syncAt) }.map { it.toPathStep() }
        val questions = QuestionTable.read { readSyncData(it.userId, it.syncAt) }.map { it.toQuestion() }
        val intents = IntentTable.read { readSyncData(it.userId, it.syncAt) }.map { it.toIntent() }
        val treks = TrekTable.read { readSyncData(it.userId, it.syncAt) }.map { it.toTrek() }
        val stepLogs = StepLogTable.read { readSyncData(it.userId, it.syncAt) }.map { it.toStepLog() }
        val answers = AnswerTable.read { readSyncData(it.userId, it.syncAt) }.map { it.toAnswer() }
        val tags = TagTable.read { readSyncData(it.userId, it.syncAt) }.map { it.toTag() }
        val stepTags = StepTagTable.read { readSyncData(it.userId, it.syncAt) }.map { it.toStepTag() }

        val deletions = DeletionTable.readColumn(DeletionTable.id) { readSyncData(it.userId, it.recordedAt) }
            .map { it.value.toStringId() }.toSet()

        SyncData(
            origin = "Server",
            startSyncAt = startSyncAt,
            endSyncAt = endSyncAt,
            deletions = deletions,
            steps = steps,
            pathSteps = pathSteps,
            questions = questions,
            intents = intents,
            treks = treks,
            stepLogs = stepLogs,
            answers = answers,
            tags = tags,
            stepTags = stepTags,
        )
    }

    suspend fun writeSync(data: SyncData, userId: String) = dbQuery {
        try {
            // handle updates
            StepTable.batchUpsert(
                data = data.steps,
                where = { StepTable.userId.eq(userId) and StepTable.updatedAt.lessEq(data.endSyncAt) },
                body = syncStep(userId, data.endSyncAt)
            )

            PathStepTable.batchUpsert(
                data = data.pathSteps,
                where = { PathStepTable.userId.eq(userId) and PathStepTable.updatedAt.lessEq(data.endSyncAt) },
                body = syncPathStep(userId, data.endSyncAt)
            )

            QuestionTable.batchUpsert(
                data = data.questions,
                where = { QuestionTable.userId.eq(userId) and QuestionTable.updatedAt.lessEq(data.endSyncAt) },
                body = syncQuestion(userId, data.endSyncAt)
            )

            IntentTable.batchUpsert(
                data = data.intents,
                where = { IntentTable.userId.eq(userId) and IntentTable.updatedAt.lessEq(data.endSyncAt) },
                body = syncIntent(userId, data.endSyncAt)
            )

            TrekTable.batchUpsert(
                data = data.treks,
                where = { TrekTable.userId.eq(userId) and TrekTable.updatedAt.lessEq(data.endSyncAt) },
                body = syncTrek(userId, data.endSyncAt)
            )

            StepLogTable.batchUpsert(
                data = data.stepLogs,
                where = { StepLogTable.userId.eq(userId) and StepLogTable.updatedAt.lessEq(data.endSyncAt) },
                body = syncStepLog(userId, data.endSyncAt)
            )

            AnswerTable.batchUpsert(
                data = data.answers,
                where = { AnswerTable.userId.eq(userId) and AnswerTable.updatedAt.lessEq(data.endSyncAt) },
                body = syncAnswer(userId, data.endSyncAt)
            )

            TagTable.batchUpsert(
                data = data.tags,
                where = { TagTable.userId.eq(userId) and TagTable.updatedAt.lessEq(data.endSyncAt) },
                body = syncTag(userId, data.endSyncAt)
            )

            StepTagTable.batchUpsert(
                data = data.stepTags,
                where = { StepTagTable.userId.eq(userId) and StepTagTable.updatedAt.lessEq(data.endSyncAt) },
                body = syncStepTag(userId, data.endSyncAt)
            )

            // handle deletions
            val deletionIds = data.deletions.map { it.fromStringId() }
            StepTable.deleteWhere { this.id.inList(deletionIds) and this.userId.eq(userId) }
            PathStepTable.deleteWhere { this.id.inList(deletionIds) and this.userId.eq(userId) }
            QuestionTable.deleteWhere { this.id.inList(deletionIds) and this.userId.eq(userId) }
            IntentTable.deleteWhere { this.id.inList(deletionIds) and this.userId.eq(userId) }
            TrekTable.deleteWhere { this.id.inList(deletionIds) and this.userId.eq(userId) }
            StepLogTable.deleteWhere { this.id.inList(deletionIds) and this.userId.eq(userId) }
            AnswerTable.deleteWhere { this.id.inList(deletionIds) and this.userId.eq(userId) }
            TagTable.deleteWhere { this.id.inList(deletionIds) and this.userId.eq(userId) }
            StepTagTable.deleteWhere { this.id.inList(deletionIds) and this.userId.eq(userId) }

            for (id in deletionIds) {
                DeletionTable.update(
                    where = { DeletionTable.id.eq(id) and DeletionTable.userId.eq(userId) }
                ) {
                    it[DeletionTable.recordedAt] = data.endSyncAt.toLocalDateTimeUtc()
                }
            }

            val isTrailingOrigin = OriginSyncTable.select(OriginSyncTable.label)
                .where { OriginSyncTable.userId.eq(userId) }
                .orderBy(OriginSyncTable.syncAt).limit(1)
                .firstOrNull()?.let { it[OriginSyncTable.label] } == data.origin

            if (isTrailingOrigin) {
                println("sync gc: ${data.origin}")
                DeletionTable.deleteWhere { this.userId.eq(userId) and this.recordedAt.lessEq(data.endSyncAt) }
            }

            OriginSyncTable.upsert(
                OriginSyncTable.label, OriginSyncTable.userId
            ) {
                it[OriginSyncTable.label] = data.origin
                it[OriginSyncTable.userId] = userId.fromStringId()
                it[OriginSyncTable.syncAt] = data.endSyncAt.toLocalDateTimeUtc()
            }

            true
        } catch (e: ExposedSQLException) {
            println(e.message)
            return@dbQuery false
        }
    }
}
