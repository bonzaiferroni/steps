package ponder.steps.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.internal.NamedCompanion
import ponder.steps.model.data.Step

@Entity
data class StepEntity(
    @PrimaryKey
    val id: String,
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
) {
    companion object
}

fun StepEntity.toStep(
    children: List<Step> = emptyList()
) = Step(
    id = id,
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
    pathId = null,
    position = null,
    children = children
)

fun Step.toStepEntity() = StepEntity(
    id = id,
    userId = userId,
    label = label,
    description = description,
    theme = theme,
    expectedMins = expectedMins,
    imgUrl = imgUrl,
    thumbUrl = thumbUrl,
    audioUrl = audioUrl,
    isPublic = isPublic,
    pathSize = pathSize
)

val StepEntity.Companion.Empty: StepEntity get() = StepEntity(
    id = "",
    userId = null,
    label = "",
    description = null,
    theme = null,
    expectedMins = null,
    imgUrl = null,
    thumbUrl = null,
    audioUrl = null,
    isPublic = false,
    pathSize = 0,
)