package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kabinet.utils.startOfDay
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import ponder.steps.appDb
import ponder.steps.db.TrekDao
import ponder.steps.io.LocalTrekRepository
import ponder.steps.io.TrekRepository
import ponder.steps.model.data.Intent
import ponder.steps.model.data.LogEntry
import ponder.steps.model.data.TrekStep
import pondui.ui.core.StateModel
import kotlin.time.Duration.Companion.days

class TrekPathModel(
    private val trekRepo: TrekRepository = LocalTrekRepository()
): StateModel<TrekPathState>(TrekPathState()) {

    private val jobs = mutableListOf<Job>()

    init {
        loadTrek(null)
    }

    fun loadTrek(trekId: String?) {
        jobs.forEach { it.cancel() }
        jobs.clear()
        if (trekId != null) {
            viewModelScope.launch {
                trekRepo.flowTrekStepById(trekId).collect { trekStep ->
                    setState { it.copy(trek = trekStep) }
                }
            }.addJob()

            viewModelScope.launch {
                trekRepo.flowTrekStepsBySuperId(trekId).collect { trekSteps ->
                    setState { it.copy(steps = trekSteps.sortedBy { trek -> trek.position }) }
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
            loadTrek(id)
        }
    }
}

data class TrekPathState(
    val trek: TrekStep? = null,
    val steps: List<TrekStep> = emptyList(),
    val logs: List<LogEntry> = emptyList(),
    val isAddingItem: Boolean = false,
)
