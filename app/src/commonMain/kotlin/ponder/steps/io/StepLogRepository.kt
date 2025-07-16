package ponder.steps.io

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ponder.steps.db.StepId
import ponder.steps.db.TimeUnit
import ponder.steps.model.data.CountBucket
import ponder.steps.model.data.StepLog
import ponder.steps.model.data.StepStatus

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
    suspend fun readStepLogsByOutcome(outcome: StepStatus): List<StepLog>

    /**
     * Get all step logs in a time range
     * @param startTime The start of the time range
     * @param endTime The end of the time range
     * @return List of step logs in the time range
     */
    suspend fun readStepLogsInTimeRange(startTime: Instant, endTime: Instant): List<StepLog>

    suspend fun createStepLog(stepLog: StepLog): String

    suspend fun deleteTrekStepLog(trekId: String, stepId: String, pathStepId: String?): Boolean

    fun flowStepLogsByStepId(stepId: StepId): Flow<List<StepLog>>

    suspend fun readLogCountsByStepId(stepId: StepId, startAt: Instant, interval: TimeUnit): List<CountBucket>

    suspend fun readEarliestLogTimeByStepId(stepId: StepId): Instant
}
