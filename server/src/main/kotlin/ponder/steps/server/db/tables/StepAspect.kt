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
    val userId = add(StepTable.userId)
    val pathId = add(PathStepTable.pathId)
    val label = add(StepTable.label)
    val description = add(StepTable.description)
    val thumbUrl = add(StepTable.thumbUrl)
    val audioUrl = add(StepTable.audioUrl)
    val isPublic = add(StepTable.isPublic)
    val expectedMins = add(StepTable.expectedMins)
    val pathSize = add(StepTable.pathSize)
    val createdAt = add(StepTable.createdAt)
    val editedAt = add(StepTable.editedAt)

    val position = add(PathStepTable.position)
    val imgUrl = add(StepTable.imgUrl)
}

fun ResultRow.toStep() = Step(
    id = this[StepAspect.stepId].value,
    userId = this[StepAspect.userId].value,
    pathId = this.getOrNull(StepAspect.pathId)?.value,
    label = this[StepAspect.label],
    description = this[StepAspect.description],
    thumbUrl = this[StepAspect.thumbUrl],
    audioUrl = this[StepAspect.audioUrl],
    isPublic = this[StepAspect.isPublic],
    expectedMins = this[StepAspect.expectedMins],
    position = this.getOrNull(StepAspect.position),
    imgUrl = this[StepAspect.imgUrl],
    pathSize = this[StepAspect.pathSize],
    children = null
)