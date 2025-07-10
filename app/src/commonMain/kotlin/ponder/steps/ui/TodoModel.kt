package ponder.steps.ui

import androidx.compose.runtime.Stable
import ponder.steps.io.LocalIntentRepository
import ponder.steps.io.LocalStepRepository
import ponder.steps.io.LocalTrekRepository
import ponder.steps.model.data.Step
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
            setState { it.copy(pageIndex = null) }
        } else {
            val pathId = trekPath.pathId
            val indexOfStepId = stateNow.pageStack.indexOfFirst { it.pathId == pathId }
            val currentIndex = stateNow.pageIndex
            if (indexOfStepId >= 0) {
                setState { it.copy(pageIndex = indexOfStepId, trekPath = trekPath) }
            } else if (isDeeper && currentIndex != null) {
                val stack = stateNow.pageStack.subList(0, currentIndex + 1) + trekPath
                setState { it.copy(pageStack = stack, pageIndex = currentIndex + 1, trekPath = trekPath) }
            } else {
                setState { it.copy(pageStack = listOf(trekPath), pageIndex = 0, trekPath = trekPath) }
            }
        }
    }
}

data class TodoState(
    val trekPath: TrekPath? = null,
    val pageStack: List<TrekPath> = emptyList(),
    val pageIndex: Int? = null,
)

@Stable
data class TrekPath(
    val trekPointId: Long,
    val pathId: StepId,
    val breadcrumbs: List<Step>,
) {
    val key get() = trekPointId.toString() + pathId

    fun toSubPath(stepId: StepId): TrekPath {
        val index = breadcrumbs.indexOfFirst { it.id == stepId }
        require(index >= 0)
        return this.copy(
            pathId = stepId,
            breadcrumbs = breadcrumbs.subList(0, index + 1)
        )
    }
}