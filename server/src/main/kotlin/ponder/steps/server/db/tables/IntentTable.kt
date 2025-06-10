package ponder.steps.server.db.tables

import kabinet.utils.toInstantFromUtc
import klutch.db.tables.UserTable
import klutch.utils.toStringId
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import ponder.steps.model.data.Intent
import ponder.steps.model.data.IntentPriority

internal object IntentTable: UUIDTable("intent") {
    val userId = reference("user_id", UserTable.id, onDelete = ReferenceOption.CASCADE)
    val rootId = reference("root_step_id", StepTable.id, onDelete = ReferenceOption.CASCADE)
    val label = text("label")
    val repeatMins = integer("repeat_mins").nullable()
    val expectedMins = integer("expected_mins").nullable()
    val priority = enumeration<IntentPriority>("priority").default(IntentPriority.Default)
    val pathIds = array<String>("path_ids")
    val completedAt = datetime("completed_at").nullable()
    val scheduledAt = datetime("scheduled_At").nullable()
}

fun ResultRow.toIntent() = Intent(
    id = this[IntentTable.id].value.toStringId(),
    userId = this[IntentTable.userId].value.toStringId(),
    rootId = this[IntentTable.rootId].value.toStringId(),
    label = this[IntentTable.label],
    repeatMins = this[IntentTable.repeatMins],
    expectedMins = this[IntentTable.expectedMins],
    priority = this[IntentTable.priority],
    pathIds = this[IntentTable.pathIds],
    completedAt = this[IntentTable.completedAt]?.toInstantFromUtc(),
    scheduledAt = this[IntentTable.scheduledAt]?.toInstantFromUtc(),
)