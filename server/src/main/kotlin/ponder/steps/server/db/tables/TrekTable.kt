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
    val stepId = reference("step_id", StepTable.id, ReferenceOption.CASCADE)
    val stepIndex = integer("step_index")
    val stepCount = integer("step_count")
    val pathIds = array<String>("path_ids")
    val breadCrumbs = array<String>("bread_crumbs")
    val availableAt = datetime("available_at")
    val startedAt = datetime("started_at").nullable()
    val progressAt = datetime("progress_at").nullable()
    val finishedAt = datetime("finished_at").nullable()
    val expectedAt = datetime("expected_at").nullable()
}

fun ResultRow.toTrek() = Trek(
    id = this[TrekTable.id].value.toString(),
    userId = this[TrekTable.userId].value.toString(),
    rootId = this[TrekTable.rootId].value.toString(),
    intentId = this[TrekTable.intentId].value.toString(),
    stepId = this[TrekTable.stepId].value.toString(),
    stepIndex = this[TrekTable.stepIndex],
    stepCount = this[TrekTable.stepCount],
    pathIds = this[TrekTable.pathIds].toList(),
    breadCrumbs = this[TrekTable.breadCrumbs].toList(),
    availableAt = this[TrekTable.availableAt].toInstantUtc(),
    startedAt = this[TrekTable.startedAt]?.toInstantUtc(),
    progressAt = this[TrekTable.progressAt]?.toInstantUtc(),
    finishedAt = this[TrekTable.finishedAt]?.toInstantUtc(),
    expectedAt = this[TrekTable.expectedAt]?.toInstantUtc()
)