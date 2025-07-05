package ponder.steps.model.data

import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
data class SyncPacket(
    val origin: String,
    val updateAt: Instant,
    val records: List<SyncRecord>
)

sealed interface SyncRecord {
}

val module = SerializersModule {
    polymorphic(SyncRecord::class) {
        subclass(StepLog::class, StepLog.serializer())
        subclass(Step::class, Step.serializer())
    }
}

@OptIn(ExperimentalSerializationApi::class)
val cbor = Cbor {
    this.serializersModule = module
}

@OptIn(ExperimentalSerializationApi::class)
fun SyncPacket.toBytes() = cbor.encodeToByteArray(this)

@OptIn(ExperimentalSerializationApi::class)
fun ByteArray.toSyncPacketOrNull() = try {
    cbor.decodeFromByteArray<SyncPacket>(this)
} catch (e: Exception) {
    println("cbor error: ${e.message}")
    null
}

//data class FullFrame(
//    override val origin: String,
//    override val updateAt: Instant,
//    val deletions: List<Deletion>,
//    val steps: List<Step>,
//    val pathSteps: List<PathStep>,
//    val questions: List<Question>,
//    val intents: List<Intent>,
//    val treks: List<Trek>,
//    val stepLogs: List<StepLog>,
//    val answers: List<Answer>,
//    val tags: List<Tag>,
//    val stepTags: List<StepTag>
//): SyncFrame
//
//data class StepLogFrame(
//    override val origin: String,
//    override val updateAt: Instant,
//    val stepLogs: List<StepLog>,
//): SyncFrame