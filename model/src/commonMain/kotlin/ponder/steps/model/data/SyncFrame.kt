package ponder.steps.model.data

import kabinet.utils.nameOrError
import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlin.reflect.KClass
import kabinet.utils.toSnakeCase

@Serializable
data class SyncPacket(
    val id: String,
    val origin: String,
    val lastSyncAt: Instant,
    val records: List<SyncRecord>
): SyncFrame

@Serializable
data class SyncHandshake(
    val origin: String,
    val lastSyncAt: Instant,
): SyncFrame

@Serializable
data class SyncReceipt(
    val id: String,
    val isSuccess: Boolean,
): SyncFrame

sealed interface SyncRecord {
    val id: String
    val updatedAt: Instant
}
sealed interface SyncFrame

val module = SerializersModule {
    polymorphic(SyncRecord::class) {
        subclass(StepLog::class, StepLog.serializer())
        subclass(Step::class, Step.serializer())
        subclass(Deletion::class, Deletion.serializer())
        subclass(PathStep::class, PathStep.serializer())
        subclass(Question::class, Question.serializer())
        subclass(Intent::class, Intent.serializer())
        subclass(Trek::class, Trek.serializer())
        subclass(Answer::class, Answer.serializer())
        subclass(Tag::class, Tag.serializer())
        subclass(StepTag::class, StepTag.serializer())
    }
    polymorphic(SyncFrame::class) {
        subclass(SyncPacket::class, SyncPacket.serializer())
        subclass(SyncHandshake::class, SyncHandshake.serializer())
        subclass(SyncReceipt::class, SyncReceipt.serializer())
    }
}

enum class SyncType(val kClass: KClass<*>) {
    StepRecord(Step::class),
    PathStepRecord(PathStep::class),
    QuestionRecord(Question::class),
    IntentRecord(Intent::class),
    TrekRecord(Trek::class),
    StepLogRecord(StepLog::class),
    AnswerRecord(Answer::class),
    TagRecord(Tag::class),
    StepTagRecord(StepTag::class);
    // DeletionRecord(Deletion::class, Deletion.serializer()),

    val className get() = kClass.nameOrError
    val entityName get() = "${kClass.nameOrError}Entity"
    val snakeName get() = kClass.nameOrError.toSnakeCase()

    companion object {
        fun fromClassName(className: String) = entries.firstOrNull() { it.className == className }
            ?: error("Invalid entity name")

        fun fromClass(kClass: KClass<*>) = entries.firstOrNull { it.kClass == kClass }
            ?: error("Invalid class")
    }
}

@OptIn(ExperimentalSerializationApi::class)
val cbor = Cbor {
    this.serializersModule = module
}

@OptIn(ExperimentalSerializationApi::class)
fun SyncFrame.toBytes() = cbor.encodeToByteArray(this)

@OptIn(ExperimentalSerializationApi::class)
fun ByteArray.toSyncFrameOrNull() = try {
    cbor.decodeFromByteArray<SyncFrame>(this)
} catch (e: Exception) {
    println("cbor error: ${e.message}")
    null
}

const val SYNC_SERVER_ORIGIN_LABEL = "Server"