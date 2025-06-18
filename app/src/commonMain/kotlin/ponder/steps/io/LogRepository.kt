package ponder.steps.io

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ponder.steps.model.data.StepLog
import ponder.steps.model.data.StepOutcome

interface LogRepository {
    /**
     * Get a log entry by its ID
     * @param logEntryId The ID of the log entry to retrieve
     * @return The log entry, or null if not found
     */
    suspend fun readStepLog(logEntryId: String): StepLog?
    
    /**
     * Get a flow of a log entry by its ID
     * @param logEntryId The ID of the log entry to retrieve
     * @return Flow of the log entry
     */
    fun flowLogEntry(logEntryId: String): Flow<StepLog>
    
    /**
     * Get all log entries for a step
     * @param stepId The ID of the step
     * @return List of log entries for the step
     */
    suspend fun readLogEntriesByStepId(stepId: String): List<StepLog>
    
    /**
     * Get all log entries for a trek
     * @param trekId The ID of the trek
     * @return List of log entries for the trek
     */
    suspend fun readLogEntriesByTrekId(trekId: String): List<StepLog>
    
    /**
     * Get a flow of all log entries for a trek
     * @param trekId The ID of the trek
     * @return Flow of list of log entries for the trek
     */
    fun flowStepLogsByTrekId(trekId: String): Flow<List<StepLog>>
    
    /**
     * Get all log entries with a specific outcome
     * @param outcome The outcome to filter by
     * @return List of log entries with the specified outcome
     */
    suspend fun readLogEntriesByOutcome(outcome: StepOutcome): List<StepLog>
    
    /**
     * Get all log entries in a time range
     * @param startTime The start of the time range
     * @param endTime The end of the time range
     * @return List of log entries in the time range
     */
    suspend fun readLogEntriesInTimeRange(startTime: Instant, endTime: Instant): List<StepLog>

    suspend fun createStepLog(stepLog: StepLog): String

    suspend fun deleteTrekStepLog(stepId: String, trekId: String, pathStepId: String?): Boolean
}