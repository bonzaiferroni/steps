package ponder.steps.server.db.tables

import kabinet.utils.toInstantFromUtc
import kabinet.utils.toLocalDateTimeUtc
import klutch.db.tables.UserTable
import klutch.utils.fromStringId
import klutch.utils.toStringId
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.statements.BatchUpsertStatement
import ponder.steps.model.data.Trek

internal object TrekTable : UUIDTable("trek") {
    val userId = reference("user_id", UserTable.id, ReferenceOption.CASCADE)
    val intentId = reference("intent_id", IntentTable.id, ReferenceOption.CASCADE)
    val rootId = reference("root_id", StepTable.id, ReferenceOption.CASCADE)
    val isComplete = bool("is_complete")
    val startedAt = datetime("started_at").nullable()
    val progressAt = datetime("progress_at").nullable()
    val finishedAt = datetime("finished_at").nullable()
    val expectedAt = datetime("expected_at").nullable()
    val updatedAt = datetime("updated_at")
}

fun ResultRow.toTrek() = Trek(
    id = this[TrekTable.id].value.toStringId(),
    userId = this[TrekTable.userId].value.toStringId(),
    rootId = this[TrekTable.rootId].value.toStringId(),
    intentId = this[TrekTable.intentId].value.toStringId(),
    isComplete = this[TrekTable.isComplete],
    startedAt = this[TrekTable.startedAt]?.toInstantFromUtc(),
    finishedAt = this[TrekTable.finishedAt]?.toInstantFromUtc(),
    expectedAt = this[TrekTable.expectedAt]?.toInstantFromUtc(),
    updatedAt = this[TrekTable.updatedAt].toInstantFromUtc(),
)

fun upsertTrek(userId: String): BatchUpsertStatement.(Trek) -> Unit = { trek ->
    this[TrekTable.id] = trek.id.fromStringId()
    this[TrekTable.userId] = userId.fromStringId()
    this[TrekTable.rootId] = trek.rootId.fromStringId()
    this[TrekTable.intentId] = trek.intentId.fromStringId()
    this[TrekTable.isComplete] = trek.isComplete
    this[TrekTable.startedAt] = trek.startedAt?.toLocalDateTimeUtc()
    this[TrekTable.finishedAt] = trek.finishedAt?.toLocalDateTimeUtc()
    this[TrekTable.expectedAt] = trek.expectedAt?.toLocalDateTimeUtc()
    this[TrekTable.updatedAt] = trek.updatedAt.toLocalDateTimeUtc()
}