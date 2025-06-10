package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kabinet.utils.startOfDay
import kabinet.utils.toRelativeTimeFormat
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ponder.steps.io.AiClient
import ponder.steps.io.LocalIntentRepository
import ponder.steps.io.IntentRepository
import ponder.steps.io.LocalStepRepository
import ponder.steps.io.StepRepository
import ponder.steps.io.LocalTrekRepository
import ponder.steps.io.TrekRepository
import ponder.steps.model.data.IntentPriority
import ponder.steps.model.data.NewIntent
import ponder.steps.model.data.NewStep
import ponder.steps.model.data.Step
import ponder.steps.model.data.TrekItem
import pondui.LocalValueRepository
import pondui.ValueRepository
import pondui.ui.core.StateModel
import kotlin.time.Duration.Companion.hours

class TodoModel(
    private val trekRepo: TrekRepository = LocalTrekRepository(),
) : StateModel<TodoState>(TodoState()) {

    fun onLoad() {
        viewModelScope.launch {
            trekRepo.flowTreksSince(Clock.startOfDay()).collect { treks ->
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
}

data class TodoState(
    val items: List<TrekItem> = emptyList(),
    val isAddingItem: Boolean = false,
)

