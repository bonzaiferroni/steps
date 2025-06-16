package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kabinet.utils.startOfDay
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import ponder.steps.appDb
import ponder.steps.db.TrekDao
import ponder.steps.model.data.Intent
import ponder.steps.model.data.LogEntry
import ponder.steps.model.data.TrekStep
import pondui.ui.core.StateModel
import kotlin.time.Duration.Companion.days

class TrekPathModel(
    private val trekDao: TrekDao = appDb.getTrekDao()
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
                trekDao.flowTrekStepById(trekId).collect { trekStep ->
                    setState { it.copy(trek = trekStep) }
                }
            }.addJob()

            viewModelScope.launch {
                trekDao.flowTrekStepsBySuperId(trekId).collect { trekSteps ->
                    setState { it.copy(steps = trekSteps.sortedBy { trek -> trek.position }) }
                }
            }.addJob()
        } else {
            viewModelScope.launch {
                val start = Clock.startOfDay()
                val end = start + 1.days
                trekDao.flowRootTrekSteps(start, end).collect { trekSteps ->
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
}

data class TrekPathState(
    val trek: TrekStep? = null,
    val steps: List<TrekStep> = emptyList(),
    val logs: List<LogEntry> = emptyList(),
    val isAddingItem: Boolean = false,
)
