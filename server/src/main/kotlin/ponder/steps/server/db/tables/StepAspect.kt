package ponder.steps.server.db.tables

import klutch.db.Aspect
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import ponder.steps.model.data.Step

object StepAspect: Aspect<StepAspect, Step>(
    StepTable.join(StepPositionTable, JoinType.LEFT, StepTable.id, StepPositionTable.stepId),
    ResultRow::toStep
)  {
    val stepId = add(StepTable.id)
    val parentId = add(StepPositionTable.parentId)
    val label = add(StepTable.label)
    val position = add(StepPositionTable.position)
}

fun ResultRow.toStep() = Step(
    id = this[StepAspect.stepId].toString(),
    parentId = this.getOrNull(StepAspect.parentId)?.toString(),
    label = this[StepAspect.label],
    position = this.getOrNull(StepAspect.position)
)