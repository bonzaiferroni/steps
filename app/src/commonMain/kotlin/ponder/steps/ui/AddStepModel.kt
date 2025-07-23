package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ponder.steps.io.AiClient
import ponder.steps.io.IntentRepository
import ponder.steps.io.LocalIntentRepository
import ponder.steps.io.LocalStepRepository
import ponder.steps.io.LocalTrekRepository
import ponder.steps.io.StepRepository
import ponder.steps.io.TrekRepository
import ponder.steps.model.data.NewIntent
import ponder.steps.model.data.NewStep
import ponder.steps.model.data.Step
import pondui.LocalValueRepository
import pondui.ValueRepository
import pondui.ui.core.StateModel
import pondui.ui.core.ViewState

class AddStepModel(
    private val dismiss: () -> Unit,
    private val trekRepo: TrekRepository = LocalTrekRepository(),
    private val stepRepo: StepRepository = LocalStepRepository(),
    private val intentRepo: IntentRepository = LocalIntentRepository(),
    private val valueRepo: ValueRepository = LocalValueRepository(),
    private val aiClient: AiClient = AiClient(),
) : StateModel<AddIntentState>(AddIntentState()) {

    private val editIntentState = ViewState(EditIntentState())
    val editIntent = EditIntentModel(this, editIntentState)

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
                    val step = stepRepo.readStepById(stepId)
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
        val step = stepRepo.readStepById(stepId) ?: error("No step with id: $stepId")
        val pathIds = if (step.pathSize > 0) listOf(stepId) else emptyList()
        intentRepo.createIntent(
            NewIntent(
                rootId = stepId,
                label = label,
                repeatMins = editIntentState.value.repeatMinutes,
                priority = editIntentState.value.priority,
                scheduledAt = editIntentState.value.scheduledAt,
                pathIds = pathIds
            )
        )
    }

    fun setIntentStep(step: Step?) {
        setState { it.copy(existingStep = step) }
    }
}

data class AddIntentState(
    val searchedSteps: List<Step> = emptyList(),
    val intentLabel: String = "",
    val existingStep: Step? = null,
    val createIntent: Boolean = false,
    val pathId: String? = null,
) {
    val isValidNewStep get() = intentLabel.isNotEmpty()
}
