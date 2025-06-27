package ponder.steps.server.db.services

import kabinet.utils.nowToLocalDateTimeUtc
import kabinet.utils.toInstantFromUtc
import kabinet.utils.toLocalDateTimeUtc
import klutch.db.DbService
import klutch.db.readById
import klutch.db.readColumn
import klutch.db.readCount
import klutch.db.read
import klutch.utils.eq
import klutch.utils.fromStringId
import klutch.utils.toStringId
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.deleteWhere
import ponder.steps.model.data.Intent
import ponder.steps.model.data.NewIntent
import ponder.steps.server.db.tables.PathStepTable
import ponder.steps.server.db.tables.IntentTable
import ponder.steps.server.db.tables.StepTable
import ponder.steps.server.db.tables.TrekTable
import ponder.steps.server.db.tables.toIntent
import ponder.steps.server.db.tables.toStep
import kotlin.time.Duration.Companion.minutes

class IntentionService: DbService() {

    suspend fun readIntent(intentId: String) = dbQuery {
        IntentTable.readById(intentId.fromStringId()).toIntent()
    }

    suspend fun readUserIntents(userId: String) = dbQuery {
        IntentTable.read { it.userId.eq(userId) }.map { it.toIntent() }
    }

    suspend fun createIntent(newIntent: NewIntent, userId: String) = dbQuery {
        val step = StepTable.readById(newIntent.rootId.fromStringId()).toStep()
        if (step.userId != userId) error("rootId: ${newIntent.rootId} does not belong to user: $userId")

        val intentId = IntentTable.insertAndGetId {
            it[this.userId] = userId.fromStringId()
            it[this.rootId] = newIntent.rootId.fromStringId()
            it[this.label] = newIntent.label
            it[this.expectedMins] = newIntent.expectedMins
            it[this.repeatMins] = newIntent.repeatMins
            it[this.scheduledAt] = newIntent.scheduledAt?.toLocalDateTimeUtc()
        }.value

        syncIntentsWithTreks(userId)
        intentId.toString()
    }

    suspend fun updateIntent(intent: Intent, userId: String) = dbQuery {
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

    suspend fun deleteIntent(intentId: String, userId: String) = dbQuery {
        IntentTable.deleteWhere { this.id.eq(intentId) and this.userId.eq(userId) } == 1
    }
}

fun syncIntentsWithTreks(userId: String) {
    // read active intents
    val activeIntentIds = IntentTable.readColumn(IntentTable.id) {
        IntentTable.userId.eq(userId) and IntentTable.completedAt.isNull()
    }.map { it.value.toStringId() }

    // read active treks
    val trekIntentIds = TrekTable.readColumn(TrekTable.intentId) {
        TrekTable.userId.eq(userId) and TrekTable.finishedAt.isNull()
    }.map { it.value.toStringId() }

    for (intentId in activeIntentIds - trekIntentIds) {
        val intent = IntentTable.readById(intentId.fromStringId()).toIntent()
        val availableAt = intent.scheduledAt ?: resolveAvailableAtFromLastTrek(intent) ?: Clock.System.now()

        TrekTable.insert {
            it[this.userId] = userId.fromStringId()
            it[this.intentId] = intent.id.fromStringId()
            it[this.rootId] = intent.rootId.fromStringId()
            it[this.progress] = 0
            it[this.availableAt] = availableAt.toLocalDateTimeUtc()
            it[this.progressAt] = Clock.nowToLocalDateTimeUtc()
            it[this.startedAt] = null
            it[this.finishedAt] = null
            it[this.expectedAt] = intent.expectedMins?.let { mins -> Clock.System.now() + mins.minutes }?.toLocalDateTimeUtc()
        }
    }
}

fun readStepCount(pathIds: List<String>): Int {
    if (pathIds.isEmpty()) return 1
    val stepCount = PathStepTable.readCount { PathStepTable.pathId.inList(pathIds.map { it.fromStringId() }) }
    return stepCount
}

fun resolveAvailableAtFromLastTrek(intent: Intent): Instant? {
    val repeatMins = intent.repeatMins ?: return null

    val lastAvailableAt = TrekTable.select(TrekTable.availableAt)
        .where { TrekTable.intentId.eq(intent.id) and TrekTable.finishedAt.isNotNull() }
        .orderBy(TrekTable.startedAt, SortOrder.DESC_NULLS_LAST)
        .firstOrNull()?.let { it[TrekTable.availableAt] }?.toInstantFromUtc() ?: return null

    return lastAvailableAt + repeatMins.minutes
}
