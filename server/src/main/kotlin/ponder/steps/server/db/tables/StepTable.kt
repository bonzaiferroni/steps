package ponder.steps.server.db.tables

import klutch.db.tables.UserTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import ponder.steps.model.data.Step
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal object StepTable : LongIdTable("step") {
    val userId = reference("user_id", UserTable.id, onDelete = ReferenceOption.CASCADE)
    val label = text("label")
    val description = text("description").nullable()
    val imgUrl = text("img_url").nullable()
    val thumbUrl = text("thumb_url").nullable()
    val audioUrl = text("audio_url").nullable()
    val isPublic = bool("is_public").default(false)
    val expectedMins = integer("expected_mins")
    val createdAt = datetime("created_at")
    val editAt = datetime("edit_at").nullable()
}
