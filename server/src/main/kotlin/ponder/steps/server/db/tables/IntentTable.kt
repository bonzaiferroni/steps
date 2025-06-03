package ponder.steps.server.db.tables

import kabinet.utils.toInstantUtc
import klutch.db.tables.UserTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import ponder.steps.model.data.Intent

internal object IntentTable: UUIDTable("intent") {
    val userId = reference("user_id", UserTable.id, onDelete = ReferenceOption.CASCADE)
    val rootId = reference("root_step_id", StepTable.id, onDelete = ReferenceOption.CASCADE)
    val label = text("label")
    val repeatMins = integer("repeat_mins").nullable()
    val expectedMins = integer("expected_mins").nullable()
    val pathIds = array<String>("path_ids")
    val completedAt = datetime("completed_at").nullable()
    val scheduledAt = datetime("scheduled_At").nullable()
}

fun ResultRow.toIntent() = Intent(
    id = this[IntentTable.id].value.toString(),
    userId = this[IntentTable.userId].value.toString(),
    rootId = this[IntentTable.rootId].value.toString(),
    label = this[IntentTable.label],
    repeatMins = this[IntentTable.repeatMins],
    expectedMins = this[IntentTable.expectedMins],
    pathIds = this[IntentTable.pathIds],
    completedAt = this[IntentTable.completedAt]?.toInstantUtc(),
    scheduledAt = this[IntentTable.scheduledAt]?.toInstantUtc(),
)