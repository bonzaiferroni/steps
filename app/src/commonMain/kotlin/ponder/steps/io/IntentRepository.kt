package ponder.steps.io

import ponder.steps.model.data.NewIntent

interface IntentRepository {
    suspend fun createIntent(intent: NewIntent)
}