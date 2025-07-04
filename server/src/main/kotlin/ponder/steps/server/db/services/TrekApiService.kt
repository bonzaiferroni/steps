package ponder.steps.server.db.services

import kabinet.utils.nowToLocalDateTimeUtc
import klutch.db.DbService
import klutch.db.readSingle
import klutch.db.readSingleOrNull
import klutch.db.readValue
import klutch.utils.eq
import klutch.utils.toStringId
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.*
import ponder.steps.server.db.tables.PathStepTable
import ponder.steps.server.db.tables.StepTable
import ponder.steps.server.db.tables.TrekItemAspect
import ponder.steps.server.db.tables.TrekTable

class TrekApiService: DbService() {

    suspend fun readUserTreks(userId: String) = dbQuery {
        TrekItemAspect.read { TrekTable.userId.eq(userId) }
    }

    suspend fun completeStep(trekId: String, userId: String) = dbQuery {
//        var trek = TrekTable.readSingleOrNull { it.id.eq(trekId) and it.userId.eq(userId) }?.toTrek()
//            ?: error("Trek not found")

//        val pathId = trek.breadCrumbs.lastOrNull()
//        if (pathId == null) {
//            trek = trek.copy(finishedAt = Clock.System.now())
//        } else {
//            val pathStep = PathStepTable.readSingle { it.pathId.eq(pathId) and it.stepId.eq(trek.stepId) }.toPathStep()
//            val nextStep = PathStepTable.readSingleOrNull { it.pathId.eq(pathId) and it.position.eq(pathStep.position + 1) }?.toPathStep()
//            if (nextStep == null) {
//                // stepping out of the path
//                val (nextStepId, breadCrumbs) = stepOut(trek.breadCrumbs)
//                if (nextStepId == null) {
//                    trek = trek.copy(finishedAt = Clock.System.now(), breadCrumbs = breadCrumbs, stepId = trek.rootId)
//                } else {
//                    trek = trek.copy(breadCrumbs = breadCrumbs, stepId = nextStepId)
//                }
//            } else {
//                val (stepId, breadCrumbs) = stepIn(nextStep.stepId, trek.breadCrumbs, trek.pathIds)
//                trek = trek.copy(stepId = stepId, breadCrumbs = breadCrumbs)
//            }
//        }
//
//        TrekTable.updateById(trekId.fromStringId()) {
//            it[this.breadCrumbs] = trek.breadCrumbs
//            it[this.nextId] = trek.stepId.fromStringId()
//            it[this.progress] = trek.progress + 1
//            it[this.progressAt] = Clock.nowToLocalDateTimeUtc()
//            it[this.finishedAt] = trek.finishedAt?.toLocalDateTimeUtc()
//        } == 1
        false
    }

    suspend fun stepIntoPath(trekId: String, userId: String) = dbQuery {
//        val trek = TrekTable.readSingleOrNull { it.id.eq(trekId) and it.userId.eq(userId) }?.toTrek()
//            ?: error("Trek not found")
//
//        if (pathSize(trek.stepId) == 0) error("Step is not a path: ${trek.stepId}")
//
//        val pathIds = trek.pathIds + trek.stepId
//        val (stepId, breadCrumbs) = stepIn(trek.stepId, trek.breadCrumbs, pathIds)
//
//        TrekTable.updateById(trekId.fromStringId()) {
//            it[this.breadCrumbs] = breadCrumbs
//            it[this.nextId] = stepId.fromStringId()
//            it[this.pathIds] = pathIds
//            it[this.stepCount] = readStepCount(pathIds)
//        } == 1
        false
    }
}

// returns the next step in the trek that is not consumed as a path and the associated breadcrumbs
fun stepIn(stepId: String, providedBreadCrumbs: List<String>, pathIds: List<String>): Pair<String, List<String>> {
    var nextStepId = stepId
    var breadCrumbs = providedBreadCrumbs
    while (pathIds.contains(nextStepId)) {
        breadCrumbs = breadCrumbs + nextStepId
        nextStepId = PathStepTable.select(PathStepTable.stepId)
            .where { PathStepTable.pathId.eq(nextStepId) and PathStepTable.position.eq(0) }
            .firstOrNull()?.let { it[PathStepTable.stepId].value.toStringId() }
            ?: error("pathId has no initial step: $nextStepId")
    }
    return nextStepId to breadCrumbs
}

// returns the next step above the current path, the null if the trek is finished
fun stepOut(providedBreadCrumbs: List<String>): Pair<String?, List<String>> {
    if (providedBreadCrumbs.isEmpty()) error("Stepped out of empty breadCrumbs")
    var stepId = providedBreadCrumbs.last()
    var breadCrumbs = providedBreadCrumbs - stepId
    var nextStepId: String? = null
    while (breadCrumbs.isNotEmpty()) {
        val position = PathStepTable.readSingle { it.pathId.eq(breadCrumbs.last()) and it.stepId.eq(stepId) }[PathStepTable.position]
        nextStepId = PathStepTable.readSingleOrNull { it.pathId.eq(breadCrumbs.last()) and it.position.eq(position + 1) }
            ?.let { it[PathStepTable.stepId].value.toStringId() }
        if (nextStepId != null) break
        stepId = breadCrumbs.last()
        breadCrumbs = breadCrumbs - stepId
    }
    return nextStepId to breadCrumbs
}

fun pathSize(stepId: String) = StepTable.readValue(StepTable.pathSize) { it.id.eq(stepId) }