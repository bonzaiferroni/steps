package ponder.steps.io

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ponder.steps.model.data.TrekItem

interface TrekRepository {

    /**
     * Create a flow of all treks since a given time.
     * @param time The Instant since which to fetch treks
     * @return Flow of List of TrekItem
     */
    fun flowTreksSince(time: Instant): Flow<List<TrekItem>>

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