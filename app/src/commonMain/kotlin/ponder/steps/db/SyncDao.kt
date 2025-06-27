package ponder.steps.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.datetime.Instant
import ponder.steps.model.data.Answer
import ponder.steps.model.data.Intent
import ponder.steps.model.data.PathStep
import ponder.steps.model.data.Question
import ponder.steps.model.data.StepLog
import ponder.steps.model.data.Trek

@Dao
interface SyncDao {

    @Delete
    suspend fun delete(syncRecord: SyncRecord)

    @Insert
    suspend fun insert(syncRecord: SyncRecord)

    @Upsert
    suspend fun upsert(vararg pathSteps: PathStepEntity): LongArray

    @Upsert
    suspend fun upsert(vararg steps: StepEntity): LongArray

    @Upsert
    suspend fun upsert(vararg questions: QuestionEntity): LongArray

    @Upsert
    suspend fun upsert(vararg intents: IntentEntity): LongArray

    @Upsert
    suspend fun upsert(vararg treks: TrekEntity): LongArray

    @Upsert
    suspend fun upsert(vararg stepLogs: StepLogEntity): LongArray

    @Upsert
    suspend fun upsert(vararg answers: AnswerEntity): LongArray

    @Query("DELETE FROM StepEntity WHERE id IN (:deletions)")
    suspend fun deleteStepsInList(deletions: List<String>)

    @Query("DELETE FROM PathStepEntity WHERE id IN (:deletions)")
    suspend fun deletePathStepsInList(deletions: List<String>)

    @Query("DELETE FROM QuestionEntity WHERE id IN (:deletions)")
    suspend fun deleteQuestionsInList(deletions: List<String>)

    @Query("DELETE FROM DeletionEntity WHERE id IN (:deletions)")
    suspend fun deleteDeletionsInList(deletions: List<String>)

    @Query("DELETE FROM IntentEntity WHERE id IN (:intents)")
    suspend fun deleteIntentsInList(intents: List<String>)

    @Query("DELETE FROM TrekEntity WHERE id IN (:treks)")
    suspend fun deleteTreksInList(treks: List<String>)

    @Query("DELETE FROM StepLogEntity WHERE id IN (:stepLogs)")
    suspend fun deleteStepLogsInList(stepLogs: List<String>)

    @Query("DELETE FROM AnswerEntity WHERE id IN (:answers)")
    suspend fun deleteAnswersInList(answers: List<String>)

    @Query("DELETE FROM DeletionEntity WHERE :syncEndAt > recordedAt")
    suspend fun deleteDeletionsBefore(syncEndAt: Instant)

    @Query("DELETE FROM SyncRecord")
    suspend fun deleteAllSyncRecords()

    @Query("SELECT * FROM StepEntity WHERE updatedAt > :startSyncAt AND :endSyncAt >= updatedAt")
    suspend fun readStepsUpdated(startSyncAt: Instant, endSyncAt: Instant): List<StepEntity>

    @Query("SELECT * FROM PathStepEntity WHERE updatedAt > :syncStartAt AND :syncEndAt > updatedAt")
    suspend fun readPathStepsUpdated(syncStartAt: Instant, syncEndAt: Instant): List<PathStep>

    @Query("SELECT * FROM QuestionEntity WHERE updatedAt > :syncStartAt AND :syncEndAt > updatedAt")
    suspend fun readQuestionsUpdated(syncStartAt: Instant, syncEndAt: Instant): List<Question>

    @Query("SELECT id FROM DeletionEntity WHERE :syncAt > recordedAt")
    suspend fun readAllDeletionsBefore(syncAt: Instant): List<String>

    @Query("SELECT * FROM SyncRecord ORDER BY endSyncAt DESC LIMIT 1")
    suspend fun readLastSync(): SyncRecord?

    @Query("SELECT * FROM IntentEntity WHERE updatedAt > :syncStartAt AND :syncEndAt > updatedAt")
    suspend fun readIntentsUpdated(syncStartAt: Instant, syncEndAt: Instant): List<Intent>

    @Query("SELECT * FROM TrekEntity WHERE updatedAt > :syncStartAt AND :syncEndAt > updatedAt")
    suspend fun readTreksUpdated(syncStartAt: Instant, syncEndAt: Instant): List<Trek>

    @Query("SELECT * FROM StepLogEntity WHERE updatedAt > :syncStartAt AND :syncEndAt > updatedAt")
    suspend fun readStepLogsUpdated(syncStartAt: Instant, syncEndAt: Instant): List<StepLog>

    @Query("SELECT * FROM AnswerEntity WHERE updatedAt > :syncStartAt AND :syncEndAt > updatedAt")
    suspend fun readAnswersUpdated(syncStartAt: Instant, syncEndAt: Instant): List<Answer>
}