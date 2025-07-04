package ponder.steps.io

import androidx.sqlite.SQLiteException
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import ponder.steps.appDb
import ponder.steps.appOrigin
import ponder.steps.db.SyncDao
import ponder.steps.db.SyncRecord
import ponder.steps.db.toEntity
import ponder.steps.db.toStep
import ponder.steps.model.data.StepLog
import ponder.steps.model.data.SyncData
import ponder.steps.model.data.Trek

class LocalSyncRepository(
    private val dao: SyncDao = appDb.getSyncDao(),
): SyncRepository {

    suspend fun readSyncStartAt() = dao.readLastSync()?.endSyncAt ?: defaultStartSyncAt

    suspend fun logSync(startSyncAt: Instant, endSyncAt: Instant) {
        dao.deleteAllSyncRecords()
        dao.insert(SyncRecord(startSyncAt = startSyncAt, endSyncAt = endSyncAt))
    }

    override suspend fun readSync(syncStartAt: Instant, syncEndAt: Instant): SyncData {
        val deletions = dao.readAllDeletionsBefore(syncEndAt).toSet()
        val steps = dao.readStepsUpdated(syncStartAt, syncEndAt).map { it.toStep() }
        val pathSteps = dao.readPathStepsUpdated(syncStartAt, syncEndAt)
        val questions = dao.readQuestionsUpdated(syncStartAt, syncEndAt)
        val intents = dao.readIntentsUpdated(syncStartAt, syncEndAt)
        val treks = dao.readTreksUpdated(syncStartAt, syncEndAt)
        val stepLogs = dao.readStepLogsUpdated(syncStartAt, syncEndAt)
        val answers = dao.readAnswersUpdated(syncStartAt, syncEndAt)
        val tags = dao.readTagsUpdated(syncStartAt, syncEndAt)
        val stepTags = dao.readStepTagsUpdated(syncStartAt, syncEndAt)

        return SyncData(
            origin = appOrigin,
            startSyncAt = syncStartAt,
            endSyncAt = syncEndAt,
            deletions = deletions,
            steps = steps,
            pathSteps = pathSteps,
            questions = questions,
            intents = intents,
            treks = treks,
            stepLogs = stepLogs,
            answers = answers,
            tags = tags,
            stepTags = stepTags
        )
    }

    @Suppress("DuplicatedCode")
    override suspend fun writeSync(data: SyncData): Boolean {
        val deletions = data.deletions.toList()

        // handle updates
        dao.upsert(*data.steps.map { it.toEntity() }.toTypedArray())
        dao.upsert(*data.pathSteps.map { it.toEntity() }.toTypedArray())
        dao.upsert(*data.questions.map { it.toEntity() }.toTypedArray())
        dao.upsert(*data.intents.map { it.toEntity() }.toTypedArray())
        val stepLogs = writeTreks(data.treks, data.stepLogs)
        try {
            dao.upsert(*stepLogs.map { it.toEntity() }.toTypedArray())
        } catch (e: SQLiteException) {
            println("failed writing ${stepLogs.size} logs: ${e.message}")
        }
        dao.upsert(*data.answers.map { it.toEntity() }.toTypedArray())
        dao.upsert(*data.tags.map { it.toEntity() }.toTypedArray())
        dao.upsert(*data.stepTags.map { it.toEntity() }.toTypedArray())

        // handle deletions
        dao.deleteStepsInList(deletions)
        dao.deletePathStepsInList(deletions)
        dao.deleteDeletionsInList(deletions)
        dao.deleteQuestionsInList(deletions)
        dao.deleteIntentsInList(deletions)
        dao.deleteTreksInList(deletions)
        dao.deleteStepLogsInList(deletions)
        dao.deleteAnswersInList(deletions)
        dao.deleteTagsInList(deletions)
        dao.deleteStepTagsInList(deletions)
        return true
    }

    suspend fun cleanSync(endSyncAt: Instant): Boolean {
        dao.deleteDeletionsBefore(endSyncAt)
        return true
    }

    suspend fun writeTreks(treks: List<Trek>, stepLogs: List<StepLog>): List<StepLog> {
        var stepLogs = stepLogs
        for (remoteTrek in treks) {
            if (remoteTrek.isComplete) {
                dao.upsert(remoteTrek.toEntity())
                continue
            }
            val localTrek = dao.readActiveTrekByIntentId(remoteTrek.intentId)
            if (localTrek == null) {
                dao.upsert(remoteTrek.toEntity())
                continue
            }
            if (localTrek.createdAt < remoteTrek.createdAt) {
                println("trek conflict resolved using local data")
                // associate incoming steplogs with local active trek
                stepLogs = stepLogs.map { if (it.trekId == remoteTrek.id) it.copy(trekId = localTrek.id) else it }
            } else {
                println("trek conflict resolved using remote data")
                dao.upsert(remoteTrek.toEntity())
                dao.replaceStepLogTrekId(localTrek.id, remoteTrek.id)
                dao.delete(localTrek.toEntity())
            }
        }
        return stepLogs
    }
}

private val defaultStartSyncAt = LocalDate.parse("2017-03-20").atStartOfDayIn(TimeZone.UTC)