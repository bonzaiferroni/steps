package ponder.steps.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Entity
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