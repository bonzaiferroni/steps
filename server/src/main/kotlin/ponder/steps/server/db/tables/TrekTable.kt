package ponder.steps.server.db.tables

import klutch.db.tables.UserTable
import klutch.utils.toInstantUtc
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import ponder.steps.model.data.Trek

internal object TrekTable : LongIdTable("trek") {
    val userId = reference("user_id", UserTable.id, ReferenceOption.CASCADE)
    val intentId = reference("quest_id", IntentTable.id, ReferenceOption.CASCADE)
    val rootId = reference("root_id", StepTable.id, ReferenceOption.CASCADE)
    val pathId = reference("path_id", StepTable.id, ReferenceOption.CASCADE).nullable()
    val positionId = reference("position_id", StepTable.id, ReferenceOption.CASCADE)
    val stepIndex = integer("step_index")
    val stepCount = integer("step_count")
    val availableAt = datetime("available_at")
    val startedAt = datetime("started_at")
    val progressAt = datetime("progress_at")
    val finishedAt = datetime("finished_at").nullable()
    val expectedAt = datetime("expected_at").nullable()
}

fun ResultRow.toTrek() = Trek(
    id = this[TrekTable.id].value,
    userId = this[TrekTable.userId].value,
    rootId = this[TrekTable.rootId].value,
    intentId = this[TrekTable.intentId].value,
    pathId = this[TrekTable.pathId]?.value,
    positionId = this[TrekTable.positionId].value,
    position = this[TrekTable.stepIndex],
    stepCount = this[TrekTable.stepCount],
    availableAt = this[TrekTable.availableAt].toInstantUtc(),
    startedAt = this[TrekTable.startedAt].toInstantUtc(),
    progressAt = this[TrekTable.progressAt].toInstantUtc(),
    finishedAt = this[TrekTable.finishedAt]?.toInstantUtc(),
    expectedAt = this[TrekTable.expectedAt]?.toInstantUtc()
)