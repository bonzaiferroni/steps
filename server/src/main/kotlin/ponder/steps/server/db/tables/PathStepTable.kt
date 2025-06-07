package ponder.steps.server.db.tables

import klutch.utils.toStringId
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import ponder.steps.model.data.PathStep

object PathStepTable: UUIDTable("path_step") {
    val pathId = reference("path_id", StepTable.id, ReferenceOption.CASCADE)
    val stepId = reference("step_id", StepTable.id, ReferenceOption.CASCADE)
    val position = integer("position")

    init {
        uniqueIndex(pathId, position)
    }
}

fun ResultRow.toPathStep() = PathStep(
    id = this[PathStepTable.id].value.toStringId(),
    stepId = this[PathStepTable.stepId].value.toStringId(),
    pathId = this[PathStepTable.pathId].value.toStringId(),
    position = this[PathStepTable.position]
)