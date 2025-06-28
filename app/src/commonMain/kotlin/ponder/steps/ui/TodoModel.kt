package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ponder.steps.io.LocalTrekRepository
import ponder.steps.io.TrekRepository
import ponder.steps.model.data.TrekId
import pondui.ui.core.StateModel
import kotlin.time.Duration.Companion.minutes

class TodoModel(
    trekRepo: TrekRepository = LocalTrekRepository()
): StateModel<TodoState>(TodoState()) {

    init {
        viewModelScope.launch {
            while (true) {
                trekRepo.syncTreksWithIntents()
                delay(1.minutes)
            }
        }
    }

    fun loadTrek(trekId: TrekId?, isDeeper: Boolean) {
        if (trekId == null) {
            setState { it.copy(stackIndex = null) }
        } else {
            val indexOfTrekId = stateNow.stack.indexOfFirst { it == trekId }
            val currentIndex = stateNow.stackIndex
            if (indexOfTrekId >= 0) {
                setState { it.copy(stackIndex = indexOfTrekId) }
            } else if (isDeeper && currentIndex != null) {
                val stack = stateNow.stack.subList(0, currentIndex + 1) + trekId
                setState { it.copy(stack = stack, stackIndex = currentIndex + 1) }
            } else {
                setState { it.copy(stack = listOf(trekId), stackIndex = 0) }
            }
        }
    }
}

data class TodoState(
    val stack: List<TrekId> = emptyList(),
    val stackIndex: Int? = null,
)