package ponder.steps.server.db.tables

import kabinet.utils.nowToLocalDateTimeUtc
import kabinet.utils.toInstantFromUtc
import kabinet.utils.toLocalDateTimeUtc
import klutch.db.tables.UserTable
import klutch.utils.fromStringId
import klutch.utils.toStringId
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.statements.BatchUpsertStatement
import org.jetbrains.exposed.sql.statements.UpsertStatement
import ponder.steps.model.data.StepLog
import ponder.steps.model.data.StepOutcome
import kotlin.time.Duration.Companion.days

object StepLogTable : UUIDTable("step_log") {
    val userId = reference("user_id", UserTable.id, onDelete = ReferenceOption.CASCADE)
    val stepId = reference("step_id", StepTable.id, onDelete = ReferenceOption.CASCADE)
    val trekId = reference("trek_id", TrekTable.id, onDelete = ReferenceOption.CASCADE).nullable()
    val pathStepId = reference("path_step_id", PathStepTable.id, onDelete = ReferenceOption.CASCADE).nullable()
    val outcome = enumeration<StepOutcome>("outcome")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
    val syncAt = datetime("sync_at").default(Clock.nowToLocalDateTimeUtc())
}

fun ResultRow.toStepLog() = StepLog(
    id = this[StepLogTable.id].value.toStringId(),
    stepId = this[StepLogTable.stepId].value.toStringId(),
    trekId = this[StepLogTable.trekId]?.value?.toStringId(),
    pathStepId = this[StepLogTable.pathStepId]?.value?.toStringId(),
    outcome = this[StepLogTable.outcome],
    createdAt = this[StepLogTable.createdAt].toInstantFromUtc(),
    updatedAt = this[StepLogTable.updatedAt].toInstantFromUtc()
)

val defaultLocalDateTime = (Instant.DISTANT_PAST + 1.days).toLocalDateTime(TimeZone.UTC)
