package ponder.steps.io

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ponder.steps.model.data.StepLog
import ponder.steps.model.data.StepOutcome

interface StepLogRepository {
    /**
     * Get a step log by its ID
     * @param stepLogId The ID of the step log to retrieve
     * @return The step log, or null if not found
     */
    suspend fun readStepLog(stepLogId: String): StepLog?

    /**
     * Get a flow of a step log by its ID
     * @param stepLogId The ID of the step log to retrieve
     * @return Flow of the step log
     */
    fun flowStepLog(stepLogId: String): Flow<StepLog>

    /**
     * Get all step logs for a step
     * @param stepId The ID of the step
     * @return List of step logs for the step
     */
    suspend fun readStepLogsByStepId(stepId: String): List<StepLog>

    /**
     * Get all step logs with a specific outcome
     * @param outcome The outcome to filter by
     * @return List of step logs with the specified outcome
     */
    suspend fun readStepLogsByOutcome(outcome: StepOutcome): List<StepLog>

    /**
     * Get all step logs in a time range
     * @param startTime The start of the time range
     * @param endTime The end of the time range
     * @return List of step logs in the time range
     */
    suspend fun readStepLogsInTimeRange(startTime: Instant, endTime: Instant): List<StepLog>

    suspend fun createStepLog(stepLog: StepLog): String

    suspend fun deleteTrekStepLog(trekId: String, stepId: String, pathStepId: String?): Boolean

    /**
     * Get a flow of all step logs for a trek
     * @param trekId The ID of the trek
     * @return Flow of list of step logs for the trek
     */
    fun flowPathLogsByTrekId(trekId: String): Flow<List<StepLog>>

    fun flowRootLogs(start: Instant, end: Instant): Flow<List<StepLog>>
}
