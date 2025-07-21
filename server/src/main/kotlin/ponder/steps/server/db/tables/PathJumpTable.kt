package ponder.steps.server.db.tables

import klutch.utils.toStringId
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import ponder.steps.model.data.PathJump
import ponder.steps.model.data.PathJumpId

internal object PathJumpTable: UUIDTable("path_jump") {
    val fromPathStepId = reference("from_path_step_id", PathStepTable.id, onDelete = ReferenceOption.CASCADE)
    val toPathStepId = reference("to_path_step_id", PathStepTable.id, onDelete = ReferenceOption.CASCADE)
    val questionId = reference("question_id", QuestionTable.id, onDelete = ReferenceOption.CASCADE)
    val value = text("value").nullable()
}

internal fun ResultRow.toPathJump() = PathJump(
    id = PathJumpId(this[PathJumpTable.id].value.toStringId()),
    fromPathStepId = this[PathJumpTable.fromPathStepId].value.toStringId(),
    toPathStepId = this[PathJumpTable.toPathStepId].value.toStringId(),
    questionId = this[PathJumpTable.questionId].value.toStringId(),
    value = this[PathJumpTable.value]
)