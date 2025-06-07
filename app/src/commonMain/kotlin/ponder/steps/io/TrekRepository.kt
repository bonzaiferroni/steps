package ponder.steps.io

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ponder.steps.model.data.TrekItem

interface TrekRepository {

    fun flowTreksSince(time: Instant): Flow<List<TrekItem>>

    suspend fun syncTreksWithIntents()
}