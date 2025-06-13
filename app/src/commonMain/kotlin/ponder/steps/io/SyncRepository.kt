package ponder.steps.io

import kotlinx.datetime.Instant
import ponder.steps.model.data.SyncData

interface SyncRepository {
    /**
     * Synchronize steps with the server.
     *
     * @param lastSyncAt The timestamp of the last synchronization
     * @return A SyncResponse containing the results of the synchronization
     */
    suspend fun readSync(lastSyncAt: Instant): SyncData

    /**
     * Write synchronization data to the server.
     *
     * @param data The SyncData to write
     * @return The number of steps written
     */
    suspend fun writeSync(data: SyncData): Int
}