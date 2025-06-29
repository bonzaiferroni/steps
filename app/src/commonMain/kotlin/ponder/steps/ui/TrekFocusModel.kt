package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import ponder.steps.io.AnswerRepository
import ponder.steps.io.LocalAnswerRepository
import ponder.steps.io.LocalQuestionRepository
import ponder.steps.io.LocalStepLogRepository
import ponder.steps.io.LocalTrekRepository
import ponder.steps.io.QuestionRepository
import ponder.steps.io.StepLogRepository
import ponder.steps.io.TrekRepository
import ponder.steps.model.data.TrekId
import ponder.steps.model.data.TrekStep
import pondui.ui.core.StateModel

class TrekFocusModel(
    private val trekId: TrekId,
    private val loadTrek: (TrekId?, Boolean) -> Unit,
    private val trekRepo: TrekRepository = LocalTrekRepository(),
): StateModel<TrekFocusState>(TrekFocusState()) {

    val treks = TrekStepListModel(this, loadTrek)

    init {
        trekRepo.flowTrekStepById(trekId).launchCollect { trekStep ->
            setState { it.copy(trek = trekStep) }
        }

        trekRepo.flowTrekStepsBySuperId(trekId).launchCollect { trekSteps ->
            treks.setTrekSteps(trekSteps.sortedBy { trek -> trek.position })
        }
    }
}

data class TrekFocusState(
    val trek: TrekStep? = null,
)