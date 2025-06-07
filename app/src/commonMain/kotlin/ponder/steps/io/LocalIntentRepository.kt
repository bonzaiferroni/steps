package ponder.steps.io

import ponder.steps.appDb
import ponder.steps.db.IntentDao
import ponder.steps.model.data.NewIntent
import kabinet.utils.randomUuidStringId
import ponder.steps.appUserId
import ponder.steps.db.IntentEntity

class LocalIntentRepository(
    private val dao: IntentDao = appDb.getIntentDao(),
): IntentRepository {
    fun readActiveIntentsFlow() = dao.readActiveIntentsFlow()

    override suspend fun createIntent(intent: NewIntent) {
        val id = randomUuidStringId()
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