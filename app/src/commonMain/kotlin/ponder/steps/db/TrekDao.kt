package ponder.steps.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ponder.steps.model.data.Trek
import ponder.steps.model.data.TrekItem

@Dao
interface TrekDao {

    @Query("SELECT * FROM TrekEntity WHERE id = :trekId")
    suspend fun readTrekById(trekId: String): Trek?

    @Query(
        "SELECT t.id trekId, t.stepIndex, t.stepCount, t.availableAt, t.startedAt, t.finishedAt, " +
                "s.label stepLabel, s.pathSize stepPathSize, s.imgUrl stepImgUrl, s.thumbUrl stepThumbUrl, " +
                "i.label intentLabel, i.expectedMins " +
                "FROM TrekEntity AS t " +
                "JOIN StepEntity AS s on t.stepId = s.id " +
                "JOIN IntentEntity AS i on t.intentId = i.id " +
                "WHERE availableAt > :time"
    )
    fun flowTrekItemsSince(time: Instant): Flow<List<TrekItem>>

    @Query("SELECT intentId FROM TrekEntity WHERE finishedAt IS NULL")
    suspend fun readActiveTrekIntentIds(): List<String>

    @Query(
        "SELECT availableAt FROM TrekEntity " +
                "WHERE intentId = :intentId " +
                "ORDER BY availableAt DESC " +
                "LIMIT 1"
    )
    suspend fun readLastAvailableAt(intentId: String): Instant?

    @Insert
    suspend fun create(trek: TrekEntity)

    @Update
    suspend fun update(vararg treks: TrekEntity): Int

    @Delete
    suspend fun delete(trek: TrekEntity): Int
}