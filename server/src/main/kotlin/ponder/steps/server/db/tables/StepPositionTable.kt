package ponder.steps.server.db.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object StepPositionTable: Table("step_position") {
    val parentId = reference("parent_id", StepTable.id, ReferenceOption.CASCADE)
    val stepId = reference("step_id", StepTable.id, ReferenceOption.CASCADE)
    val position = integer("position").default(0)

    override val primaryKey = PrimaryKey(parentId, stepId, name = "PK_parent_step")
}