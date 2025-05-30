package ponder.steps.server.db.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption

internal object TrekPathTable: LongIdTable("trek_path") {
    val trekId = reference("trek_id", TrekTable.id, onDelete = ReferenceOption.CASCADE)
    val pathId = reference("step_id", StepTable.id, onDelete = ReferenceOption.CASCADE)
}