package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kabinet.utils.startOfDay
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
        println(stateNow.intentRepeat)
        intentRepo.createIntent(
            NewIntent(
                rootId = stepId,
                label = label,
                repeatMins = stateNow.repeatMinutes
            )
        )
        trekRepo.syncTreksWithIntents()
    }

    fun completeStep(item: TrekItem) {
        viewModelScope.launch {
            trekRepo.completeStep(item.trekId)
        }
    }

    fun setIntentTiming(value: IntentTiming) {
        setState { it.copy(intentTiming = value) }
    }

    fun setIntentRepeat(value: Int) {
        setState { it.copy(intentRepeat = value) }
    }

    fun setIntentRepeatUnit(value: TimeUnit) {
        setState { it.copy(intentRepeatUnit = value) }
    }
}

data class TodoState(
    val items: List<TrekItem> = emptyList(),
    val isAddingItem: Boolean = false,
    val newStepLabel: String = "",
    val searchedSteps: List<Step> = emptyList(),
    val intentTiming: IntentTiming = IntentTiming.Once,
    val intentRepeat: Int = 1,
    val intentRepeatUnit: TimeUnit = TimeUnit.Hours
) {
    val isValidNewStep get() = newStepLabel.isNotEmpty()
    val repeatValues
        get() = when (intentRepeatUnit) {
            TimeUnit.Minutes -> repeatMinuteValues
            TimeUnit.Hours -> repeatHourValues
            TimeUnit.Days -> repeatDayValues
            TimeUnit.Weeks -> repeatWeekValues
            TimeUnit.Months -> repeatMonth
            TimeUnit.Years -> repeatYears
        }

    val repeatMinutes
        get() = if (intentTiming != IntentTiming.Repeat) null else when (intentRepeatUnit) {
            TimeUnit.Minutes -> intentRepeat
            TimeUnit.Hours -> intentRepeat * 60
            TimeUnit.Days -> intentRepeat * 60 * 24
            TimeUnit.Weeks -> intentRepeat * 60 * 24 * 7
            TimeUnit.Months -> intentRepeat * 60 * 24 * 30
            TimeUnit.Years -> intentRepeat * 60 * 24 * 365
        }
}

enum class IntentTiming(val label: String) {
    Schedule("Schedule"),
    Once("One time"),
    Repeat("Repeat");

    override fun toString() = label
}

enum class TimeUnit {
    Minutes,
    Hours,
    Days,
    Weeks,
    Months,
    Years
}

private val repeatMinuteValues = (1..24).map { it * 5 }.toImmutableList()
private val repeatHourValues = (1..48).toImmutableList()
private val repeatDayValues = (1..60).toImmutableList()
private val repeatWeekValues = (1..8).toImmutableList()
private val repeatMonth = (1..24).toImmutableList()
private val repeatYears = (1..100).toImmutableList()