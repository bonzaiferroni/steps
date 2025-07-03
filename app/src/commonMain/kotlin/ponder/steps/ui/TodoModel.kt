package ponder.steps.ui

import ponder.steps.db.StepImgUrl
import ponder.steps.io.LocalIntentRepository
import ponder.steps.io.LocalStepRepository
import ponder.steps.io.LocalTrekRepository
import ponder.steps.model.data.StepId
import ponder.steps.model.data.TrekId
import pondui.ui.core.StateModel

class TodoModel(
    private val trekRepo: LocalTrekRepository = LocalTrekRepository(),
    private val intentRepo: LocalIntentRepository = LocalIntentRepository(),
    private val stepRepo: LocalStepRepository = LocalStepRepository()

): StateModel<TodoState>(TodoState()) {

    val trekStarter = TrekStarter(this)

    fun navToPath(trekPath: TrekPath?, isDeeper: Boolean) {
        if (trekPath == null) {
            setState { it.copy(stackIndex = null, stack = emptyList(), trekId = null) }
        } else {
            val (trekId, stepId) = trekPath
            val indexOfStepId = stateNow.stack.indexOfFirst { it == stepId }
            val currentIndex = stateNow.stackIndex
            if (indexOfStepId >= 0) {
                setState { it.copy(stackIndex = indexOfStepId, trekId = trekId) }
            } else if (isDeeper && currentIndex != null) {
                val stack = stateNow.stack.subList(0, currentIndex + 1) + stepId
                setState { it.copy(stack = stack, stackIndex = currentIndex + 1, trekId = trekId) }
            } else {
                setState { it.copy(stack = listOf(stepId), stackIndex = 0, trekId = trekId) }
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
                val urls = stepRepo.readThumbnails(stateNow.stack.subList(0, index + 1))
                    .sortedBy { t -> stateNow.stack.indexOfFirst { it == t.stepId } }
                setState { it.copy(breadcrumbUrls = urls) }
            }
        }
    }
}

data class TodoState(
    val trekId: TrekId? = null,
    val stack: List<StepId> = emptyList(),
    val stackIndex: Int? = null,
    val breadcrumbUrls: List<StepImgUrl> = emptyList(),
)

data class TrekPath(
    val trekId: TrekId,
    val pathId: StepId,
)