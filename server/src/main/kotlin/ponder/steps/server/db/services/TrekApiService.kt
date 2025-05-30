package ponder.steps.server.db.services

import klutch.db.DbService
import klutch.db.read
import klutch.db.readById
import klutch.utils.nowToLocalDateTimeUtc
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.*
import ponder.steps.model.data.TrekStatus
import ponder.steps.server.db.tables.IntentTable
import ponder.steps.server.db.tables.StepAspect
import ponder.steps.server.db.tables.PathStepTable
import ponder.steps.server.db.tables.StepTable
import ponder.steps.server.db.tables.TrekTable
import ponder.steps.server.db.tables.toIntent
import ponder.steps.server.db.tables.toStep
import ponder.steps.server.db.tables.toTrek

class TrekApiService: DbService() {

    suspend fun readActiveTreks(userId: Long) = dbQuery {
        TrekTable.read { it.userId.eq(userId) and it.finishedAt.isNull() }.map { it.toTrek() }
    }

    suspend fun readStatus(trekId: Long) = dbQuery {
        val step = TrekTable.join(StepTable, JoinType.LEFT, TrekTable.positionId, StepTable.id)
            .select(StepTable.columns)
            .where { TrekTable.id.eq(trekId) }
            .firstOrNull()?.toStep()
            ?: error("trekId not found: $trekId")

        val isPath = PathStepTable.select(PathStepTable.pathId.count())
            .where { PathStepTable.pathId.eq(step.id) }
            .single()[PathStepTable.pathId.count()] > 0

        TrekStatus(step, isPath)
    }

    suspend fun completeStep(trekId: Long) = dbQuery {
        val trek = TrekTable.readById(trekId).toTrek()
        val intentId = trek.intentId; val pathId = trek.pathId; val rootId = trek.rootId; val positionId = trek.positionId
        val quest = IntentTable.readById(intentId).toIntent()
        val pathIds = readPathIds(intentId)

//        val currentPosition = currentStep.position ?: error("current step does not have position: $currentStepId")
//        val nextStep = StepAspect.readFirst { it.pathId.eq(trek.rootId) and it.position.eq(currentPosition + 1) }
//        if (nextStep != null) {
//            TrekTable.update(where = { TrekTable.id.eq(trekId) }) {
//                it[this.positionId] = nextStep.id
//                it[this.progressAt] = Clock.nowToLocalDateTimeUtc()
//            }
//        }
    }

    // helper methods

    // returns Pair with pathId and current positionId
    fun settlePositionId(pathId: Long?, positionId: Long, pathIds: List<Long>): Pair<Long?, Long> {
        var settledPathId: Long? = pathId
        var settledId = positionId
        while (pathIds.contains(settledId)) {
            settledPathId = settledId
            settledId = PathStepTable.select(PathStepTable.stepId)
                .where { PathStepTable.pathId.eq(settledId) and PathStepTable.position.eq(0) }
                .firstOrNull()?.let { it[PathStepTable.stepId].value }
                ?: error("pathId has no initial step: $settledId")
        }
        return settledPathId to settledId
    }
}