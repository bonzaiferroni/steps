package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ponder.steps.io.LocalTrekRepository
import ponder.steps.io.TrekRepository
import ponder.steps.model.data.PathStepId
import ponder.steps.model.data.TrekId
import ponder.steps.model.data.TrekStep
import pondui.ui.core.StateModel

class TrekProfileModel(
    private val trekId: TrekId,
    private val loadTrek: (TrekId?, Boolean) -> Unit,
    protected val trekRepo: TrekRepository = LocalTrekRepository(),
): StateModel<TodoTrekState>(TodoTrekState()) {

    init {
        trekRepo.flowTrekStepById(trekId).launchCollect { trekStep ->
            setState { it.copy(trek = trekStep) }
        }
    }

    val treks = object: TrekStepListModel(loadTrek, viewModelScope) {
        init {
            trekRepo.flowTrekStepsBySuperId(trekId).launchCollect { trekSteps ->
                setState { it.copy(steps = trekSteps.sortedBy { trek -> trek.position }) }
            }
            stepLogRepo.flowPathLogsByTrekId(trekId).launchCollect(::setLogs)
            questionRepo.flowPathQuestionsByTrekId(trekId).launchCollect(::setQuestions)
            answerRepo.flowPathQuestionsByTrekId(trekId).launchCollect(::setAnswers)
        }
    }
}

data class TodoTrekState(
    val trek: TrekStep? = null,
)