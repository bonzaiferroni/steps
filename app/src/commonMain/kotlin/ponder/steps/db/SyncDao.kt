package ponder.steps.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.datetime.Instant
import ponder.steps.model.data.PathStep

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

    @Query("DELETE FROM StepEntity WHERE id IN (:deletions)")
    suspend fun deleteStepsInList(deletions: List<String>)

    @Query("DELETE FROM PathStepEntity WHERE id IN (:deletions)")
    suspend fun deletePathStepsInList(deletions: List<String>)

    @Query("DELETE FROM DeletionEntity WHERE id IN (:deletions)")
    suspend fun deleteDeletionsInList(deletions: List<String>)

    @Query("DELETE FROM DeletionEntity WHERE :syncEndAt > recordedAt")
    suspend fun deleteDeletionsBefore(syncEndAt: Instant)

    @Query("DELETE FROM SyncRecord")
    suspend fun deleteAllSyncRecords()

    @Query("SELECT * FROM StepEntity WHERE updatedAt > :startSyncAt AND :endSyncAt >= updatedAt")
    suspend fun readStepsUpdated(startSyncAt: Instant, endSyncAt: Instant): List<StepEntity>

    @Query("SELECT * FROM PathStepEntity WHERE updatedAt > :syncStartAt AND :syncEndAt > updatedAt")
    suspend fun readPathStepsUpdated(syncStartAt: Instant, syncEndAt: Instant): List<PathStep>

    @Query("SELECT id FROM DeletionEntity WHERE :syncAt > recordedAt")
    suspend fun readAllDeletionsBefore(syncAt: Instant): List<String>

    @Query("SELECT * FROM SyncRecord ORDER BY endSyncAt DESC LIMIT 1")
    suspend fun readLastSync(): SyncRecord?
}