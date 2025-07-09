package ponder.steps.server.db.tables

import kabinet.utils.nowToLocalDateTimeUtc
import kabinet.utils.toInstantFromUtc
import kabinet.utils.toLocalDateTimeUtc
import klutch.db.tables.UserTable
import klutch.utils.fromStringId
import klutch.utils.toStringId
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.statements.BatchUpsertStatement
import org.jetbrains.exposed.sql.statements.UpsertStatement
import ponder.steps.model.data.Trek

internal object TrekTable : UUIDTable("trek") {
    val userId = reference("user_id", UserTable.id, ReferenceOption.CASCADE)
    val intentId = reference("intent_id", IntentTable.id, ReferenceOption.CASCADE)
    val rootId = reference("root_id", StepTable.id, ReferenceOption.CASCADE)
    val isComplete = bool("is_complete")
    val createdAt = datetime("created_at")
    val progressAt = datetime("progress_at").nullable()
    val finishedAt = datetime("finished_at").nullable()
    val expectedAt = datetime("expected_at").nullable()
    val updatedAt = datetime("updated_at")
    val syncAt = datetime("sync_at").default(Clock.nowToLocalDateTimeUtc())
}

fun ResultRow.toTrek() = Trek(
    id = this[TrekTable.id].value.toStringId(),
    userId = this[TrekTable.userId].value.toStringId(),
    rootId = this[TrekTable.rootId].value.toStringId(),
    intentId = this[TrekTable.intentId].value.toStringId(),
    isComplete = this[TrekTable.isComplete],
    createdAt = this[TrekTable.createdAt].toInstantFromUtc(),
    finishedAt = this[TrekTable.finishedAt]?.toInstantFromUtc(),
    expectedAt = this[TrekTable.expectedAt]?.toInstantFromUtc(),
    updatedAt = this[TrekTable.updatedAt].toInstantFromUtc(),
)
