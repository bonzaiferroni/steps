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
import ponder.steps.model.data.Intent
import ponder.steps.model.data.IntentPriority
import ponder.steps.model.data.IntentTiming

internal object IntentTable: UUIDTable("intent") {
    val userId = reference("user_id", UserTable.id, onDelete = ReferenceOption.CASCADE)
    val rootId = reference("root_step_id", StepTable.id, onDelete = ReferenceOption.CASCADE)
    val label = text("label")
    val repeatMins = integer("repeat_mins").nullable()
    val expectedMins = integer("expected_mins").nullable()
    val priority = enumeration<IntentPriority>("priority").default(IntentPriority.Default)
    val timing = enumeration<IntentTiming>("timing")
    val pathIds = array<String>("path_ids")
    val completedAt = datetime("completed_at").nullable()
    val scheduledAt = datetime("scheduled_at").nullable()
    val updatedAt = datetime("updated_at")
}

fun ResultRow.toIntent() = Intent(
    id = this[IntentTable.id].value.toStringId(),
    userId = this[IntentTable.userId].value.toStringId(),
    rootId = this[IntentTable.rootId].value.toStringId(),
    label = this[IntentTable.label],
    repeatMins = this[IntentTable.repeatMins],
    expectedMins = this[IntentTable.expectedMins],
    priority = this[IntentTable.priority],
    timing = this[IntentTable.timing],
    pathIds = this[IntentTable.pathIds],
    completedAt = this[IntentTable.completedAt]?.toInstantFromUtc(),
    scheduledAt = this[IntentTable.scheduledAt]?.toInstantFromUtc(),
    updatedAt = this[IntentTable.updatedAt].toInstantFromUtc(),
)

fun upsertIntent(userId: String): BatchUpsertStatement.(Intent) -> Unit = { intent ->
    this[IntentTable.id] = intent.id.fromStringId()
    this[IntentTable.userId] = userId.fromStringId()
    this[IntentTable.rootId] = intent.rootId.fromStringId()
    this[IntentTable.label] = intent.label
    this[IntentTable.repeatMins] = intent.repeatMins
    this[IntentTable.expectedMins] = intent.expectedMins
    this[IntentTable.priority] = intent.priority
    this[IntentTable.timing] = intent.timing
    this[IntentTable.pathIds] = intent.pathIds
    this[IntentTable.completedAt] = intent.completedAt?.toLocalDateTimeUtc()
    this[IntentTable.scheduledAt] = intent.scheduledAt?.toLocalDateTimeUtc()
    this[IntentTable.updatedAt] = intent.updatedAt.toLocalDateTimeUtc()
}