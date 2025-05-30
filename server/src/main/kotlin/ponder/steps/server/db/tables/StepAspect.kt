package ponder.steps.server.db.tables

import klutch.db.Aspect
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import ponder.steps.model.data.Step

object StepAspect: Aspect<StepAspect, Step>(
    StepTable.join(PathStepTable, JoinType.LEFT, StepTable.id, PathStepTable.stepId),
    ResultRow::toStep
)  {
    val stepId = add(StepTable.id)
    val pathId = add(PathStepTable.pathId)
    val label = add(StepTable.label)
    val position = add(PathStepTable.position)
    val imgUrl = add(StepTable.imgUrl)
}

fun ResultRow.toStep() = Step(
    id = this[StepAspect.stepId].value,
    parentId = this.getOrNull(StepAspect.pathId)?.value,
    label = this[StepAspect.label],
    position = this.getOrNull(StepAspect.position),
    imgUrl = this[StepAspect.imgUrl],
    children = null
)