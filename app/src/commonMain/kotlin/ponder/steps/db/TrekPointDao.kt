package ponder.steps.db

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import kotlinx.datetime.Instant
import ponder.steps.model.data.IntentId
import ponder.steps.model.data.Trek
import ponder.steps.model.data.TrekId

@Dao
interface TrekPointDao {

    @Query("SELECT tp.intentId, t.finishedAt FROM TrekPoint AS tp " +
            "LEFT JOIN TrekEntity AS t ON tp.trekId = t.id " +
            "WHERE tp.intentId IN (:intentIds) " +
            "AND tp.id = (SELECT MAX(id) FROM TrekPoint WHERE intentId = tp.intentId)")
    suspend fun readActiveTrekPoints(intentIds: List<IntentId>): List<ActiveTrekPoint>

    @Insert
    suspend fun createTrekPoint(trekPoint: TrekPoint)

    @Query("UPDATE TrekPoint SET trekId = :trekId WHERE id = :trekPointId")
    suspend fun updateTrekPointWithTrekId(trekPointId: TrekPointId, trekId: TrekId)
}

data class ActiveTrekPoint(
    val intentId: IntentId,
    val finishedAt: Instant?,
)