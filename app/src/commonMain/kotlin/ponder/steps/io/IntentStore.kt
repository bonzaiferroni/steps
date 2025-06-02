package ponder.steps.io

import ponder.steps.appDb
import ponder.steps.db.IntentDao
import ponder.steps.model.data.NewIntent
import kabinet.utils.randomUuidString
import kotlinx.datetime.Clock
import ponder.steps.appUserId
import ponder.steps.db.IntentEntity

class IntentStore(
    private val dao: IntentDao = appDb.getIntentDao(),
) {
    fun readActiveIntentsFlow() = dao.readActiveIntentsFlow()

    suspend fun createIntent(intent: NewIntent) {
        val id = randomUuidString()
        dao.create(
            IntentEntity(
                id = id,
                userId = appUserId,
                rootId = intent.rootId,
                label = intent.label,
                repeatMins = intent.repeatMins,
                expectedMins = intent.expectedMins,
                completedAt = null,
                scheduledAt = intent.scheduledAt,
                pathIds = emptyList()
            )
        )
    }
}