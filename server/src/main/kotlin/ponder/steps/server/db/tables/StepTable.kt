package ponder.steps.server.db.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import ponder.steps.model.data.Step
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal object StepTable : UUIDTable("step") {
    val label = text("label")
}
