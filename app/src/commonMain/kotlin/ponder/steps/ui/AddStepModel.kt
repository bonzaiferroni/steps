package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kabinet.utils.toRelativeTimeFormat
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ponder.steps.io.AiClient
import ponder.steps.io.IntentRepository
import ponder.steps.io.LocalIntentRepository
import ponder.steps.io.LocalStepRepository
import ponder.steps.io.LocalTrekRepository
import ponder.steps.io.StepRepository
import ponder.steps.io.TrekRepository
import ponder.steps.model.data.IntentPriority
import ponder.steps.model.data.NewIntent
import ponder.steps.model.data.NewStep
import ponder.steps.model.data.Step
import pondui.LocalValueRepository
import pondui.ValueRepository
import pondui.ui.core.StateModel
import kotlin.time.Duration.Companion.hours

class AddStepModel(
    private val dismiss: () -> Unit,
    private val trekRepo: TrekRepository = LocalTrekRepository(),
    private val stepRepo: StepRepository = LocalStepRepository(),
    private val intentRepo: IntentRepository = LocalIntentRepository(),
    private val valueRepo: ValueRepository = LocalValueRepository(),
    private val aiClient: AiClient = AiClient(),
) : StateModel<AddIntentState>(AddIntentState()) {

    init {
        setNewStepLabel("")
    }

    fun setParameters(createIntent: Boolean, pathId: String?) {
        setState { it.copy(createIntent = createIntent, pathId = pathId) }
    }

    fun setNewStepLabel(value: String) {
        setState { it.copy(intentLabel = value) }
        viewModelScope.launch {
            val searchedSteps = value.takeIf { it.isNotEmpty() }?.let { stepRepo.readSearch(it) }
                ?: stepRepo.readRootSteps()
            setState { it.copy(searchedSteps = searchedSteps) }
        }
    }

    fun createStep() {
        if (!stateNow.isValidNewStep) return
        viewModelScope.launch {
            val stepId = stepRepo.createStep(
                NewStep(
                    label = stateNow.intentLabel,
                    pathId = stateNow.pathId
                )
            )
            val defaultTheme = valueRepo.readString(SETTINGS_DEFAULT_IMAGE_THEME)
            if (defaultTheme.isNotEmpty()) {
                viewModelScope.launch(Dispatchers.IO) {
                    val step = stepRepo.readStep(stepId)
                        ?: error("unable to read step for image creation: ${stateNow.intentLabel}")
                    val url = aiClient.generateImage(step, null, defaultTheme)
                    stepRepo.updateStep(step.copy(imgUrl = url.url, thumbUrl = url.thumbUrl))
                }
            }

            if (stateNow.createIntent) {
                addIntent(stepId, stateNow.intentLabel)
            }
            finishDialog()
        }
    }

    fun addExistingStep() {
        val step = stateNow.existingStep ?: return
        viewModelScope.launch {
            val pathId = stateNow.pathId
            if (stateNow.createIntent) {
                addIntent(step.id, step.label)
                finishDialog()
            } else if (pathId != null) {
                stepRepo.addStepToPath(pathId, step.id, null)
                finishDialog()
            }
        }
    }

    private fun finishDialog() {
        setState {
            it.copy(
                intentLabel = "",
                existingStep = null,
            )
        }
        dismiss()
    }

    private suspend fun addIntent(stepId: String, label: String) {
        val step = stepRepo.readStep(stepId) ?: error("No step with id: $stepId")
        val pathIds = if (step.pathSize > 0) listOf(stepId) else emptyList()
        intentRepo.createIntent(
            NewIntent(
                rootId = stepId,
                label = label,
                repeatMins = stateNow.repeatMinutes,
                priority = stateNow.intentPriority,
                scheduledAt = stateNow.intentScheduledAt,
                pathIds = pathIds
            )
        )
        trekRepo.syncTreksWithIntents()
    }

    fun setIntentTiming(value: IntentTiming) {
        setState { it.copy(intentTiming = value) }
    }

    fun setIntentRepeat(value: Int) {
        setState { it.copy(intentRepeatValue = value) }
    }

    fun setIntentRepeatUnit(value: TimeUnit) {
        setState { it.copy(intentRepeatUnit = value) }
    }

    fun setScheduleAt(time: Instant) {
        setState { it.copy(intentScheduledAt = time) }
    }

    fun setIntentPriority(priority: IntentPriority) {
        setState { it.copy(intentPriority = priority) }
    }

    fun setIntentStep(step: Step?) {
        setState { it.copy(existingStep = step) }
    }
}

data class AddIntentState(
    val searchedSteps: List<Step> = emptyList(),
    val intentLabel: String = "",
    val intentTiming: IntentTiming = IntentTiming.Once,
    val intentRepeatValue: Int = 1,
    val intentRepeatUnit: TimeUnit = TimeUnit.Hour,
    val intentScheduledAt: Instant = Clock.System.now() + 1.hours,
    val intentPriority: IntentPriority = IntentPriority.Default,
    val existingStep: Step? = null,
    val createIntent: Boolean = false,
    val pathId: String? = null,
) {
    val isValidNewStep get() = intentLabel.isNotEmpty()
    val repeatValues
        get() = when (intentRepeatUnit) {
            TimeUnit.Minute -> repeatMinuteValues
            TimeUnit.Hour -> repeatHourValues
            TimeUnit.Day -> repeatDayValues
            TimeUnit.Week -> repeatWeekValues
            TimeUnit.Month -> repeatMonth
            TimeUnit.Year -> repeatYears
        }

    val repeatMinutes
        get() = if (intentTiming != IntentTiming.Repeat) null else when (intentRepeatUnit) {
            TimeUnit.Minute -> intentRepeatValue
            TimeUnit.Hour -> intentRepeatValue * 60
            TimeUnit.Day -> intentRepeatValue * 60 * 24
            TimeUnit.Week -> intentRepeatValue * 60 * 24 * 7
            TimeUnit.Month -> intentRepeatValue * 60 * 24 * 30
            TimeUnit.Year -> intentRepeatValue * 60 * 24 * 365
        }

    val scheduleDescription
        get() = when (intentTiming) {
            IntentTiming.Schedule -> intentScheduledAt.toRelativeTimeFormat()
            IntentTiming.Once -> "Once"
            IntentTiming.Repeat -> "Every ${intentRepeatUnit.toRepeatFormat(intentRepeatValue)}"
        }
}

enum class IntentTiming(val label: String) {
    Schedule("Schedule"),
    Once("One time"),
    Repeat("Repeat");

    override fun toString() = label
}

enum class TimeUnit {
    Minute,
    Hour,
    Day,
    Week,
    Month,
    Year;

    fun toRepeatFormat(value: Int) = when {
        value == 1 -> this.toString().lowercase()
        else -> "$value ${this.toString().lowercase()}s"
    }
}

private val repeatMinuteValues = (1..24).map { it * 5 }.toImmutableList()
private val repeatHourValues = (1..48).toImmutableList()
private val repeatDayValues = (1..60).toImmutableList()
private val repeatWeekValues = (1..8).toImmutableList()
private val repeatMonth = (1..24).toImmutableList()
private val repeatYears = (1..100).toImmutableList()