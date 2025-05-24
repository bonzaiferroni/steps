package ponder.steps.model.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

fun LocalDateTime.toEpochSeconds() = toInstant(TimeZone.currentSystemDefault()).epochSeconds
fun Long.toLocalDateTime() = Instant.fromEpochSeconds(this)
    .toLocalDateTime(TimeZone.currentSystemDefault())

fun Instant.toLocalEpochSeconds() = toLocalDateTime(TimeZone.currentSystemDefault())
    .toEpochSeconds()

fun Instant.toLocalDateTime() = toLocalDateTime(TimeZone.currentSystemDefault())

@OptIn(FormatStringsInDatetimeFormats::class)
fun LocalDateTime.toFormatString(pattern: String) = this.format(
    LocalDateTime.Format { byUnicodePattern(pattern) }
)