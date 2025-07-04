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
import ponder.steps.model.data.PathStep

object PathStepTable: UUIDTable("path_step") {
    val pathId = reference("path_id", StepTable.id, ReferenceOption.CASCADE)
    val stepId = reference("step_id", StepTable.id, ReferenceOption.CASCADE)
    val userId = reference("user_id", UserTable.id, ReferenceOption.CASCADE)
    val position = integer("position")
    val updatedAt = datetime("updated_at")
    val syncAt = datetime("sync_at").default(Clock.nowToLocalDateTimeUtc())

    init {
        uniqueIndex(pathId, position)
    }
}

fun ResultRow.toPathStep() = PathStep(
    id = this[PathStepTable.id].value.toStringId(),
    stepId = this[PathStepTable.stepId].value.toStringId(),
    pathId = this[PathStepTable.pathId].value.toStringId(),
    position = this[PathStepTable.position],
    updatedAt = this[PathStepTable.updatedAt].toInstantFromUtc()
)

fun syncPathStep(userId: String, syncAt: Instant): BatchUpsertStatement.(PathStep) -> Unit = { pathStep ->
    this[PathStepTable.id] = pathStep.id.fromStringId()
    this[PathStepTable.userId] = userId.fromStringId()
    this[PathStepTable.stepId] = pathStep.stepId.fromStringId()
    this[PathStepTable.pathId] = pathStep.pathId.fromStringId()
    this[PathStepTable.position] = pathStep.position
    this[PathStepTable.updatedAt] = pathStep.updatedAt.toLocalDateTimeUtc()
    this[PathStepTable.syncAt] = syncAt.toLocalDateTimeUtc()
}