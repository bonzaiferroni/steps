package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kabinet.utils.startOfDay
import kabinet.utils.startOfWeek
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ponder.steps.io.LocalTrekRepository
import ponder.steps.io.TrekRepository
import ponder.steps.model.data.TrekItem
import pondui.ui.core.StateModel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

class TodoModel(
    private val trekRepo: TrekRepository = LocalTrekRepository(),
) : StateModel<TodoState>(TodoState()) {

    private var flowJob: Job? = null

    fun onLoad() {
        flowJob?.cancel()
        flowJob = viewModelScope.launch {
            val startTime = when (stateNow.span) {
                TrekSpan.Hours -> Clock.startOfDay()
                TrekSpan.Day -> Clock.startOfDay()
                TrekSpan.Week -> Clock.startOfWeek()
            }
            val endTime = startTime + stateNow.span.duration
            trekRepo.flowTreksInRange(startTime, endTime).collect { treks ->

                setState {
                    it.copy(
                        items = treks.sortedWith(
                            compareByDescending<TrekItem> { trek -> trek.finishedAt ?: Instant.DISTANT_FUTURE }
                                .thenBy { trek -> trek.intentPriority.ordinal }
                        ))
                }
            }
        }
    }

    fun onDispose() = cancelJobs()

    fun toggleAddItem() {
        setState { it.copy(isAddingItem = !it.isAddingItem) }
    }

    fun completeStep(item: TrekItem) {
        viewModelScope.launch {
            trekRepo.completeStep(item.trekId)
        }
    }

    fun setSpan(span: TrekSpan) {
        if (stateNow.span == span) return
        setState { it.copy(span = span) }
        onLoad() // Reload items for the new span
    }
}

data class TodoState(
    val items: List<TrekItem> = emptyList(),
    val isAddingItem: Boolean = false,
    val span: TrekSpan = TrekSpan.Day
)

enum class TrekSpan(val label: String, val duration: Duration) {
    Hours("4 hours", 4.hours),
    Day("Day", 1.days),
    Week("Week", 7.days);
}