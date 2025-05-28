package ponder.steps.server.db.services

import klutch.db.DbService
import klutch.db.read
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

    // Avast ye! This function fetches a single step by its id, or returns null if the treasure be not found!
    suspend fun readStep(stepId: String) = dbQuery {
        StepTable.read { it.id.eq(UUID.fromString(stepId)) }.firstOrNull()?.toStep()
    }

    // Gather all steps for a parent
    suspend fun readStepsByParent(parentId: String) = dbQuery {
        StepAspect.read { it.parentId.eq(UUID.fromString(parentId)) }
    }

    // Arr! Fetch all the root steps
    suspend fun readRootSteps() = dbQuery {
        val subQuery = StepPositionTable.select(StepPositionTable.stepId)
        StepTable.read { it.id.notInSubQuery(subQuery) }.map { it.toStep() }
    }

    // Add a new step to the plan, like marking a new spot on yer treasure map!
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

    // Update a step, like redrawing part of yer map when ye find better information!
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

    // Remove a step from the plan, like crossing out a spot on yer map that turned out to be empty!
    suspend fun deleteStep(stepId: String) = dbQuery {
        StepTable.deleteWhere { this.id.eq(UUID.fromString(stepId)) } == 1
    }
}
