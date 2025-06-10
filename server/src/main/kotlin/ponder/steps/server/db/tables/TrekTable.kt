package ponder.steps.server.db.tables

import kabinet.utils.toInstantFromUtc
import klutch.db.tables.UserTable
import klutch.utils.toStringId
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import ponder.steps.model.data.Trek

internal object TrekTable : UUIDTable("trek") {
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
    id = this[TrekTable.id].value.toStringId(),
    userId = this[TrekTable.userId].value.toStringId(),
    rootId = this[TrekTable.rootId].value.toStringId(),
    intentId = this[TrekTable.intentId].value.toStringId(),
    stepId = this[TrekTable.stepId].value.toStringId(),
    stepIndex = this[TrekTable.stepIndex],
    stepCount = this[TrekTable.stepCount],
    pathIds = this[TrekTable.pathIds].toList(),
    breadCrumbs = this[TrekTable.breadCrumbs].toList(),
    availableAt = this[TrekTable.availableAt].toInstantFromUtc(),
    startedAt = this[TrekTable.startedAt]?.toInstantFromUtc(),
    progressAt = this[TrekTable.progressAt]?.toInstantFromUtc(),
    finishedAt = this[TrekTable.finishedAt]?.toInstantFromUtc(),
    expectedAt = this[TrekTable.expectedAt]?.toInstantFromUtc()
)