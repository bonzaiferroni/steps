package ponder.steps.server.db.services

import klutch.db.DbService
import klutch.db.readById
import klutch.db.readColumn
import klutch.db.readCount
import klutch.db.read
import klutch.utils.nowToLocalDateTimeUtc
import klutch.utils.toInstantUtc
import klutch.utils.toLocalDateTimeUtc
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.deleteWhere
import ponder.steps.model.data.Intent
import ponder.steps.model.data.NewIntent
import ponder.steps.server.db.tables.PathStepTable
import ponder.steps.server.db.tables.IntentPathTable
import ponder.steps.server.db.tables.IntentTable
import ponder.steps.server.db.tables.StepTable
import ponder.steps.server.db.tables.TrekTable
import ponder.steps.server.db.tables.toIntent
import ponder.steps.server.db.tables.toStep
import kotlin.time.Duration.Companion.minutes

class IntentionService: DbService() {

    suspend fun readIntent(intentId: Long) = dbQuery {
        IntentTable.readById(intentId).toIntent()
    }

    suspend fun readUserIntents(userId: Long) = dbQuery {
        IntentTable.read { it.userId.eq(userId) }.map { it.toIntent() }
    }

    suspend fun createIntent(newIntent: NewIntent, userId: Long) = dbQuery {
        val step = StepTable.readById(newIntent.rootId).toStep()
        if (step.userId != userId) error("rootId: ${newIntent.rootId} does not belong to user: $userId")

        IntentTable.insertAndGetId {
            it[this.userId] = userId
            it[this.rootId] = newIntent.rootId
            it[this.label] = newIntent.label
            it[this.expectedMins] = newIntent.expectedMins
            it[this.repeatMins] = newIntent.repeatMins
            it[this.scheduledAt] = newIntent.scheduledAt?.toLocalDateTimeUtc()
        }.value
    }

    suspend fun updateIntent(intent: Intent, userId: Long) = dbQuery {
        IntentTable.update(
            where = { IntentTable.id.eq(intent.id) and IntentTable.userId.eq(userId) }
        ) {
            it[this.label] = intent.label
            it[this.expectedMins] = intent.expectedMins
            it[this.repeatMins] = intent.repeatMins
            it[this.completedAt] = intent.completedAt?.toLocalDateTimeUtc()
            it[this.scheduledAt] = intent.scheduledAt?.toLocalDateTimeUtc()
        } == 1
    }

    suspend fun deleteIntent(intentId: Long, userId: Long) = dbQuery {
        IntentTable.deleteWhere { this.id.eq(intentId) and this.userId.eq(userId) } == 1
    }
}

fun syncIntentsWithTreks(userId: Long) {
    // read active intents
    val activeIntentIds = IntentTable.readColumn(IntentTable.id) {
        IntentTable.userId.eq(userId) and IntentTable.completedAt.isNull()
    }.map { it.value }

    // read active treks
    val trekIntentIds = TrekTable.readColumn(TrekTable.intentId) {
        TrekTable.userId.eq(userId) and TrekTable.finishedAt.isNull()
    }.map { it.value }

    for (intentId in activeIntentIds - trekIntentIds) {
        val intent = IntentTable.readById(intentId).toIntent()
        val availableAt = intent.scheduledAt ?: resolveAvailableAtFromLastTrek(intent) ?: Clock.System.now()

        val pathIds = readPathIds(intentId)
        val (stepId, breadCrumbs) = stepIn(intent.rootId, emptyList(), pathIds)

        TrekTable.insert {
            it[this.userId] = userId
            it[this.intentId] = intent.id
            it[this.rootId] = intent.rootId
            it[this.stepId] = stepId
            it[this.breadCrumbs] = breadCrumbs
            it[this.pathIds] = pathIds
            it[this.stepIndex] = 0
            it[this.stepCount] = readStepCount(intent.id)
            it[this.availableAt] = availableAt.toLocalDateTimeUtc()
            it[this.startedAt] = Clock.nowToLocalDateTimeUtc()
            it[this.progressAt] = Clock.nowToLocalDateTimeUtc()
            it[this.finishedAt] = null
            it[this.expectedAt] = intent.expectedMins?.let { mins -> Clock.System.now() + mins.minutes }?.toLocalDateTimeUtc()
        }
    }
}

fun readStepCount(intentId: Long): Int {
    val pathIds = readPathIds(intentId)
    val stepCount = PathStepTable.readCount(PathStepTable.stepId) { PathStepTable.pathId.inList(pathIds) }
    return stepCount - pathIds.size
}

fun readPathIds(intentId: Long) = IntentPathTable.readColumn(IntentPathTable.pathId) {
    IntentPathTable.intentId.eq(intentId)
}.map { it.value }

fun resolveAvailableAtFromLastTrek(intent: Intent): Instant? {
    val repeatMins = intent.repeatMins ?: return null

    val lastAvailableAt = TrekTable.select(TrekTable.availableAt)
        .where { TrekTable.intentId.eq(intent.id) and TrekTable.finishedAt.isNotNull() }
        .orderBy(TrekTable.startedAt, SortOrder.DESC_NULLS_LAST)
        .firstOrNull()?.let { it[TrekTable.availableAt] }?.toInstantUtc() ?: return null

    return lastAvailableAt + repeatMins.minutes
}
