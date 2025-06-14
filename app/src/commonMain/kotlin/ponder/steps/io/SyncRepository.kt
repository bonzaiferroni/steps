package ponder.steps.io

import kotlinx.datetime.Instant
import ponder.steps.model.data.SyncData

interface SyncRepository {
    /**
     * Synchronize steps with the server.
     *
     * @param syncStartAt The start time of the synchronization period
     * @param syncEndAt The end time of the synchronization period
     * @return A SyncResponse containing the results of the synchronization
     */
    suspend fun readSync(syncStartAt: Instant, syncEndAt: Instant): SyncData

    /**
     * Write synchronization data to the server.
     *
     * @param data The SyncData to write
     * @return The number of steps written
     */
    suspend fun writeSync(data: SyncData): Boolean
}