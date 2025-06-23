package ponder.steps.db

import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

enum class TimeUnit {
    Minute,
    Hour,
    Day,
    Week,
    Month,
    Year;

    fun toRepeatFormat(value: Int) = when {
        value == 1 -> this.toString().lowercase()
        else -> "$value ${this.toString().lowercase()}s"
    }

    fun toDuration(): kotlin.time.Duration = when (this) {
        Minute -> 1.minutes
        Hour -> 1.hours
        Day -> 1.days
        Week -> 7.days
        Month -> 30.days
        Year -> 365.days
    }
}