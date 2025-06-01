package ponder.steps.db

import ponder.steps.model.data.Step

data class StepJoin(
    val stepId: String,
    val userId: String?,
    val label: String,
    val description: String?,
    val expectedMins: Int?,
    val imgUrl: String?,
    val thumbUrl: String?,
    val audioUrl: String?,
    val isPublic: Boolean,
    val pathSize: Int,
    val pathId: String?,
    val position: Int?,
)

fun StepJoin.toStep() = Step(
    id = stepId,
    userId = userId,
    label = label,
    description = description,
    expectedMins = expectedMins,
    imgUrl = imgUrl,
    thumbUrl = thumbUrl,
    audioUrl = audioUrl,
    isPublic = isPublic,
    pathSize = pathSize,
    pathId = pathId,
    position = position,
    children = null // Children are not included in this join
)