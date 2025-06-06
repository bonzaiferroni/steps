package ponder.steps.server.db.tables

import kabinet.utils.toInstantUtc
import klutch.db.Aspect
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import ponder.steps.model.data.Step

object PathAspect: Aspect<PathAspect, Step>(
    StepTable.join(PathStepTable, JoinType.LEFT, StepTable.id, PathStepTable.stepId),
    ResultRow::toStep
)  {
    val stepId = add(StepTable.id)
    val userId = add(StepTable.userId)
    val pathId = add(PathStepTable.pathId)
    val label = add(StepTable.label)
    val description = add(StepTable.description)
    val theme = add(StepTable.theme)
    val thumbUrl = add(StepTable.thumbUrl)
    val audioUrl = add(StepTable.audioUrl)
    val isPublic = add(StepTable.isPublic)
    val expectedMins = add(StepTable.expectedMins)
    val pathSize = add(StepTable.pathSize)
    val createdAt = add(StepTable.createdAt)
    val updatedAt = add(StepTable.updatedAt)

    val position = add(PathStepTable.position)
    val imgUrl = add(StepTable.imgUrl)
}

fun ResultRow.toStep() = Step(
    id = this[PathAspect.stepId].value.toString(),
    userId = this[PathAspect.userId].value.toString(),
    pathId = this.getOrNull(PathAspect.pathId)?.value.toString(),
    label = this[PathAspect.label],
    description = this[PathAspect.description],
    theme = this[PathAspect.theme],
    thumbUrl = this[PathAspect.thumbUrl],
    audioUrl = this[PathAspect.audioUrl],
    isPublic = this[PathAspect.isPublic],
    expectedMins = this[PathAspect.expectedMins],
    position = this.getOrNull(PathAspect.position),
    imgUrl = this[PathAspect.imgUrl],
    pathSize = this[PathAspect.pathSize],
    updatedAt = this[PathAspect.updatedAt].toInstantUtc(),
    createdAt = this[PathAspect.createdAt].toInstantUtc()
)