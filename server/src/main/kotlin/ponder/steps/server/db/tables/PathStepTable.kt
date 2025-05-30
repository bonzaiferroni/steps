package ponder.steps.server.db.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import ponder.steps.model.data.PathStep

object PathStepTable: LongIdTable("step_position") {
    val pathId = reference("parent_id", StepTable.id, ReferenceOption.CASCADE)
    val stepId = reference("step_id", StepTable.id, ReferenceOption.CASCADE)
    val position = integer("position").uniqueIndex()
}

fun ResultRow.toPathStep() = PathStep(
    id = this[PathStepTable.id].value,
    stepId = this[PathStepTable.stepId].value,
    pathId = this[PathStepTable.pathId].value,
    position = this[PathStepTable.position]
)