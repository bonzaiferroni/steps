package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ponder.steps.db.TrekImgUrl
import ponder.steps.io.LocalTrekRepository
import ponder.steps.io.TrekRepository
import ponder.steps.model.data.TrekId
import pondui.ui.core.StateModel
import kotlin.time.Duration.Companion.minutes

class TodoModel(
    private val trekRepo: LocalTrekRepository = LocalTrekRepository()
): StateModel<TodoState>(TodoState()) {

    init {
        ioLaunch {
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
        refreshBreadcrumbs()
    }

    private fun refreshBreadcrumbs() {
        val index = stateNow.stackIndex
        if (index == null) {
            setState { it.copy(breadcrumbUrls = emptyList()) }
        } else {
            ioLaunch {
                val urls = trekRepo.readTrekThumbnails(stateNow.stack.subList(0, index + 1))
                    .sortedBy { t -> stateNow.stack.indexOfFirst { it == t.trekId } }
                setState { it.copy(breadcrumbUrls = urls) }
            }
        }
    }
}

data class TodoState(
    val stack: List<TrekId> = emptyList(),
    val stackIndex: Int? = null,
    val breadcrumbUrls: List<TrekImgUrl> = emptyList(),
)