package ponder.steps.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ponder.steps.model.data.Step

@Entity
data class StepEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val label: String,
    val description: String?,
    val theme: String?,
    val expectedMins: Int?,
    val imgUrl: String?,
    val thumbUrl: String?,
    val audioLabelUrl: String?,
    val audioFullUrl: String?,
    val isPublic: Boolean,
    val pathSize: Int,
    val updatedAt: Instant,
    val createdAt: Instant,
) {
    companion object {
        val Empty get() = StepEntity(
            id = "",
            userId = "",
            label = "",
            description = null,
            theme = null,
            expectedMins = null,
            imgUrl = null,
            thumbUrl = null,
            audioLabelUrl = null,
            audioFullUrl = null,
            isPublic = false,
            pathSize = 0,
            updatedAt = Clock.System.now(),
            createdAt = Clock.System.now(),
        )
    }
}

fun StepEntity.toStep() = Step(
    id = id,
    userId = userId,
    label = label,
    description = description,
    theme = theme,
    expectedMins = expectedMins,
    imgUrl = imgUrl,
    thumbUrl = thumbUrl,
    audioLabelUrl = audioLabelUrl,
    audioFullUrl = audioFullUrl,
    isPublic = isPublic,
    pathSize = pathSize,
    updatedAt = updatedAt,
    createdAt = createdAt,
    pathId = null,
    pathStepId = null,
    position = null,
)

fun Step.toEntity() = StepEntity(
    id = id,
    userId = userId,
    label = label,
    description = description,
    theme = theme,
    expectedMins = expectedMins,
    imgUrl = imgUrl,
    thumbUrl = thumbUrl,
    audioLabelUrl = audioLabelUrl,
    audioFullUrl = audioFullUrl,
    isPublic = isPublic,
    pathSize = pathSize,
    updatedAt = updatedAt,
    createdAt = createdAt,
)