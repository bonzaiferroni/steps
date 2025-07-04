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
    val isComplete: Boolean,
    val createdAt: Instant,
    val finishedAt: Instant?,
    val expectedAt: Instant?,
    val updatedAt: Instant,
)

fun Trek.toEntity() = TrekEntity(
    id = id,
    userId = userId,
    intentId = intentId,
    rootId = rootId,
    isComplete = isComplete,
    createdAt = createdAt,
    finishedAt = finishedAt,
    expectedAt = expectedAt,
    updatedAt = updatedAt,
)