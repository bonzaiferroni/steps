package ponder.steps.io

import kotlinx.datetime.Instant
import ponder.steps.appDb
import ponder.steps.db.PathStepDao
import ponder.steps.db.StepDao
import ponder.steps.db.toEntity
import ponder.steps.db.toStep
import ponder.steps.model.data.SyncData

class LocalSyncRepository(
    private val stepDao: StepDao = appDb.getStepDao(),
    private val pathStepDao: PathStepDao = appDb.getPathStepDao(),
): SyncRepository {

    override suspend fun readSync(lastSyncAt: Instant): SyncData {
        val steps = stepDao.readStepsUpdatedAfter(lastSyncAt).map { it.toStep() }
        val pathSteps = pathStepDao.readPathStepsByPathIds(steps.map { it.id })
        return SyncData(steps, pathSteps)
    }

    override suspend fun writeSync(data: SyncData): Int {
        val count = stepDao.upsert(*data.steps.map { it.toEntity() }.toTypedArray()).size
        pathStepDao.upsert(*data.pathSteps.map { it.toEntity() }.toTypedArray())
        return count
    }
}