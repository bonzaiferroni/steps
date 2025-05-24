package ponder.contemplate.server.db.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ResultRow
import ponder.contemplate.model.data.Step

internal object StepTable : IntIdTable("step") {
    val parentId = integer("parent_id").nullable()
    val label = text("label")
    val position = integer("position")
}

internal fun ResultRow.toStep() = Step(
    id = this[StepTable.id].value,
    parentId = this[StepTable.parentId],
    label = this[StepTable.label],
    position = this[StepTable.position],
)
