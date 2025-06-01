package ponder.steps.server.db.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import ponder.steps.model.data.PathStep

object PathStepTable: UUIDTable("step_position") {
    val pathId = reference("parent_id", StepTable.id, ReferenceOption.CASCADE)
    val stepId = reference("step_id", StepTable.id, ReferenceOption.CASCADE)
    val position = integer("position")

    init {
        uniqueIndex(pathId, position)
    }
}

fun ResultRow.toPathStep() = PathStep(
    id = this[PathStepTable.id].value.toString(),
    stepId = this[PathStepTable.stepId].value.toString(),
    pathId = this[PathStepTable.pathId].value.toString(),
    position = this[PathStepTable.position]
)