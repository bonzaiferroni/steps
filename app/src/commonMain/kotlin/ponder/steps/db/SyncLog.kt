package ponder.steps.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

@Entity
data class SyncLog(
    @PrimaryKey
    val id: Long = 1,
    val lastSyncAt: Instant,
)