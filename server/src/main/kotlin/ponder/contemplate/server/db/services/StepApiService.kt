package ponder.contemplate.server.db.services

import klutch.db.DbService
import klutch.db.read
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.update
import ponder.contemplate.model.data.Step
import ponder.contemplate.model.data.NewStep
import ponder.contemplate.server.db.tables.StepTable
import ponder.contemplate.server.db.tables.toStep

class StepApiService: DbService() {

    // Avast ye! This function fetches a single step by its id, or returns null if the treasure be not found!
    suspend fun readStep(stepId: Int) = dbQuery {
        StepTable.read { it.id.eq(stepId) }.firstOrNull()?.toStep()
    }

    // Gather all steps for a parent
    suspend fun readStepsByParent(parentId: Int) = dbQuery {
        StepTable.read { it.parentId.eq(parentId) }.map { it.toStep() }
    }

    // Arr! Fetch all the root steps
    suspend fun readRootSteps() = dbQuery {
        StepTable.read { it.parentId.isNull() }.map { it.toStep() }
    }

    // Add a new step to the plan, like marking a new spot on yer treasure map!
    suspend fun createStep(newStep: NewStep) = dbQuery {
        StepTable.insertAndGetId {
            it[this.parentId] = newStep.parentId
            it[this.label] = newStep.label
            it[this.position] = newStep.position
        }.value.toInt()
    }

    // Update a step, like redrawing part of yer map when ye find better information!
    suspend fun updateStep(step: Step) = dbQuery {
        StepTable.update(
            where = { StepTable.id.eq(step.id) }
        ) {
            it[this.parentId] = step.parentId
            it[this.label] = step.label
            it[this.position] = step.position
        } == 1
    }

    // Remove a step from the plan, like crossing out a spot on yer map that turned out to be empty!
    suspend fun deleteStep(stepId: Int) = dbQuery {
        StepTable.deleteWhere { this.id.eq(stepId) } == 1
    }
}
