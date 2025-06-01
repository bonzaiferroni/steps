package ponder.steps.db

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import ponder.steps.model.data.Step

@Entity
@Serializable
data class StepEntity(
    @PrimaryKey
    val id: String,
    val userId: String?,
    val label: String,
    val description: String?,
    val expectedMins: Int?,
    val imgUrl: String?,
    val thumbUrl: String?,
    val audioUrl: String?,
    val isPublic: Boolean,
    val pathSize: Int,
)

val StepEntity.Companion.Default get () = StepEntity(
    id = "",
    userId = null,
    label = "",
    description = null,
    expectedMins = null,
    imgUrl = null,
    thumbUrl = null,
    audioUrl = null,
    isPublic = false,
    pathSize = 0,
)

fun StepEntity.toStep(
    children: List<Step> = emptyList()
) = Step(
    id = id,
    userId = userId,
    label = label,
    description = description,
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
    expectedMins = expectedMins,
    imgUrl = imgUrl,
    thumbUrl = thumbUrl,
    audioUrl = audioUrl,
    isPublic = isPublic,
    pathSize = pathSize
)