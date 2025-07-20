package ponder.steps.ui

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import kabinet.utils.toRelativeTimeFormat
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.Instant
import ponder.steps.db.TimeUnit
import ponder.steps.model.data.IntentPriority
import pondui.ui.core.SubModel

@Stable
class EditIntentModel(
    override val viewModel: ViewModel
): SubModel<EditIntentState>(EditIntentState()) {

    fun setIntentTiming(value: IntentTiming) {
        setState { it.copy(timing = value) }
    }

    fun setIntentRepeat(value: Int?) {
        setState { it.copy(repeatValue = value) }
    }

    fun setIntentRepeatUnit(value: TimeUnit) {
        setState { it.copy(repeatUnit = value) }
    }

    fun setScheduleAt(time: Instant) {
        setState { it.copy(scheduledAt = time) }
    }

    fun setIntentPriority(priority: IntentPriority) {
        setState { it.copy(priority = priority) }
    }
}

data class EditIntentState(
    val timing: IntentTiming = IntentTiming.Once,
    val repeatValue: Int? = null,
    val repeatUnit: TimeUnit = TimeUnit.Hour,
    val scheduledAt: Instant? = null,
    val priority: IntentPriority = IntentPriority.Default,
) {
    val repeatValues
        get() = when (repeatUnit) {
            TimeUnit.Minute -> repeatMinuteValues
            TimeUnit.Hour -> repeatHourValues
            TimeUnit.Day -> repeatDayValues
            TimeUnit.Week -> repeatWeekValues
            TimeUnit.Month -> repeatMonth
            TimeUnit.Year -> repeatYears
        }

    val repeatMinutes
        get() = repeatValue?.let {
            when (repeatUnit) {
                TimeUnit.Minute -> repeatValue
                TimeUnit.Hour -> repeatValue * 60
                TimeUnit.Day -> repeatValue * 60 * 24
                TimeUnit.Week -> repeatValue * 60 * 24 * 7
                TimeUnit.Month -> repeatValue * 60 * 24 * 30
                TimeUnit.Year -> repeatValue * 60 * 24 * 365
            }
        }

    val scheduleDescription
        get() = when {
            scheduledAt != null -> scheduledAt.toRelativeTimeFormat()
            repeatValue != null -> "Every ${repeatUnit.toRepeatFormat(repeatValue)}"
            else -> "Once"
        }
}

private val repeatMinuteValues = ((1..4) + (1..60).map { it * 5 }).toImmutableList()
private val repeatHourValues = (1..48).toImmutableList()
private val repeatDayValues = (1..60).toImmutableList()
private val repeatWeekValues = (1..8).toImmutableList()
private val repeatMonth = (1..24).toImmutableList()
private val repeatYears = (1..100).toImmutableList()

fun repeatMinutesToValueUnits(repeatMins: Int) = when {
    repeatMins < 60 -> repeatMins to TimeUnit.Minute
    repeatMins < 60 * 24 -> repeatMins / 60 to TimeUnit.Hour
    repeatMins < 60 * 24 * 7 -> repeatMins / (60 * 24) to TimeUnit.Day
    repeatMins < 60 * 24 * 30 -> repeatMins / (60 * 24 * 7) to TimeUnit.Week
    repeatMins < 60 * 24 * 365 -> repeatMins / (60 * 24 * 30) to TimeUnit.Month
    else -> repeatMins / (60 * 24 * 365) to TimeUnit.Year
}