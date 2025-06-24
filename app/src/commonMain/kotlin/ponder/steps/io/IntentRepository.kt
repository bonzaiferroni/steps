package ponder.steps.io

import ponder.steps.model.data.IntentId
import ponder.steps.model.data.NewIntent

interface IntentRepository {
    suspend fun createIntent(intent: NewIntent)

    suspend fun deleteIntent(intentId: IntentId): Boolean
}