package ponder.steps.io

import ponder.steps.model.Api
import ponder.steps.model.data.Intent
import ponder.steps.model.data.NewIntent
import pondui.io.ApiStore

class IntentApiStore: ApiStore() {
    suspend fun readIntent(intentId: String) = client.get(Api.Intents, intentId)
    suspend fun readUserIntents() = client.get(Api.Intents.User)
    suspend fun createIntent(newIntent: NewIntent) = client.post(Api.Intents.Create, newIntent)
    suspend fun updateIntent(intent: Intent) = client.update(Api.Intents.Update, intent)
    suspend fun deleteIntent(intentId: String) = client.delete(Api.Intents.Delete, intentId)
}
