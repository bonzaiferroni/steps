package ponder.steps.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ponder.steps.model.data.Intent
import ponder.steps.model.data.Trek
import ponder.steps.model.data.TrekItem

@Dao
interface TrekDao {

    @Insert
    suspend fun create(trek: TrekEntity)

    @Update
    suspend fun update(vararg treks: TrekEntity): Int

    @Delete
    suspend fun delete(trek: TrekEntity): Int

    @Query("SELECT * FROM TrekEntity WHERE id = :trekId")
    suspend fun readTrekById(trekId: String): Trek?

    @Query(
        "SELECT t.id trekId, t.progress, t.availableAt, t.startedAt, t.finishedAt, " +
                "s.id stepId, s.label stepLabel, s.pathSize stepPathSize, s.imgUrl stepImgUrl, s.thumbUrl stepThumbUrl, " +
                "s.audioLabelUrl stepAudioLabelUrl, s.audioFullUrl stepAudioFullUrl, " +
                "s.description stepDescription, " +
                "i.label intentLabel, i.expectedMins, i.priority intentPriority " +
                "FROM TrekEntity AS t " +
                "JOIN StepEntity AS s on t.nextId = s.id " +
                "JOIN IntentEntity AS i on t.intentId = i.id " +
                "WHERE availableAt > :start AND availableAt < :end"
    )
    fun flowTreksInRange(start: Instant, end: Instant): Flow<List<TrekItem>>

    @Query("SELECT intentId FROM TrekEntity WHERE finishedAt IS NULL")
    suspend fun readActiveTrekIntentIds(): List<String>

    @Query(
        "SELECT availableAt FROM TrekEntity " +
                "WHERE intentId = :intentId " +
                "ORDER BY availableAt DESC " +
                "LIMIT 1"
    )
    suspend fun readLastAvailableAt(intentId: String): Instant?

    @Query("SELECT finishedAt IS NOT NULL AS isFinished FROM TrekEntity WHERE id = :trekId")
    suspend fun isFinished(trekId: String): Boolean
}