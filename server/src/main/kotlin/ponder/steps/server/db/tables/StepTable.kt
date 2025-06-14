package ponder.steps.server.db.tables

import kabinet.utils.toLocalDateTimeUtc
import klutch.db.tables.UserTable
import klutch.utils.fromStringId
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.statements.BatchUpsertStatement
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
}

fun upsertStep(userId: String): BatchUpsertStatement.(Step) -> Unit = { step ->
    this[StepTable.id] = step.id.fromStringId()
    this[StepTable.userId] = userId.fromStringId()
    this[StepTable.label] = step.label
    this[StepTable.description] = step.description
    this[StepTable.theme] = step.theme
    this[StepTable.expectedMins] = step.expectedMins
    this[StepTable.imgUrl] = step.imgUrl
    this[StepTable.thumbUrl] = step.thumbUrl
    this[StepTable.audioLabelUrl] = step.audioLabelUrl
    this[StepTable.audioFullUrl] = step.audioFullUrl
    this[StepTable.isPublic] = step.isPublic
    this[StepTable.pathSize] = step.pathSize
    this[StepTable.updatedAt] = step.updatedAt.toLocalDateTimeUtc()
    this[StepTable.createdAt] = step.createdAt.toLocalDateTimeUtc()
}