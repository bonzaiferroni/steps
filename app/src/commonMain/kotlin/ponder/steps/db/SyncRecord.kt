package ponder.steps.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

@Entity
data class SyncRecord(
    @PrimaryKey
    val id: Long = 0,
    val startSyncAt: Instant,
    val endSyncAt: Instant,
)