package ponder.steps.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import ponder.steps.model.data.Intent
import ponder.steps.model.data.IntentPriority

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = StepEntity::class,
            parentColumns = ["id"],
            childColumns = ["rootId"],
            onDelete = ForeignKey.CASCADE
        ),
    ],
    indices = [
        Index(value = ["rootId"]),
    ],
)
data class IntentEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val rootId: String,
    val label: String,
    val repeatMins: Int?,
    val expectedMins: Int?,
    val priority: IntentPriority,
    val pathIds: List<String>,
    val completedAt: Instant?,
    val scheduledAt: Instant?,
    val updatedAt: Instant
)

fun Intent.toEntity() = IntentEntity(
    id = id,
    userId = userId,
    rootId = rootId,
    label = label,
    repeatMins = repeatMins,
    expectedMins = expectedMins,
    priority = priority,
    pathIds = pathIds,
    completedAt = completedAt,
    scheduledAt = scheduledAt,
    updatedAt = updatedAt,
)