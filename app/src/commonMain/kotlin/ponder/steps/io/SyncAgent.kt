package ponder.steps.io

import kabinet.PAST_MOMENT
import kabinet.utils.nameOrError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ponder.steps.db.AppDatabase
import ponder.steps.db.DeletionEntity
import ponder.steps.db.StepLogEntity
import ponder.steps.db.SyncDao
import ponder.steps.db.SyncLog
import ponder.steps.db.toEntity
import ponder.steps.model.data.*
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.minutes

class SyncAgent(
    private val origin: String,
    private val db: AppDatabase,
    private val syncDao: SyncDao = db.getSyncDao(),
    private val socket: SyncSocket = SyncSocket(origin)
) {
    private var lastSyncAt: Instant = PAST_MOMENT
    private val sentPackets = MutableSharedFlow<SyncPacket>()

    companion object {
        var syncInProgress = false
            private set
    }

    fun startSync() {
        CoroutineScope(Dispatchers.IO).launch {
            var syncLog = syncDao.readSyncLog() ?: SyncLog(lastSyncAt = PAST_MOMENT)
            lastSyncAt = syncLog.lastSyncAt

            val syncJob = socket.startSync(coroutineScope = this, lastSyncAt = lastSyncAt, syncFlow = sentPackets)

            emitDeletions()
            emitRecords() 

            launch {
                socket.receivedPackets.collect { packet ->
                    syncInProgress = true
                    for (record in packet.records) {
                        integrateRecord(packet.origin, record)
                    }
                    syncInProgress = false
                }
            }

            val syncWindow = 1.minutes
            while (isActive && syncJob.isActive) {
                delay(syncWindow)
                if (syncJob.isActive) {
                    syncLog = syncLog.copy(lastSyncAt = Clock.System.now() - syncWindow)
                    syncDao.upsert(syncLog)
                } else {
                    println("Sync job disconnected")
                }
            }
        }
    }

    private fun CoroutineScope.emitRecords() {
        emitUpdatedRecords(StepLog::class, syncDao::readUpdatedStepLogs)
        emitUpdatedRecords(Step::class, syncDao::readUpdatedSteps)
        emitUpdatedRecords(PathStep::class, syncDao::readUpdatedPathSteps)
        emitUpdatedRecords(Question::class, syncDao::readUpdatedQuestions)
        emitUpdatedRecords(Intent::class, syncDao::readUpdatedIntents)
        emitUpdatedRecords(Trek::class, syncDao::readUpdatedTreks)
        emitUpdatedRecords(Answer::class, syncDao::readUpdatedAnswers)
        emitUpdatedRecords(Tag::class, syncDao::readUpdatedTags)
        emitUpdatedRecords(StepTag::class, syncDao::readUpdatedStepTags)
    }

    private suspend fun integrateRecord(origin: String, record: SyncRecord) {
        remoteIds.add(record.id)
        // println("record from $origin: ${record::class.nameOrError}")
        when (record) {
            is Deletion -> integrateDeletion(record)
            is Trek -> integrateTrek(record)
            is Answer -> integrateRecord(
                record = record,
                provideEntity = { it.toEntity(false) },
                readRecord = syncDao::readAnswerById,
                insertEntity = syncDao::insertAnswer,
                updateEntity = syncDao::updateAnswer,
            )
            is Intent -> integrateRecord(
                record = record,
                provideEntity = { it.toEntity(false) },
                readRecord = syncDao::readIntentById,
                insertEntity = syncDao::insertIntent,
                updateEntity = syncDao::updateIntent,
            )
            is PathStep -> integrateRecord(
                record = record,
                provideEntity = { it.toEntity(false) },
                readRecord = syncDao::readPathStepById,
                insertEntity = syncDao::insertPathStep,
                updateEntity = syncDao::updatePathStep,
            )
            is Question -> integrateRecord(
                record = record,
                provideEntity = { it.toEntity(false) },
                readRecord = syncDao::readQuestionById,
                insertEntity = syncDao::insertQuestion,
                updateEntity = syncDao::updateQuestion,
            )
            is Step -> integrateRecord(
                record = record,
                provideEntity = { it.toEntity(false) },
                readRecord = syncDao::readStepById,
                insertEntity = syncDao::insertStep,
                updateEntity = syncDao::updateStep,
            )
            is StepLog -> integrateRecord(
                record = record,
                provideEntity = { it.toEntity(false) },
                readRecord = syncDao::readStepLogById,
                insertEntity = ::insertStepLog,
                updateEntity = syncDao::updateStepLog,
            )
            is StepTag -> integrateRecord(
                record = record,
                provideEntity = { it.toEntity(false) },
                readRecord = syncDao::readStepTagById,
                insertEntity = syncDao::insertStepTag,
                updateEntity = syncDao::updateStepTag,
            )
            is Tag -> integrateRecord(
                record = record,
                provideEntity = { it.toEntity(false) },
                readRecord = syncDao::readTagById,
                insertEntity = syncDao::insertTag,
                updateEntity = syncDao::updateTag,
            )
        }
    }

    private suspend fun insertStepLog(stepLog: StepLogEntity) {
        val trekId = stepLog.trekId
        if (trekId == null) {
            syncDao.insertStepLog(stepLog)
            return
        }
        val isLocalTrek = syncDao.readTrekById(trekId) != null
        val stepLog = if (!isLocalTrek) stepLog.copy(trekId = null) else stepLog
        syncDao.insertStepLog(stepLog)
    }

    private suspend fun integrateTrek(remote: Trek) {
        val writeRemoteTrek = suspend {
            integrateRecord(
                record = remote,
                provideEntity = { remote.toEntity(false) },
                readRecord = syncDao::readTrekById,
                insertEntity = syncDao::insertTrek,
                updateEntity = syncDao::updateTrek
            )
        }

        if (remote.isComplete) {
            writeRemoteTrek()
            return
        }
        val local = syncDao.readTrekByIntentId(remote.intentId)
        if (local == null || local.id == remote.id) {
            writeRemoteTrek()
            return
        }
        val keepRemote = when {
            remote.createdAt < local.createdAt -> true
            remote.createdAt > local.createdAt -> false
            else -> remote.id < local.id   // string compare tiebreaker
        }
        if (keepRemote) {
            println("integrate remote trek")
            syncDao.deleteTrekById(local.id)
            writeRemoteTrek()
        } else {
            println("preserve local trek")
        }
    }

    private fun <Data: SyncRecord> CoroutineScope.emitUpdatedRecords(
        recordClass: KClass<Data>,
        readUpdates: suspend (Instant) -> List<Data>,
    ) = launch {
        var lastSeen = lastSyncAt
        val tableName = SyncType.fromClass(recordClass).entityName
        db.invalidationTracker.createFlow(tableName).collect {
            val records = readUpdates(lastSeen).filter {
                val isRemoteId = remoteIds.contains(it.id)
                if (isRemoteId) remoteIds.remove(it.id)
                !isRemoteId
            }
            if (records.isNotEmpty()) {
                // println("sending ${records.size} ${recordClass.nameOrError}")
                val latestUpdatedAt = records.maxOf { it.updatedAt }
                val packet = SyncPacket(origin, latestUpdatedAt, records)
                sentPackets.emit(packet)
                lastSeen = maxOf(lastSeen, latestUpdatedAt)
            }
        }
    }

    // Deletions 🗑️
    private val remoteIds = mutableSetOf<DeletionId>()

    private fun CoroutineScope.emitDeletions() = launch {
        db.invalidationTracker.createFlow(DeletionEntity::class.nameOrError).collect {
            val deletions = syncDao.readDeletions().filter {
                val isRemoteDeletion = remoteIds.contains(it.recordId)
                remoteIds.remove(it.recordId)
                !isRemoteDeletion
            }
            if (deletions.isNotEmpty()) {
                val packet = SyncPacket(origin, Clock.System.now(), deletions)
                // println("sending ${deletions.size} deletions")
                sentPackets.emit(packet)
            }
        }
    }

    suspend fun integrateDeletion(deletion: Deletion) {
        remoteIds.add(deletion.recordId)
        // println("receiving deletion")
        val syncType = SyncType.fromEntityName(deletion.entity)
        when (syncType) {
            SyncType.StepRecord -> syncDao.deleteStepById(deletion.recordId)
            SyncType.PathStepRecord -> syncDao.deletePathStepById(deletion.recordId)
            SyncType.QuestionRecord -> syncDao.deleteQuestionById(deletion.recordId)
            SyncType.IntentRecord -> syncDao.deleteIntentById(deletion.recordId)
            SyncType.TrekRecord -> syncDao.deleteTrekById(deletion.recordId)
            SyncType.StepLogRecord -> syncDao.deleteStepLogById(deletion.recordId)
            SyncType.AnswerRecord -> syncDao.deleteAnswerById(deletion.recordId)
            SyncType.TagRecord -> syncDao.deleteTagById(deletion.recordId)
            SyncType.StepTagRecord -> syncDao.deleteStepTagById(deletion.recordId)
        }
    }
}

private suspend fun <Data: SyncRecord, Entity> integrateRecord(
    record: Data,
    provideEntity: (Data) -> Entity,
    readRecord: suspend (String) -> Data?,
    insertEntity: suspend (Entity) -> Unit,
    updateEntity: suspend (Entity) -> Unit,
) {
    val existingRecord = readRecord(record.id)
    if (existingRecord == null) {
        insertEntity(provideEntity(record))
    } else if (existingRecord.updatedAt < record.updatedAt) {
        updateEntity(provideEntity(record))
    }
}
