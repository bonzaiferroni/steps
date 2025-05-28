package ponder.steps.server.db.services

import klutch.db.DbService
import klutch.db.read
import klutch.utils.*
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.update
import ponder.steps.model.data.Step
import ponder.steps.model.data.NewStep
import ponder.steps.server.db.tables.StepAspect
import ponder.steps.server.db.tables.StepPositionTable
import ponder.steps.server.db.tables.StepTable
import ponder.steps.server.db.tables.toStep
import java.util.UUID

class StepApiService : DbService() {

    suspend fun readStep(stepId: String, includeChildren: Boolean) = dbQuery {
        val step = StepTable.read { it.id.eq(stepId) }.firstOrNull()?.toStep()
        if (includeChildren) step?.readChildren()?.first() else step
    }

    suspend fun readStepsByParent(parentId: String, includeChildren: Boolean) = dbQuery {
        val steps = StepAspect.read { it.parentId.eq(UUID.fromString(parentId)) }
        if (includeChildren) steps.readChildren() else steps
    }

    private fun Step.readChildren() = listOf(this).readChildren()

    private fun List<Step>.readChildren(): List<Step> {
        val parentIds = this.map { it.id.toUUID() }
        val children = StepPositionTable.join(StepTable, JoinType.LEFT, StepPositionTable.stepId, StepTable.id)
            .select(StepPositionTable.position, StepTable.id, StepTable.label)
            .where { StepPositionTable.parentId.inList(parentIds) }
            .map { it.toStep() }
        return this.map { parent ->
            parent.copy(children = children.filter { it.parentId == parent.id }.sortedBy { it.position })
        }
    }

    suspend fun readRootSteps(includeChildren: Boolean) = dbQuery {
        val subQuery = StepPositionTable.select(StepPositionTable.stepId)
        val steps = StepTable.read { it.id.notInSubQuery(subQuery) }.map { it.toStep() }
        if (includeChildren) steps.readChildren() else steps
    }

    suspend fun createStep(newStep: NewStep) = dbQuery {
        if (newStep.parentId != null && newStep.position == null) return@dbQuery null

        val stepId = StepTable.insertAndGetId {
            it[this.label] = newStep.label
        }.value

        newStep.parentId?.let { parentId ->
            StepPositionTable.insert {
                it[this.parentId] = UUID.fromString(parentId)
                it[this.stepId] = stepId
                it[this.position] = newStep.position!!
            }
        }

        stepId.toString()
    }

    suspend fun updateStep(step: Step) = dbQuery {
        val updated = StepTable.update(
            where = { StepTable.id.eq(UUID.fromString(step.id)) }
        ) {
            it[this.label] = step.label
        } == 1

        if (!updated) return@dbQuery null

        val parentId = step.parentId
        if (parentId != null) {
            val equalsParent = StepPositionTable.parentId.eq(UUID.fromString(parentId))
            val equalsStep = StepPositionTable.stepId.eq(UUID.fromString(step.id))
            StepPositionTable.update(
                where = { equalsParent and equalsStep }
            ) {
                it[this.position] = step.position!!
            }
        }

        true
    }

    suspend fun deleteStep(stepId: String) = dbQuery {
        StepTable.deleteWhere { this.id.eq(UUID.fromString(stepId)) } == 1
    }
}
