package ponder.steps.server.db.services

import klutch.db.DbService
import klutch.db.readCount
import klutch.db.readSingle
import klutch.db.readSingleOrNull
import klutch.db.updateById
import klutch.utils.nowToLocalDateTimeUtc
import klutch.utils.toLocalDateTimeUtc
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import ponder.steps.server.db.tables.PathStepTable
import ponder.steps.server.db.tables.TrekItemAspect
import ponder.steps.server.db.tables.TrekPathTable
import ponder.steps.server.db.tables.TrekTable
import ponder.steps.server.db.tables.toPathStep
import ponder.steps.server.db.tables.toTrek

class JourneyService: DbService() {

    suspend fun readUserTreks(userId: Long) = dbQuery {
        syncIntentsWithTreks(userId)
        TrekItemAspect.read { TrekTable.userId.eq(userId) }
    }

    suspend fun startTrek(trekId: Long, userId: Long) = dbQuery {
        TrekTable.update(where = { TrekTable.id.eq(trekId) and TrekTable.userId.eq(userId)}) {
            it[this.startedAt] = Clock.nowToLocalDateTimeUtc()
        } == 1
    }

    suspend fun pauseTrek(trekId: Long, userId: Long) = dbQuery {
        TrekTable.update(where = { TrekTable.id.eq(trekId) and TrekTable.userId.eq(userId)}) {
            it[this.startedAt] = null
        } == 1
    }

    suspend fun completeStep(trekId: Long, userId: Long) = dbQuery {
        var trek = TrekTable.readSingleOrNull { it.id.eq(trekId) and it.userId.eq(userId) }?.toTrek()
            ?: error("Trek not found")

        val pathId = trek.breadCrumbs.lastOrNull()
        if (pathId == null) {
            trek = trek.copy(finishedAt = Clock.System.now())
        } else {
            val pathStep = PathStepTable.readSingle { it.pathId.eq(pathId) and it.stepId.eq(trek.stepId) }.toPathStep()
            var nextStep = PathStepTable.readSingleOrNull { it.pathId.eq(pathId) and it.position.eq(pathStep.position + 1) }?.toPathStep()
            if (nextStep == null) {
                // stepping out of the path
                val (nextStepId, breadCrumbs) = stepOut(trek.breadCrumbs)
                if (nextStepId == null) {
                    trek = trek.copy(finishedAt = Clock.System.now(), breadCrumbs = breadCrumbs, stepId = trek.rootId)
                } else {
                    trek = trek.copy(breadCrumbs = breadCrumbs, stepId = nextStepId)
                }
            } else {
                val (stepId, breadCrumbs) = stepIn(nextStep.stepId, trek.breadCrumbs, trek.pathIds)
                trek = trek.copy(stepId = stepId, breadCrumbs = breadCrumbs)
            }
        }

        TrekTable.updateById(trekId) {
            it[this.breadCrumbs] = trek.breadCrumbs
            it[this.stepId] = trek.stepId
            it[this.stepIndex] = trek.stepIndex + 1
            it[this.progressAt] = Clock.nowToLocalDateTimeUtc()
            it[this.finishedAt] = trek.finishedAt?.toLocalDateTimeUtc()
        } == 1
    }

    suspend fun stepIntoCurrentPath(trekId: Long, userId: Long) = dbQuery {
        val trek = TrekTable.readSingleOrNull { it.id.eq(trekId) and it.userId.eq(userId) }?.toTrek()
            ?: error("Trek not found")

        if (!isPath(trek.stepId)) error("Step is not a path: ${trek.stepId}")

        val pathIds = trek.pathIds + trek.stepId
        val (stepId, breadCrumbs) = stepIn(trek.stepId, trek.breadCrumbs, pathIds)

        TrekTable.updateById(trekId) {
            it[this.breadCrumbs] = breadCrumbs
            it[this.stepId] = stepId
            it[this.pathIds] = pathIds
        } == 1
    }
}

// returns the next step in the trek that is not consumed as a path and the associated breadcrumbs
fun stepIn(stepId: Long, providedBreadCrumbs: List<Long>, pathIds: List<Long>): Pair<Long, List<Long>> {
    var nextStepId = stepId
    var breadCrumbs = providedBreadCrumbs
    while (pathIds.contains(nextStepId)) {
        breadCrumbs = breadCrumbs + nextStepId
        nextStepId = PathStepTable.select(PathStepTable.stepId)
            .where { PathStepTable.pathId.eq(nextStepId) and PathStepTable.position.eq(0) }
            .firstOrNull()?.let { it[PathStepTable.stepId].value }
            ?: error("pathId has no initial step: $nextStepId")
    }
    return nextStepId to breadCrumbs
}

// returns the next step above the current path, the null if the trek is finished
fun stepOut(providedBreadCrumbs: List<Long>): Pair<Long?, List<Long>> {
    if (providedBreadCrumbs.isEmpty()) error("Stepped out of empty breadCrumbs")
    if (providedBreadCrumbs.size == 1) return providedBreadCrumbs.last() to emptyList()
    var stepId = providedBreadCrumbs.last()
    var breadCrumbs = providedBreadCrumbs - stepId
    var nextStepId: Long? = null
    while (breadCrumbs.isNotEmpty()) {
        val position = PathStepTable.readSingle { it.pathId.eq(breadCrumbs.last()) and it.stepId.eq(stepId) }[PathStepTable.position]
        nextStepId = PathStepTable.readSingleOrNull { it.pathId.eq(breadCrumbs.last()) and it.position.eq(position + 1) }
            ?.let { it[PathStepTable.stepId].value }
        if (nextStepId != null) break
        stepId = breadCrumbs.last()
        breadCrumbs = breadCrumbs - stepId
    }
    return nextStepId to breadCrumbs
}

fun isPath(stepId: Long) = TrekPathTable.readCount(TrekPathTable.trekId) { TrekPathTable.pathId.eq(stepId) } > 0