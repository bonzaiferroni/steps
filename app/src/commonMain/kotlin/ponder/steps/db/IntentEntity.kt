package ponder.steps.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = StepEntity::class,
            parentColumns = ["id"],
            childColumns = ["rootId"],
            onDelete = ForeignKey.CASCADE
        ),
    ]
)
data class IntentEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val rootId: String,
    val label: String,
    val repeatMins: Int?,
    val expectedMins: Int?,
    // val isRegularTime: Boolean,
    val pathIds: List<String>,
    val completedAt: Instant?,
    val scheduledAt: Instant?,
)