package ponder.steps.server.db.services

import klutch.db.DbService
import klutch.db.read
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.update
import ponder.steps.model.data.Step
import ponder.steps.model.data.NewStep
import ponder.steps.server.db.tables.StepAspect
import ponder.steps.server.db.tables.PathStepTable
import ponder.steps.server.db.tables.StepTable
import ponder.steps.server.db.tables.toStep

class StepApiService : DbService() {

    suspend fun readStep(stepId: Long, includeChildren: Boolean) = dbQuery {
        val step = StepTable.read { it.id.eq(stepId) }.firstOrNull()?.toStep()
        if (includeChildren) step?.addChildren()?.first() else step
    }

    suspend fun readParent(parentId: Long, includeChildren: Boolean) = dbQuery {
        val step = readStep(parentId, true)
        if (includeChildren) step?.copy(children = step.children?.addChildren()) else step
    }

    suspend fun readChildren(parentId: Long, includeChildren: Boolean) = dbQuery {
        val steps = StepAspect.read { it.pathId.eq(parentId) }
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
        val children = PathStepTable.join(StepTable, JoinType.LEFT, PathStepTable.stepId, StepTable.id)
            .select(PathStepTable.position, PathStepTable.pathId, StepTable.id, StepTable.label)
            .where { PathStepTable.pathId.inList(parentIds) }
            .map { it.toStep() }
        return this.map { parent ->
            parent.copy(children = children.filter { it.parentId == parent.id }.sortedBy { it.position })
        }
    }

    suspend fun createStep(newStep: NewStep) = dbQuery {
        if (newStep.parentId != null && newStep.position == null) return@dbQuery null

        val stepId = StepTable.insertAndGetId {
            it[this.label] = newStep.label
        }.value

        newStep.parentId?.let { parentId ->
            PathStepTable.insert {
                it[this.pathId] = parentId
                it[this.stepId] = stepId
                it[this.position] = newStep.position!!
            }
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

        val parentId = step.parentId
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
        StepTable.deleteWhere { this.id.eq(stepId) } == 1
    }
}
