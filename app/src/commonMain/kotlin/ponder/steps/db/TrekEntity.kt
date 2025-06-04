package ponder.steps.db

import androidx.room.Entity
import androidx.room.ForeignKey
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
    ]
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
    pathIds = pathIds,
    breadCrumbs = breadCrumbs,
    availableAt = availableAt,
    startedAt = startedAt,
    progressAt = progressAt,
    finishedAt = finishedAt,
    expectedAt = expectedAt
)