package ponder.steps.db

import kotlinx.datetime.Instant
import ponder.steps.model.data.Step

data class StepJoin(
    val stepId: String,
    val userId: String?,
    val label: String,
    val description: String?,
    val theme: String?,
    val expectedMins: Int?,
    val imgUrl: String?,
    val thumbUrl: String?,
    val audioUrl: String?,
    val isPublic: Boolean,
    val pathSize: Int,
    val pathId: String?,
    val position: Int?,
    val updatedAt: Instant,
    val createdAt: Instant,
)

fun StepJoin.toStep() = Step(
    id = stepId,
    userId = userId,
    label = label,
    description = description,
    theme = theme,
    expectedMins = expectedMins,
    imgUrl = imgUrl,
    thumbUrl = thumbUrl,
    audioUrl = audioUrl,
    isPublic = isPublic,
    pathSize = pathSize,
    pathId = pathId,
    position = position,
    updatedAt = updatedAt,
    createdAt = createdAt,
)