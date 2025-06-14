package ponder.steps.server.db.services

import kabinet.utils.nowToLocalDateTimeUtc
import kabinet.utils.toInstantFromUtc
import kabinet.utils.toLocalDateTimeUtc
import klutch.db.DbService
import klutch.db.read
import klutch.db.readColumn
import klutch.db.readCount
import klutch.utils.eq
import klutch.utils.fromStringId
import klutch.utils.greater
import klutch.utils.less
import klutch.utils.toStringId
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchUpsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.upsert
import ponder.steps.model.data.Step
import ponder.steps.model.data.NewStep
import ponder.steps.model.data.SyncData
import ponder.steps.server.db.tables.PathStepTable
import ponder.steps.server.db.tables.StepAspect
import ponder.steps.server.db.tables.StepTable
import ponder.steps.server.db.tables.toPathStep
import ponder.steps.server.db.tables.toStep

class PathService : DbService(1) {

    suspend fun readStep(stepId: String) = dbQuery {
        StepTable.read { it.id.eq(stepId) }.firstOrNull()?.toStep()
    }

    suspend fun readChildren(pathId: String) = dbQuery {
        StepAspect.read { PathStepTable.pathId.eq(pathId) }
    }

    suspend fun readRootSteps() = dbQuery {
        val subQuery = PathStepTable.select(PathStepTable.stepId)
        StepTable.read { it.id.notInSubQuery(subQuery) }.map { it.toStep() }
    }

    suspend fun createStep(newStep: NewStep, userId: String) = dbQuery {
        if (newStep.pathId != null && newStep.position == null) return@dbQuery null

        val stepId = StepTable.insertAndGetId {
            it[this.label] = newStep.label
            it[this.userId] = userId.fromStringId()
            it[this.createdAt] = Clock.nowToLocalDateTimeUtc()
            it[this.updatedAt] = Clock.nowToLocalDateTimeUtc()
            it[this.isPublic] = false
            it[this.pathSize] = 0
        }.value

        newStep.pathId?.let { pathId ->
            PathStepTable.insert {
                it[this.pathId] = pathId.fromStringId()
                it[this.stepId] = stepId
                it[this.position] = newStep.position!!
            }

            updatePathSize(pathId)
        }

        stepId.toString()
    }

    suspend fun updateStep(step: Step) = dbQuery {
        val updated = StepTable.update(
            where = { StepTable.id.eq(step.id) }
        ) {
            it[this.label] = step.label
            it[this.imgUrl] = step.imgUrl
        } == 1

        if (!updated) return@dbQuery false

        val parentId = step.pathId
        if (parentId != null) {
            val equalsParent = PathStepTable.pathId.eq(parentId)
            val equalsStep = PathStepTable.stepId.eq(step.id)
            PathStepTable.update(
                where = { equalsParent and equalsStep }
            ) {
                it[this.position] = step.position!!
            }
        }

        true
    }

    suspend fun deleteStep(stepId: String) = dbQuery {
        val pathIds =
            PathStepTable.readColumn(PathStepTable.pathId) { it.stepId.eq(stepId) }.map { it.value.toStringId() }
        val isSuccess = StepTable.deleteWhere { this.id.eq(stepId) } == 1
        if (isSuccess) {
            for (pathId in pathIds) {
                updatePathSize(pathId)
            }
        }
        isSuccess
    }

    suspend fun searchSteps(query: String) = dbQuery {
        StepTable.read {
            StepTable.label.lowerCase().like("%${query.lowercase()}%")
        }.map { it.toStep() }
    }
}

fun updatePathSize(pathId: String) {
    val pathSize = PathStepTable.readCount { it.pathId.eq(pathId) }
    StepTable.update(where = { StepTable.id.eq(pathId) }) {
        it[this.pathSize] = pathSize
    }
}
