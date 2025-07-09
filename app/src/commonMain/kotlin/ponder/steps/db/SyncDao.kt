package ponder.steps.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.datetime.Instant
import ponder.steps.model.data.*

@Dao
interface SyncDao {

    // SyncRecord 🔄
    @Query("SELECT * FROM SyncLog")
    suspend fun readSyncLog(): SyncLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(syncLog: SyncLog)

    // Step 🦶
    @Query("SELECT * FROM StepEntity WHERE updatedAt > :start")
    suspend fun readUpdatedSteps(start: Instant): List<Step>

    @Query("SELECT * FROM StepEntity WHERE id = :stepId")
    suspend fun readStepById(stepId: StepId): Step?

    @Insert
    suspend fun insertStep(step: StepEntity)

    @Update
    suspend fun updateStep(step: StepEntity)

    @Query("DELETE FROM StepEntity WHERE id = :stepId")
    suspend fun deleteStepById(stepId: StepId)

    // StepLog 🦶🪵
    @Query("SELECT * FROM StepLogEntity WHERE updatedAt > :start")
    suspend fun readUpdatedStepLogs(start: Instant): List<StepLog>

    @Query("SELECT * FROM StepLogEntity WHERE id = :stepLogId")
    suspend fun readStepLogById(stepLogId: StepLogId): StepLog?

    @Insert
    suspend fun insertStepLog(stepLog: StepLogEntity)

    @Update
    suspend fun updateStepLog(stepLog: StepLogEntity)

    @Query("DELETE FROM StepLogEntity WHERE id = :stepLogId")
    suspend fun deleteStepLogById(stepLogId: StepLogId)

    // PathStep 🛣️
    @Query("SELECT * FROM PathStepEntity WHERE updatedAt > :start")
    suspend fun readUpdatedPathSteps(start: Instant): List<PathStep>

    @Query("SELECT * FROM PathStepEntity WHERE id = :pathStepId")
    suspend fun readPathStepById(pathStepId: PathStepId): PathStep?

    @Insert
    suspend fun insertPathStep(pathStep: PathStepEntity)

    @Update
    suspend fun updatePathStep(pathStep: PathStepEntity)

    @Query("DELETE FROM PathStepEntity WHERE id = :pathStepId")
    suspend fun deletePathStepById(pathStepId: PathStepId)

    // Question ❓
    @Query("SELECT * FROM QuestionEntity WHERE updatedAt > :start")
    suspend fun readUpdatedQuestions(start: Instant): List<Question>

    @Query("SELECT * FROM QuestionEntity WHERE id = :questionId")
    suspend fun readQuestionById(questionId: QuestionId): Question?

    @Insert
    suspend fun insertQuestion(question: QuestionEntity)

    @Update
    suspend fun updateQuestion(question: QuestionEntity)

    @Query("DELETE FROM QuestionEntity WHERE id = :questionId")
    suspend fun deleteQuestionById(questionId: QuestionId)

    // Intent 🎯
    @Query("SELECT * FROM IntentEntity WHERE updatedAt > :start")
    suspend fun readUpdatedIntents(start: Instant): List<Intent>

    @Query("SELECT * FROM IntentEntity WHERE id = :intentId")
    suspend fun readIntentById(intentId: IntentId): Intent?

    @Insert
    suspend fun insertIntent(intent: IntentEntity)

    @Update
    suspend fun updateIntent(intent: IntentEntity)

    @Query("DELETE FROM IntentEntity WHERE id = :intentId")
    suspend fun deleteIntentById(intentId: IntentId)

    // Trek 🧗
    @Query("SELECT * FROM TrekEntity WHERE updatedAt > :start")
    suspend fun readUpdatedTreks(start: Instant): List<Trek>

    @Query("SELECT * FROM TrekEntity WHERE id = :trekId")
    suspend fun readTrekById(trekId: TrekId): Trek?

    @Query("SELECT * FROM TrekEntity WHERE intentId = :intentId")
    suspend fun readTrekByIntentId(intentId: IntentId): Trek?

    @Insert
    suspend fun insertTrek(trek: TrekEntity)

    @Update
    suspend fun updateTrek(trek: TrekEntity)

    @Query("DELETE FROM TrekEntity WHERE id = :trekId")
    suspend fun deleteTrekById(trekId: TrekId)

    // Answer ✅
    @Query("SELECT * FROM AnswerEntity WHERE updatedAt > :start")
    suspend fun readUpdatedAnswers(start: Instant): List<Answer>

    @Query("SELECT * FROM AnswerEntity WHERE id = :answerId")
    suspend fun readAnswerById(answerId: AnswerId): Answer?

    @Insert
    suspend fun insertAnswer(answer: AnswerEntity)

    @Update
    suspend fun updateAnswer(answer: AnswerEntity)

    @Query("DELETE FROM AnswerEntity WHERE id = :answerId")
    suspend fun deleteAnswerById(answerId: AnswerId)

    // Tag 🏷️
    @Query("SELECT * FROM TagEntity WHERE updatedAt > :start")
    suspend fun readUpdatedTags(start: Instant): List<Tag>

    @Query("SELECT * FROM TagEntity WHERE id = :tagId")
    suspend fun readTagById(tagId: TagId): Tag?

    @Insert
    suspend fun insertTag(tag: TagEntity)

    @Update
    suspend fun updateTag(tag: TagEntity)

    @Query("DELETE FROM TagEntity WHERE id = :tagId")
    suspend fun deleteTagById(tagId: TagId)

    // StepTag 🦶🏷️
    @Query("SELECT * FROM StepTagEntity WHERE updatedAt > :start")
    suspend fun readUpdatedStepTags(start: Instant): List<StepTag>

    @Query("SELECT * FROM StepTagEntity WHERE id = :stepTagId")
    suspend fun readStepTagById(stepTagId: StepTagId): StepTag?

    @Insert
    suspend fun insertStepTag(stepTag: StepTagEntity)

    @Update
    suspend fun updateStepTag(stepTag: StepTagEntity)

    @Query("DELETE FROM StepTagEntity WHERE id = :stepTagId")
    suspend fun deleteStepTagById(stepTagId: StepTagId)

    // Deletion 🗑️
    @Query("SELECT * FROM DeletionEntity")
    suspend fun readDeletions(): List<Deletion>

}
