package ponder.steps.io

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ponder.steps.model.data.Intent
import ponder.steps.model.data.TrekItem

interface TrekRepository {

    /**
     * Create a flow of all treks since a given time.
     * @param start The start time to filter treks
     * @param end The end time to filter treks
     * @return Flow of List of TrekItem
     */
    fun flowTreksInRange(start: Instant, end: Instant): Flow<List<TrekItem>>

    /**
     * Create new treks for active intents without a corresponding trek
     */
    suspend fun syncTreksWithIntents()

    /**
     * Complete the current step of the trek
     * @param trekId The id of the trek to complete the step for
     * @return True if the step was completed successfully, false otherwise
     */
    suspend fun completeStep(trekId: String): Boolean
}