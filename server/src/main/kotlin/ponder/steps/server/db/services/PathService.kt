package ponder.steps.server.db.services

import klutch.db.DbService
import klutch.db.read
import klutch.db.readColumn
import klutch.db.readCount
import klutch.utils.nowToLocalDateTimeUtc
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.update
import ponder.steps.model.data.Step
import ponder.steps.model.data.NewStep
import ponder.steps.server.db.tables.PathAspect
import ponder.steps.server.db.tables.PathStepTable
import ponder.steps.server.db.tables.StepAspect
import ponder.steps.server.db.tables.StepTable
import ponder.steps.server.db.tables.toStep

class PathService : DbService(1) {

    suspend fun readStep(stepId: Long, includeChildren: Boolean) = dbQuery {
        val step = StepTable.read { it.id.eq(stepId) }.firstOrNull()?.toStep()
        if (includeChildren) step?.addChildren()?.first() else step
    }

    suspend fun readParent(parentId: Long, includeChildren: Boolean) = dbQuery {
        val step = readStep(parentId, true)
        if (includeChildren) step?.copy(children = step.children?.addChildren()) else step
    }

    suspend fun readChildren(parentId: Long, includeChildren: Boolean) = dbQuery {
        val steps = PathAspect.read { it.pathId.eq(parentId) }
        if (includeChildren) steps.addChildren() else steps
    }

    suspend fun readRootSteps(includeChildren: Boolean) = dbQuery {
        val subQuery = PathStepTable.select(PathStepTable.stepId)
        val steps = StepTable.read { it.id.notInSubQuery(subQuery) }.map { it.toStep() }
        if (includeChildren) steps.addChildren() else steps
    }

    private fun Step.addChildren() = listOf(this).addChildren()

    private fun List<Step>.addChildren(): List<Step> {
        val parentIds = this.map { it.id }
        val children = StepAspect.read { PathStepTable.pathId.inList(parentIds) }
//        val children = PathStepTable.join(StepTable, JoinType.LEFT, PathStepTable.stepId, StepTable.id)
//            .select(PathStepTable.position, PathStepTable.pathId, StepTable.id, StepTable.label)
//            .where { PathStepTable.pathId.inList(parentIds) }
//            .map { it.toStep() }
        return this.map { parent ->
            parent.copy(children = children.filter { it.pathId == parent.id }.sortedBy { it.position })
        }
    }

    suspend fun createStep(newStep: NewStep, userId: Long) = dbQuery {
        if (newStep.pathId != null && newStep.position == null) return@dbQuery null

        val stepId = StepTable.insertAndGetId {
            it[this.label] = newStep.label
            it[this.userId] = userId
            it[this.createdAt] = Clock.nowToLocalDateTimeUtc()
            it[this.editedAt] = Clock.nowToLocalDateTimeUtc()
            it[this.isPublic] = false
            it[this.pathSize] = 0
        }.value

        newStep.pathId?.let { pathId ->
            PathStepTable.insert {
                it[this.pathId] = pathId
                it[this.stepId] = stepId
                it[this.position] = newStep.position!!
            }

            updatePathSize(pathId)
        }

        stepId
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

    suspend fun deleteStep(stepId: Long) = dbQuery {
        val pathIds = PathStepTable.readColumn(PathStepTable.pathId) { it.stepId.eq(stepId) }.map { it.value }
        val isSuccess = StepTable.deleteWhere { this.id.eq(stepId) } == 1
        if (isSuccess) {
            for (pathId in pathIds) {
                updatePathSize(pathId)
            }
        }
        isSuccess
    }

    suspend fun searchSteps(query: String, includeChildren: Boolean) = dbQuery {
        val steps = StepTable.read { 
            StepTable.label.lowerCase().like("%${query.lowercase()}%") 
        }.map { it.toStep() }

        if (includeChildren) steps.addChildren() else steps
    }
}

fun updatePathSize(pathId: Long) {
    val pathSize = PathStepTable.readCount { it.pathId.eq(pathId) }
    StepTable.update(where = { StepTable.id.eq(pathId) }) {
        it[this.pathSize] = pathSize
    }
}