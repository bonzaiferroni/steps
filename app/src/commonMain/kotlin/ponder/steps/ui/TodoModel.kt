package ponder.steps.ui

import androidx.lifecycle.ViewModel
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

    fun loadTrek(trekId: TrekId?) {
        setState { it.copy(trekId = trekId) }
    }
}

data class TodoState(
    val trekId: TrekId? = null,
    // val stack: List<TrekId> = emptyList()
)