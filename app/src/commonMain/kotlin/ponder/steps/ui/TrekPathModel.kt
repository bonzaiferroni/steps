package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kabinet.utils.startOfDay
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import ponder.steps.io.LocalLogRepository
import ponder.steps.io.LocalTrekRepository
import ponder.steps.io.LogRepository
import ponder.steps.io.TrekRepository
import ponder.steps.model.data.StepLog
import ponder.steps.model.data.StepOutcome
import ponder.steps.model.data.TrekStep
import pondui.ui.core.StateModel
import kotlin.time.Duration.Companion.days

class TrekPathModel(
    private val trekRepo: TrekRepository = LocalTrekRepository(),
    private val logRepo: LogRepository = LocalLogRepository(),
): StateModel<TrekPathState>(TrekPathState()) {

    private val jobs = mutableListOf<Job>()

    init {
        loadTrek(null, true)
    }

    fun loadTrek(trekId: String?, isDeeper: Boolean) {
        jobs.forEach { it.cancel() }
        jobs.clear()
        if (trekId != null) {
            viewModelScope.launch {
                trekRepo.flowTrekStepById(trekId).collect { trekStep ->
                    val steps = stateNow.steps.filter { it.pathStepId != trekStep.pathStepId }
                    setState { it.copy(trek = trekStep, steps = steps) }
                }
            }.addJob()
            viewModelScope.launch {
                trekRepo.flowTrekStepsBySuperId(trekId).collect { trekSteps ->
                    setState { it.copy(steps = trekSteps.sortedBy { trek -> trek.position }) }
                }
            }.addJob()
            viewModelScope.launch {
                logRepo.flowStepLogsByTrekId(trekId).collect { logs ->
                    setState { it.copy(logs = logs) }
                }
            }.addJob()
        } else {
            viewModelScope.launch {
                val start = Clock.startOfDay()
                val end = start + 1.days
                trekRepo.flowRootTrekSteps(start, end).collect { trekSteps ->
                    setState { it.copy(steps = trekSteps.sortedBy { trek -> trek.availableAt }) }
                }
            }.addJob()
            setState { it.copy(trek = null) }
        }
        setState { it.copy(isDeeper = isDeeper) }
    }

    private fun Job.addJob() = jobs.add(this)

    fun toggleAddItem() {
        setState { it.copy(isAddingItem = !it.isAddingItem) }
    }

    fun branchStep(pathStepId: String?) {
        val trekId = stateNow.trek?.trekId ?: return
        val pathStepId = pathStepId ?: return
        viewModelScope.launch {
            val id = trekRepo.createSubTrek(trekId, pathStepId)
            loadTrek(id, true)
        }
    }

    fun setOutcome(trekStep: TrekStep, outcome: StepOutcome? = null) {
        val trekId = trekStep.superId ?: trekStep.trekId ?: error("No trekId")
        viewModelScope.launch {
            trekRepo.setOutcome(trekId, trekStep.stepId, trekStep.pathStepId, outcome)
        }
    }
}

data class TrekPathState(
    val trek: TrekStep? = null,
    val steps: List<TrekStep> = emptyList(),
    val logs: List<StepLog> = emptyList(),
    val isAddingItem: Boolean = false,
    val isDeeper: Boolean = false,
    val stepLogs: List<StepLog> = emptyList()
)
