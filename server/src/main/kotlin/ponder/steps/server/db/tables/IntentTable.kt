package ponder.steps.server.db.tables

import klutch.db.tables.UserTable
import klutch.utils.toInstantUtc
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
    // val isRegularTime = bool("is_regular_time").default(true)
    val completedAt = datetime("completed_at").nullable()
    val scheduledAt = datetime("scheduled_At").nullable()
}

internal object IntentPathTable: Table("intent_step") {
    val intentId = reference("intent_id", IntentTable.id, onDelete = ReferenceOption.CASCADE)
    val pathId = reference("path_id", StepTable.id, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(intentId, pathId, name = "PK_intent_step")
}

fun ResultRow.toIntent() = Intent(
    id = this[IntentTable.id].value.toString(),
    userId = this[IntentTable.userId].value.toString(),
    rootId = this[IntentTable.rootId].value.toString(),
    label = this[IntentTable.label],
    repeatMins = this[IntentTable.repeatMins],
    expectedMins = this[IntentTable.expectedMins],
    // isRegularTime = this[IntentTable.isRegularTime],
    completedAt = this[IntentTable.completedAt]?.toInstantUtc(),
    scheduledAt = this[IntentTable.scheduledAt]?.toInstantUtc(),
)