package ponder.steps.server.db.services

import klutch.db.DbService
import klutch.db.readById
import klutch.db.readColumn
import klutch.utils.nowToLocalDateTimeUtc
import klutch.utils.toInstantUtc
import klutch.utils.toLocalDateTimeUtc
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import ponder.steps.model.data.Intent
import ponder.steps.server.db.tables.PathStepTable
import ponder.steps.server.db.tables.IntentPathTable
import ponder.steps.server.db.tables.IntentTable
import ponder.steps.server.db.tables.TrekTable
import ponder.steps.server.db.tables.toIntent
import kotlin.time.Duration.Companion.minutes

class IntentApiService: DbService() {


}

fun syncIntentsWithTreks(userId: Long) {
    // read active intents
    val activeIntentIds = IntentTable.readColumn(IntentTable.id) {
        IntentTable.userId.eq(userId) and IntentTable.completedAt.isNull()
    }.map { it.value }

    // read active treks
    val trekQuestIds = TrekTable.readColumn(TrekTable.intentId) {
        TrekTable.userId.eq(userId) and TrekTable.finishedAt.isNull()
    }.map { it.value }

    for (intentId in activeIntentIds - trekQuestIds) {
        val intent = IntentTable.readById(intentId).toIntent()
        val availableAt = intent.scheduledAt ?: resolveAvailableAtFromLastTrek(intent) ?: Clock.System.now()

        TrekTable.insert {
            it[this.userId] = userId
            it[this.intentId] = intent.id
            it[this.rootId] = intent.rootId
            it[this.pathId] = null
            it[this.positionId] = intent.rootId
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

fun readStepCount(questId: Long): Int {
    val pathIds = readPathIds(questId)
    val stepCount = PathStepTable.select(PathStepTable.stepId)
        .where { PathStepTable.pathId.inList(pathIds) }
        .count().toInt()
    return stepCount - pathIds.size
}

fun readPathIds(questId: Long) = IntentPathTable.select(IntentPathTable.pathId)
    .where { IntentPathTable.intentId.eq(questId) }
    .map { it[IntentPathTable.pathId].value }

fun resolveAvailableAtFromLastTrek(intent: Intent): Instant? {
    val repeatMins = intent.repeatMins ?: return null

    val lastAvailableAt = TrekTable.select(TrekTable.availableAt)
        .where { TrekTable.intentId.eq(intent.id) and TrekTable.finishedAt.isNotNull() }
        .orderBy(TrekTable.startedAt, SortOrder.DESC_NULLS_LAST)
        .firstOrNull()?.let { it[TrekTable.availableAt] }?.toInstantUtc() ?: return null

    return lastAvailableAt + repeatMins.minutes
}