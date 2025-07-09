package ponder.steps.server.db.tables

import kabinet.model.UserId
import kabinet.utils.nowToLocalDateTimeUtc
import kabinet.utils.toLocalDateTimeUtc
import klutch.db.tables.UserTable
import klutch.utils.fromStringId
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.statements.UpsertStatement
import ponder.steps.model.data.Step

internal object StepTable : UUIDTable("step") {
    val userId = reference("user_id", UserTable.id, onDelete = ReferenceOption.CASCADE)
    val label = text("label")
    val description = text("description").nullable()
    val theme = text("theme").nullable()
    val expectedMins = integer("expected_mins").nullable()
    val imgUrl = text("img_url").nullable()
    val thumbUrl = text("thumb_url").nullable()
    val audioLabelUrl = text("audio_label_url").nullable()
    val audioFullUrl = text("audio_full_url").nullable()
    val isPublic = bool("is_public").default(false)
    val pathSize = integer("path_size")
    val updatedAt = datetime("updated_at")
    val createdAt = datetime("created_at")
    val syncAt = datetime("sync_at").default(Clock.nowToLocalDateTimeUtc())
}