package ponder.steps.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ponder.steps.model.data.IntentId
import ponder.steps.model.data.PathStepId
import ponder.steps.model.data.Trek
import ponder.steps.model.data.TrekId

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

    @Query("SELECT intentId FROM TrekEntity WHERE NOT isComplete")
    suspend fun readActiveTrekIntentIds(): List<String>

    @Query(
        "SELECT finishedAt FROM TrekEntity " +
                "WHERE intentId = :intentId " +
                "ORDER BY finishedAt DESC " +
                "LIMIT 1"
    )
    suspend fun readLastFinishedAt(intentId: String): Instant?

    @Query("SELECT finishedAt IS NOT NULL AS isFinished FROM TrekEntity WHERE id = :trekId")
    suspend fun isFinished(trekId: String): Boolean

    @Query(
        "SELECT t1.intentId, t1.finishedAt FROM TrekEntity t1 " +
                "WHERE t1.intentId IN (:intentIds) " +
                "AND t1.finishedAt = (SELECT MAX(t2.finishedAt) FROM TrekEntity t2 WHERE t2.intentId = t1.intentId)"
    )
    suspend fun readTrekFinishedAt(intentIds: List<IntentId>): List<TrekFinishedAt>

    @Query(
        "SELECT t1.* FROM TrekEntity t1 " +
                "WHERE t1.intentId IN (:intentIds) " +
                "AND t1.startedAt = (SELECT MAX(t2.startedAt) FROM TrekEntity t2 WHERE t2.intentId = t1.intentId)"
    )
    suspend fun readTreksLastStartedAt(intentIds: List<IntentId>): List<Trek>

    @Query("SELECT id FROM TrekEntity WHERE intentId = :intentId AND NOT isComplete")
    suspend fun readActiveTrekId(intentId: IntentId): TrekId?

    @Query("SELECT t.id trekId, t.startedAt, s.* FROM TrekEntity AS t " +
            "JOIN StepEntity AS s ON s.id = t.rootId " +
            "WHERE t.startedAt >= :start OR NOT t.isComplete ")
    fun flowRootTodoSteps(start: Instant): Flow<List<TodoStep>>

    @Query("SELECT * FROM TrekEntity WHERE startedAt > :start OR NOT isComplete")
    fun flowActiveTreks(start: Instant): Flow<List<Trek>>

    @Query("SELECT COUNT(*) cnt, t.id trekId FROM TrekEntity AS t " +
            "JOIN PathStepEntity AS ps ON t.rootId = ps.pathId " +
            "JOIN StepLogEntity AS l ON l.pathStepId = ps.id AND l.trekId = t.id " +
            "WHERE t.startedAt > :start OR NOT t.isComplete " +
            "GROUP BY trekId ")
    fun flowRootProgresses(start: Instant): Flow<Map<@MapColumn("trekId") TrekId, @MapColumn("cnt") Int>>

    @Query("SELECT COUNT(*) cnt, ps1.id pathStepId FROM PathStepEntity AS ps1 " +
            "JOIN PathStepEntity AS ps2 ON ps1.stepId = ps2.pathId " +
            "JOIN StepLogEntity AS l on ps2.id = l.pathStepId " +
            "WHERE ps1.pathId = :pathId AND l.trekId = :trekId " +
            "GROUP BY ps1.id")
    fun flowPathProgresses(pathId: StepId, trekId: TrekId): Flow<Map<@MapColumn("pathStepId") PathStepId, @MapColumn("cnt") Int>>
}

data class TrekFinishedAt(
    val intentId: IntentId,
    val finishedAt: Instant?
)