package ponder.steps.io

import kabinet.utils.randomUuidStringId
import kotlinx.datetime.Clock
import ponder.steps.appDb
import ponder.steps.db.StepId
import ponder.steps.db.StepTagDao
import ponder.steps.db.StepTagEntity
import ponder.steps.db.TagDao
import ponder.steps.db.TagEntity
import ponder.steps.model.data.TagId

class LocalTagRepository(
    val tagDao: TagDao = appDb.getTagDao(),
    val stepTagDao: StepTagDao = appDb.getStepTagDao(),
) {
    fun flowTagsByStepId(stepId: StepId) = tagDao.flowTagsByStepId(stepId)

    suspend fun addTag(stepId: StepId, label: String): Boolean {
        val tagId = tagDao.readTagIdByLabel(label) ?: randomUuidStringId().also {
            require(tagDao.insert(TagEntity(it, label, Clock.System.now())) > 0)
        }

        return stepTagDao.readStepTagId(stepId, tagId) != null || stepTagDao.insert(StepTagEntity(
            id = randomUuidStringId(),
            stepId = stepId,
            tagId = tagId,
            updatedAt = Clock.System.now()
        ))  > 0
    }

    suspend fun removeTag(stepId: StepId, tagId: TagId) = stepTagDao.deleteStepTag(stepId, tagId) == 1

    fun flowTopTagCounts(limit: Int = 10) = tagDao.flowTopTagCounts(limit)
}