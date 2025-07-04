package ponder.steps.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

@Entity
data class DeletionEntity(
    @PrimaryKey
    val id: String,
    val recordedAt: Instant,
    val entity: String,
)