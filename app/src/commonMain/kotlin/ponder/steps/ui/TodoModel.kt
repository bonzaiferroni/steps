package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kabinet.utils.startOfDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import ponder.steps.io.AiClient
import ponder.steps.io.LocalIntentRepository
import ponder.steps.io.IntentRepository
import ponder.steps.io.LocalStepRepository
import ponder.steps.io.StepRepository
import ponder.steps.io.LocalTrekRepository
import ponder.steps.io.TrekRepository
import ponder.steps.model.data.NewIntent
import ponder.steps.model.data.NewStep
import ponder.steps.model.data.Step
import ponder.steps.model.data.TrekItem
import pondui.LocalValueRepository
import pondui.ValueRepository
import pondui.ui.core.StateModel

class TodoModel(
    private val trekRepo: TrekRepository = LocalTrekRepository(),
    private val stepRepo: StepRepository = LocalStepRepository(),
    private val intentRepo: IntentRepository = LocalIntentRepository(),
    private val valueRepo: ValueRepository = LocalValueRepository(),
    private val aiClient: AiClient = AiClient(),
) : StateModel<TodoState>(TodoState()) {

    init {
        setNewStepLabel("")
    }

    fun onLoad() {
        viewModelScope.launch {
            trekRepo.flowTreksSince(Clock.startOfDay()).collect { treks ->
                setState { it.copy(items = treks) }
            }
        }
    }

    fun onDispose() = cancelJobs()

    fun toggleAddItem() {
        setState { it.copy(isAddingItem = !it.isAddingItem) }
    }

    fun setNewStepLabel(value: String) {
        setState { it.copy(newStepLabel = value) }
        viewModelScope.launch {
            val searchedSteps = value.takeIf { it.isNotEmpty() }?.let { stepRepo.readSearch(it) }
                ?: stepRepo.readRootSteps()
            setState { it.copy(searchedSteps = searchedSteps) }
        }
    }

    fun createStep() {
        if (!stateNow.isValidNewStep) return
        viewModelScope.launch {
            val stepId = stepRepo.createStep(NewStep(stateNow.newStepLabel))
            val defaultTheme = valueRepo.readString(SETTINGS_DEFAULT_THEME)
            if (defaultTheme.isNotEmpty()) {
                viewModelScope.launch(Dispatchers.IO) {
                    val step = stepRepo.readStep(stepId)
                        ?: error("unable to read step for image creation: ${stateNow.newStepLabel}")
                    val url = aiClient.generateImage(step, null, defaultTheme)
                    stepRepo.updateStep(step.copy(imgUrl = url.url, thumbUrl = url.thumbUrl))
                }
            }

            addIntent(stepId, stateNow.newStepLabel)
            setState { it.copy(newStepLabel = "", isAddingItem = false) }
        }
    }

    fun addSearchedStep(step: Step) {
        viewModelScope.launch {
            addIntent(step.id, step.label)
            setState { it.copy(isAddingItem = false) }
        }
    }

    private suspend fun addIntent(stepId: String, label: String) {
        intentRepo.createIntent(
            NewIntent(
                rootId = stepId,
                label = label,
            )
        )
        trekRepo.syncTreksWithIntents()
    }
}

data class TodoState(
    val items: List<TrekItem> = emptyList(),
    val isAddingItem: Boolean = false,
    val newStepLabel: String = "",
    val searchedSteps: List<Step> = emptyList()
) {
    val isValidNewStep get() = newStepLabel.isNotEmpty()
}