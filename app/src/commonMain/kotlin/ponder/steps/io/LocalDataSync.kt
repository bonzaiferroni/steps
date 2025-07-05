package ponder.steps.io

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import ponder.steps.db.AppDatabase
import ponder.steps.db.toEntity
import ponder.steps.model.data.StepLog
import ponder.steps.model.data.SyncPacket
import ponder.steps.model.data.SyncRecord
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

class LocalDataSync(
    private val origin: String,
    private val db: AppDatabase,
) {
    private val syncDao = db.getSyncDao()

    private var lastSyncAt: Instant = Instant.DISTANT_PAST

    private val _sentPackets = MutableSharedFlow<SyncPacket>()
    val sentPackets: SharedFlow<SyncPacket> = _sentPackets

    private val socket = SyncSocket(sentPackets)

    init {
        CoroutineScope(Dispatchers.IO).launch {
            lastSyncAt = syncDao.readLastSync()?.endSyncAt ?: defaultStartSyncAt

            syncTarget(
                recordClass = StepLog::class,
                readUpdates = syncDao::readLogsUpdatedSince,
                provideUpdatedAt = { it.updatedAt },
                provideId = { it.id },
                provideEntity = StepLog::toEntity,
                readRecord = syncDao::readStepLogById,
                insertEntity = syncDao::insertStepLog,
                updateEntity = syncDao::updateStepLog,
            )
        }
    }

    private fun <Data: SyncRecord, Entity> CoroutineScope.syncTarget(
        recordClass: KClass<Data>,
        readUpdates: suspend (Instant) -> List<Data>,
        provideUpdatedAt: (Data) -> Instant,
        provideId: (Data) -> String,
        provideEntity: (Data) -> Entity,
        readRecord: suspend (String) -> Data?,
        insertEntity: suspend (Entity) -> Unit,
        updateEntity: suspend (Entity) -> Unit,
    ) {
        launch {
            var lastSeen = lastSyncAt
            val tableName = "${recordClass.simpleName!!}Entity"
            db.invalidationTracker.createFlow(tableName, emitInitialState = false).collect {
                // query only the fresh rows since lastSeen
                val records = readUpdates(lastSeen)
                if (records.isNotEmpty()) {
                    val latestUpdatedAt = records.maxOf { provideUpdatedAt(it) }
                    val packet = SyncPacket(origin, latestUpdatedAt, records)
                    _sentPackets.emit(packet)
                    // bump the watermark
                    lastSeen = maxOf(lastSeen, latestUpdatedAt)
                }
            }
        }

        launch {
            socket.receivedPackets.collect { packet ->
                for (record in packet.records) {
                    val record = recordClass.safeCast(record) ?: continue
                    val existingRecord = readRecord(provideId(record))
                    if (existingRecord == null) {
                        insertEntity(provideEntity(record))
                    } else if (provideUpdatedAt(existingRecord) < provideUpdatedAt(record)) {
                        updateEntity(provideEntity(record))
                    }
                }
            }
        }
    }
}