package ponder.steps.io

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ponder.steps.model.data.SyncData
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class DataMerger(
    private val localRepo: LocalSyncRepository,
    private val remoteRepo: SyncRepository,
    private val interval: Duration = 10.seconds
) {
    fun init(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            while (true) {
                try {
                    val startSyncAt = localRepo.readSyncStartAt()
                    val endSyncAt = Clock.System.now()

                    val localData = localRepo.readSync(startSyncAt, endSyncAt)
                    val remoteData = remoteRepo.readSync(startSyncAt, endSyncAt)

                    logData("sending", localData)
                    logData("receiving", remoteData)

                    if (!localData.isEmpty) {
                        val isSuccess = remoteRepo.writeSync(resolveConflicts(localData, remoteData))
                        if (!isSuccess) error("error writing remote data")
                    }

                    if (!remoteData.isEmpty) {
                        val isSuccess = localRepo.writeSync(resolveConflicts(remoteData, localData))
                        if (!isSuccess) error("error writing remote data")
                    }

                    localRepo.logSync(startSyncAt, endSyncAt)
                } catch (e: Exception) {
                    println("Error with sync: ${e.message}\n${e.stackTraceToString()}")
                }

                delay(interval)
            }
        }
    }
}

private fun logData(label: String, data: SyncData) {
    if (data.isEmpty) return
    println(label)
    print("deletions: ${data.deletions.size}")
    print(", steps: ${data.steps.size}")
    print(", pathSteps: ${data.pathSteps.size}")
    println(", questions: ${data.questions.size}")
}

private fun resolveConflicts(incoming: SyncData, outgoing: SyncData) = incoming.copy(
    steps = resolveConflicts(incoming.steps, outgoing.steps, outgoing.deletions) {
        UpdatedItem(it.id, it.updatedAt)
    },
    pathSteps = resolveConflicts(incoming.pathSteps, outgoing.pathSteps, outgoing.deletions) {
        UpdatedItem(it.id, it.updatedAt)
    },
    questions = resolveConflicts(incoming.questions, outgoing.questions, outgoing.deletions) {
        UpdatedItem(it.id, it.updatedAt)
    }
)

private fun <T> resolveConflicts(
    incoming: List<T>,
    outgoing: List<T>,
    deletions: Set<String>,
    toItem: (T) -> UpdatedItem
) = incoming.filter { incomingItem ->
    val (incomingId, incomingUpdatedAt) = toItem(incomingItem)
    !deletions.contains(incomingId) && outgoing.all { outgoingItem ->
        val (outgoingId, outgoingUpdatedAt) = toItem(outgoingItem)
        incomingId != outgoingId || incomingUpdatedAt > outgoingUpdatedAt
    }
}

private data class UpdatedItem(
    val id: String,
    val updatedAt: Instant
)