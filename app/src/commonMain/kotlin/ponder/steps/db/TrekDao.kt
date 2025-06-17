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
import ponder.steps.model.data.TrekStep

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

    @Query("SELECT * FROM TrekEntity WHERE id = :trekId")
    fun flowTrekById(trekId: String): Flow<Trek>

    @Query(
        "SELECT t.id trekId, t.progress, t.availableAt, t.startedAt, t.finishedAt, t.pathStepId, " +
                "s.id stepId, s.label stepLabel, s.pathSize, s.imgUrl, s.thumbUrl, " +
                "s.audioLabelUrl, s.audioFullUrl, " +
                "s.description, " +
                "i.label intentLabel, i.expectedMins, i.priority " +
                "FROM TrekEntity AS t " +
                "JOIN StepEntity AS s ON t.nextId = s.id " +
                "JOIN IntentEntity AS i ON t.intentId = i.id " +
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

    @Query("SELECT * FROM TrekEntity WHERE superId = :superId")
    fun flowSubTreks(superId: String): Flow<List<Trek>>

    @Query("SELECT * FROM TrekEntity WHERE superId IS NULL AND availableAt > :start AND availableAt < :end")
    fun flowAvailableRootTreks(start: Instant, end: Instant): Flow<List<Trek>>

    @Query(
        "SELECT s.label stepLabel, s.pathSize, s.imgUrl, s.thumbUrl, s.description, s.audioLabelUrl, s.audioFullUrl, " +
                "s.id stepId, " +
                "t.id trekId, t.progress, t.pathStepId, t.availableAt, t.startedAt, t.finishedAt, t.pathStepId, t.superId, " +
                "i.label intentLabel, i.priority, i.expectedMins intentMins " +
                "FROM TrekEntity AS t " +
                "JOIN StepEntity AS s ON t.rootId = s.id " +
                "JOIN IntentEntity AS i ON t.intentId = i.id " +
                "WHERE t.id = :trekId"
    )
    fun flowTrekStepById(trekId: String): Flow<TrekStep>

    @Query(
        "SELECT s.label stepLabel, s.pathSize, s.imgUrl, s.thumbUrl, s.description, s.audioLabelUrl, s.audioFullUrl, " +
                "s.id stepId, " +
                "t.id trekId, t.progress, t.pathStepId, t.availableAt, t.startedAt, t.finishedAt, t.pathStepId, " +
                "i.label intentLabel, i.priority, i.expectedMins intentMins " +
                "FROM TrekEntity AS t " +
                "JOIN StepEntity AS s ON t.rootId = s.id " +
                "JOIN IntentEntity AS i ON t.intentId = i.id " +
                "WHERE t.superId IS NULL AND availableAt > :start AND availableAt < :end"
    )
    fun flowRootTrekSteps(start: Instant, end: Instant): Flow<List<TrekStep>>

    @Query(
        "SELECT s.label stepLabel, s.pathSize, s.imgUrl, s.thumbUrl, s.description, s.audioLabelUrl, s.audioFullUrl, " +
                "s.id stepId, " +
                "t.id trekId, t.progress, t.availableAt, t.startedAt, t.finishedAt, " +
                "p.position, p.id pathStepId, " +
                "st.id superId " +
                "FROM TrekEntity AS st " +
                "JOIN PathStepEntity AS p ON st.rootId = p.pathId " +
                "JOIN StepEntity AS s ON p.stepId = s.id " +
                "LEFT JOIN TrekEntity AS t ON p.id = t.pathStepId AND st.id = t.superId " +
                "WHERE st.id = :superId"
    )
    fun flowTrekStepsBySuperId(superId: String): Flow<List<TrekStep>>
}