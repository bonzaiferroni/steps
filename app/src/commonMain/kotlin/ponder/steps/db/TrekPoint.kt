package ponder.steps.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import ponder.steps.model.data.IntentId
import ponder.steps.model.data.TrekId

@Entity(
    foreignKeys = [
        ForeignKey(IntentEntity::class, ["id"], ["intentId"], ForeignKey.CASCADE),
        ForeignKey(TrekEntity::class, ["id"], ["trekId"], ForeignKey.CASCADE),
    ],
    indices = [Index("trekId"), Index("intentId")],
)
data class TrekPoint(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val intentId: IntentId,
    val trekId: TrekId? = null,
)

typealias TrekPointId = Long