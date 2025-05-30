package ponder.steps.server.db.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object PathStepTable: Table("step_position") {
    val pathId = reference("parent_id", StepTable.id, ReferenceOption.CASCADE)
    val stepId = reference("step_id", StepTable.id, ReferenceOption.CASCADE)
    val position = integer("position").default(0)

    override val primaryKey = PrimaryKey(pathId, stepId, position, name = "PK_parent_step_position")
}