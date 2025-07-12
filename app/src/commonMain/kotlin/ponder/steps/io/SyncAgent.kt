package ponder.steps.io

import kabinet.PAST_MOMENT
import kabinet.utils.nameOrError
import kabinet.utils.randomUuidStringId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import ponder.steps.db.AppDatabase
import ponder.steps.db.DeletionEntity
import ponder.steps.db.StepLogEntity
import ponder.steps.db.SyncDao
import ponder.steps.db.SyncLog
import ponder.steps.db.TrekPoint
import ponder.steps.db.toEntity
import ponder.steps.model.data.*
import kotlin.reflect.KClass

class SyncAgent(
    private val origin: String,
    private val db: AppDatabase,
    private val syncDao: SyncDao = db.getSyncDao(),
    private val socket: SyncSocket = SyncSocket(origin)
) {
    private var lastSyncAt: Instant = PAST_MOMENT
    private val sentPackets = Channel<SyncPacket>(Channel.RENDEZVOUS)

    companion object {
        var syncInProgress = false
            private set
    }

    fun startSync() {
        CoroutineScope(Dispatchers.IO).launch {
            val syncLog = syncDao.readSyncLog() ?: SyncLog(lastSyncAt = PAST_MOMENT)
            lastSyncAt = syncLog.lastSyncAt

            socket.startSync(coroutineScope = this, lastSyncAt = lastSyncAt, syncFlow = sentPackets.receiveAsFlow())

            emitDeletions()
            sendPacket()

            launch {
                socket.receivedFrames.collect { frame ->
                    when (frame) {
                        is SyncHandshake -> error("client never learned how to shake hands")
                        is SyncPacket -> syncPackets(frame)
                        is SyncReceipt -> checkReceipt(frame, syncLog)
                    }
                }
            }
        }
    }

    // frame depot 📤 📥
    private val frameIds = mutableMapOf<String, Instant>()

    private suspend fun sendPacket(records: List<SyncRecord>, updatedAt: Instant) {
        val id = randomUuidStringId()
        frameIds[id] = updatedAt
        val packet = SyncPacket(id, origin, updatedAt, records)
        sentPackets.send(packet)
    }

    private suspend fun checkReceipt(frame: SyncReceipt, syncLog: SyncLog) {
        val updatedAt = frameIds.remove(frame.id) ?: error("Took frame receipt with unknown id")
        if (frameIds.isEmpty()) {
            println("sync complete")
            syncDao.upsert(syncLog.copy(lastSyncAt = updatedAt))
        }
    }

    private suspend fun syncPackets(frame: SyncPacket) {
        syncInProgress = true
        for (record in frame.records) {
            integrateRecord(frame.origin, record)
        }
        syncInProgress = false
    }

    private fun CoroutineScope.sendPacket() {
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

        val insertTrekPointIfMissing = suspend {
            if (syncDao.readTrekPointIdByTrekId(remote.id) == null) {
                println("TrekPoint created by remote trek")
                syncDao.insertTrekPoint(TrekPoint(intentId = remote.intentId, trekId = remote.id))
            }
        }

        if (remote.isComplete) {
            writeRemoteTrek()
            insertTrekPointIfMissing()
            return
        }

        writeRemoteTrek()
        val trekPoint = syncDao.readActiveTrekPointByIntentId(remote.intentId)
        if (trekPoint != null) {
            println("Local TrekPoint absorbs remote trek")
            syncDao.updateTrekPoint(trekPoint.copy(trekId = remote.id))
        } else {
            insertTrekPointIfMissing()
        }
    }

    private fun <Data : SyncRecord> CoroutineScope.emitUpdatedRecords(
        recordClass: KClass<Data>,
        readUpdates: suspend (Instant) -> List<Data>,
    ) = launch {
        var lastSeen = lastSyncAt
        val tableName = SyncType.fromClass(recordClass).entityName
        db.invalidationTracker.createFlow(tableName).collect {
            delay(100)
            val records = readUpdates(lastSeen).filter {
                val isRemoteId = remoteIds.contains(it.id)
                if (isRemoteId) remoteIds.remove(it.id)
                !isRemoteId
            }
            if (records.isNotEmpty()) {
                val latestUpdatedAt = records.maxOf { it.updatedAt }
                // println("emitting ${records.size} ${recordClass.nameOrError}")
                sendPacket(records, latestUpdatedAt)
                lastSeen = maxOf(lastSeen, latestUpdatedAt)
            }
        }
    }

    // Deletions 🗑️
    private val remoteIds = mutableSetOf<DeletionId>()

    private fun CoroutineScope.emitDeletions() = launch {
        db.invalidationTracker.createFlow(DeletionEntity::class.nameOrError).collect {
            delay(100)
            val deletions = syncDao.readDeletions().filter {
                val isRemoteDeletion = remoteIds.contains(it.recordId)
                remoteIds.remove(it.recordId)
                !isRemoteDeletion
            }
            if (deletions.isNotEmpty()) {
                val updatedAt = deletions.maxOf { it.deletedAt }
                // println("emitting ${deletions.size} deletions")
                sendPacket(deletions, updatedAt)
            }
        }
    }

    suspend fun integrateDeletion(deletion: Deletion) {
        remoteIds.add(deletion.recordId)
        // println("receiving deletion")
        val syncType = SyncType.fromClassName(deletion.entity)
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

private suspend fun <Data : SyncRecord, Entity> integrateRecord(
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
