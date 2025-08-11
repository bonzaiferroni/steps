package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ponder.steps.db.TimeUnit
import ponder.steps.io.LocalIntentRepository
import ponder.steps.io.LocalStepRepository
import ponder.steps.model.data.Intent
import ponder.steps.model.data.IntentId
import pondui.ui.core.StateModel
import pondui.ui.core.ModelState

class EditIntentCloudModel(
    private val dismiss: () -> Unit,
    private val intentRepo: LocalIntentRepository = LocalIntentRepository(),
    private val stepRepo: LocalStepRepository = LocalStepRepository(),
): StateModel<EditIntentCloudState>() {
    override val state = ModelState(EditIntentCloudState())

    private val editIntentState = ModelState(EditIntentState())
    val editIntent = EditIntentModel(this, editIntentState)

    fun setParameters(intentId: IntentId) {
        setState { it.copy(intent = null, imgUrl = null, label = null) }
        viewModelScope.launch {
            val intent = intentRepo.readIntentById(intentId) ?: return@launch
            val step = stepRepo.readStepById(intent.rootId) ?: return@launch
            val timing = when {
                intent.repeatMins != null -> IntentTiming.Repeat
                intent.scheduledAt != null -> IntentTiming.Schedule
                else -> IntentTiming.Once
            }
            val (repeatValue, repeatUnit) = intent.repeatMins?.let {
                repeatMinutesToValueUnits(it)
            } ?: (null to TimeUnit.Hour)

            editIntentState.setValue { it.copy(
                timing = timing,
                repeatValue = repeatValue,
                repeatUnit = repeatUnit,
                scheduledAt = intent.scheduledAt,
                priority = intent.priority,
            ) }
            setState { it.copy(intent = intent, imgUrl = step.thumbUrl, label = intent.label) }
        }
    }

    fun confirm() {
        val intent = stateNow.intent ?: return
        viewModelScope.launch {
            val isSuccess = intentRepo.updateIntent(
                intent.copy(
                    repeatMins = editIntentState.value.repeatMinutes,
                    priority = editIntentState.value.priority,
                    scheduledAt = editIntentState.value.scheduledAt,
                )
            )
            if (isSuccess) dismiss()
        }
    }
}

data class EditIntentCloudState(
    val intent: Intent? = null,
    val imgUrl: String? = null,
    val label: String? = null,
)
