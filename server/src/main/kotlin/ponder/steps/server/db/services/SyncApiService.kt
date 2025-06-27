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
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchUpsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.update
import ponder.steps.model.data.SyncData
import ponder.steps.server.db.tables.AnswerTable
import ponder.steps.server.db.tables.DeletionTable
import ponder.steps.server.db.tables.IntentTable
import ponder.steps.server.db.tables.PathStepTable
import ponder.steps.server.db.tables.QuestionTable
import ponder.steps.server.db.tables.StepLogTable
import ponder.steps.server.db.tables.StepTable
import ponder.steps.server.db.tables.StepTagTable
import ponder.steps.server.db.tables.TagTable
import ponder.steps.server.db.tables.TrekTable
import ponder.steps.server.db.tables.toAnswer
import ponder.steps.server.db.tables.toIntent
import ponder.steps.server.db.tables.upsertStep
import ponder.steps.server.db.tables.toPathStep
import ponder.steps.server.db.tables.toQuestion
import ponder.steps.server.db.tables.toStep
import ponder.steps.server.db.tables.toStepLog
import ponder.steps.server.db.tables.toStepTag
import ponder.steps.server.db.tables.toTag
import ponder.steps.server.db.tables.toTrek
import ponder.steps.server.db.tables.upsertAnswer
import ponder.steps.server.db.tables.upsertIntent
import ponder.steps.server.db.tables.upsertPathStep
import ponder.steps.server.db.tables.upsertQuestion
import ponder.steps.server.db.tables.upsertStepLog
import ponder.steps.server.db.tables.upsertStepTag
import ponder.steps.server.db.tables.upsertTag
import ponder.steps.server.db.tables.upsertTrek
import java.util.UUID

class SyncApiService : DbService() {

    suspend fun readSync(startSyncAt: Instant, endSyncAt: Instant, userId: String) = dbQuery {

        fun syncTable(userIdColumn: Column<EntityID<UUID>>, updatedAtColumn: Column<LocalDateTime>) =
            userIdColumn.eq(userId) and updatedAtColumn.greater(startSyncAt) and updatedAtColumn.lessEq(endSyncAt)

        val steps = StepTable.read { syncTable(it.userId, it.updatedAt) }.map { it.toStep() }
        val pathSteps = PathStepTable.read { syncTable(it.userId, it.updatedAt) }.map { it.toPathStep() }
        val questions = QuestionTable.read { syncTable(it.userId, it.updatedAt) }.map { it.toQuestion() }
        val intents = IntentTable.read { syncTable(it.userId, it.updatedAt) }.map { it.toIntent() }
        val treks = TrekTable.read { syncTable(it.userId, it.updatedAt) }.map { it.toTrek() }
        val stepLogs = StepLogTable.read { syncTable(it.userId, it.updatedAt) }.map { it.toStepLog() }
        val answers = AnswerTable.read { syncTable(it.userId, it.updatedAt) }.map { it.toAnswer() }
        val tags = TagTable.read { syncTable(it.userId, it.updatedAt) }.map { it.toTag() }
        val stepTags = StepTagTable.read { syncTable(it.userId, it.updatedAt) }.map { it.toStepTag() }

        val deletions = DeletionTable.readColumn(DeletionTable.id) { syncTable(it.userId, it.recordedAt) }
            .map { it.value.toStringId() }.toSet()

        SyncData(
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
                body = upsertStep(userId)
            )

            PathStepTable.batchUpsert(
                data = data.pathSteps,
                where = { PathStepTable.userId.eq(userId) and PathStepTable.updatedAt.lessEq(data.endSyncAt) },
                body = upsertPathStep(userId)
            )

            QuestionTable.batchUpsert(
                data = data.questions,
                where = { QuestionTable.userId.eq(userId) and QuestionTable.updatedAt.lessEq(data.endSyncAt) },
                body = upsertQuestion(userId)
            )

            IntentTable.batchUpsert(
                data = data.intents,
                where = { IntentTable.userId.eq(userId) and IntentTable.updatedAt.lessEq(data.endSyncAt) },
                body = upsertIntent(userId)
            )

            TrekTable.batchUpsert(
                data = data.treks,
                where = { TrekTable.userId.eq(userId) and TrekTable.updatedAt.lessEq(data.endSyncAt) },
                body = upsertTrek(userId)
            )

            StepLogTable.batchUpsert(
                data = data.stepLogs,
                where = { StepLogTable.userId.eq(userId) and StepLogTable.updatedAt.lessEq(data.endSyncAt) },
                body = upsertStepLog(userId)
            )

            AnswerTable.batchUpsert(
                data = data.answers,
                where = { AnswerTable.userId.eq(userId) and AnswerTable.updatedAt.lessEq(data.endSyncAt) },
                body = upsertAnswer(userId)
            )

            TagTable.batchUpsert(
                data = data.tags,
                where = { TagTable.userId.eq(userId) and TagTable.updatedAt.lessEq(data.endSyncAt) },
                body = upsertTag(userId)
            )

            StepTagTable.batchUpsert(
                data = data.stepTags,
                where = { StepTagTable.userId.eq(userId) and StepTagTable.updatedAt.lessEq(data.endSyncAt) },
                body = upsertStepTag(userId)
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

            // proto garbage collection, needs to use device id
            DeletionTable.deleteWhere { this.userId.eq(userId) and this.recordedAt.lessEq(data.endSyncAt) }

            true
        } catch (e: ExposedSQLException) {
            println(e.message)
            return@dbQuery false
        }
    }
}
