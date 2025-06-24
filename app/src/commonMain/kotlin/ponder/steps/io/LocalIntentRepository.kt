package ponder.steps.io

import ponder.steps.appDb
import ponder.steps.db.IntentDao
import ponder.steps.model.data.NewIntent
import kabinet.utils.randomUuidStringId
import ponder.steps.appUserId
import ponder.steps.db.IntentEntity
import ponder.steps.db.StepDao
import ponder.steps.model.data.IntentId

class LocalIntentRepository(
    private val intentDao: IntentDao = appDb.getIntentDao(),
): IntentRepository {
    fun readActiveIntentsFlow() = intentDao.readActiveIntentsFlow()

    override suspend fun createIntent(intent: NewIntent) {
        val id = randomUuidStringId()
        intentDao.create(
            IntentEntity(
                id = id,
                userId = appUserId,
                rootId = intent.rootId,
                label = intent.label,
                repeatMins = intent.repeatMins,
                expectedMins = intent.expectedMins,
                priority = intent.priority,
                timing = intent.timing,
                completedAt = null,
                scheduledAt = intent.scheduledAt,
                pathIds = intent.pathIds,
            )
        )
    }

    override suspend fun deleteIntent(intentId: IntentId) = intentDao.deleteIntent(intentId) == 1
}