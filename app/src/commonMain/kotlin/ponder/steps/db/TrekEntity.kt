package ponder.steps.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import ponder.steps.model.data.Trek

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = IntentEntity::class,
            parentColumns = ["id"],
            childColumns = ["intentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StepEntity::class,
            parentColumns = ["id"],
            childColumns = ["rootId"],
            onDelete = ForeignKey.CASCADE
        ),
    ],
    indices = [
        Index(value = ["intentId"]),
        Index(value = ["rootId"]),
    ],
)
data class TrekEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val intentId: String,
    val rootId: String,
    val stepId: String,
    val stepIndex: Int,
    val stepCount: Int,
    val isComplete: Boolean,
    val pathIds: List<String>,
    val breadCrumbs: List<String>,
    val availableAt: Instant,
    val startedAt: Instant?,
    val progressAt: Instant?,
    val finishedAt: Instant?,
    val expectedAt: Instant?,
)

fun Trek.toEntity() = TrekEntity(
    id = id,
    userId = userId,
    intentId = intentId,
    rootId = rootId,
    stepId = stepId,
    stepIndex = stepIndex,
    stepCount = stepCount,
    isComplete = isComplete,
    pathIds = pathIds,
    breadCrumbs = breadCrumbs,
    availableAt = availableAt,
    startedAt = startedAt,
    progressAt = progressAt,
    finishedAt = finishedAt,
    expectedAt = expectedAt
)