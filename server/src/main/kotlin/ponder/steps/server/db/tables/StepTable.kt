package ponder.steps.server.db.tables

import klutch.db.tables.UserTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

internal object StepTable : UUIDTable("step") {
    val userId = reference("user_id", UserTable.id, onDelete = ReferenceOption.CASCADE)
    val label = text("label")
    val description = text("description").nullable()
    val theme = text("theme").nullable()
    val imgUrl = text("img_url").nullable()
    val thumbUrl = text("thumb_url").nullable()
    val shortAudioUrl = text("short_audio_url").nullable()
    val longAudioUrl = text("long_audio_url").nullable()
    val isPublic = bool("is_public").default(false)
    val expectedMins = integer("expected_mins").nullable()
    val pathSize = integer("path_size")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}
