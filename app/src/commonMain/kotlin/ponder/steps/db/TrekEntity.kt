package ponder.steps.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import ponder.steps.model.data.Trek

@Entity(
    foreignKeys = [
        ForeignKey(IntentEntity::class, ["id"], ["intentId"], ForeignKey.CASCADE),
        ForeignKey(StepEntity::class, ["id"], ["rootId"], ForeignKey.CASCADE),
        ForeignKey(PathStepEntity::class, ["id"], ["nextId"], ForeignKey.SET_NULL),
        ForeignKey(TrekEntity::class, ["id"], ["superId"], ForeignKey.CASCADE),
        ForeignKey(PathStepEntity::class, ["id"], ["pathStepId"], ForeignKey.SET_NULL)
    ],
    indices = [
        Index(value = ["intentId"]),
        Index(value = ["rootId"]),
        Index(value = ["nextId"]),
        Index(value = ["superId"]),
        Index(value = ["pathStepId"]),
    ],
)
data class TrekEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val intentId: String,
    val superId: String?,
    val pathStepId: String?,
    val rootId: String,
    val nextId: String?,
    val progress: Int,
    val isComplete: Boolean,
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
    superId = superId,
    pathStepId = pathStepId,
    rootId = rootId,
    nextId = nextId,
    progress = progress,
    isComplete = isComplete,
    availableAt = availableAt,
    startedAt = startedAt,
    progressAt = progressAt,
    finishedAt = finishedAt,
    expectedAt = expectedAt
)