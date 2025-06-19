package ponder.steps.ui

import ponder.steps.db.StepId
import ponder.steps.io.AnswerRepository
import ponder.steps.io.LocalAnswerRepository
import ponder.steps.io.LocalStepLogRepository
import ponder.steps.io.StepLogRepository
import ponder.steps.model.data.Answer
import ponder.steps.model.data.StepLog
import pondui.ui.core.StateModel

class StepActivityModel(
    private val stepId: String,
    private val stepLogRepo: StepLogRepository = LocalStepLogRepository(),
    private val answerRepo: AnswerRepository = LocalAnswerRepository()
): StateModel<StepActivityState>(StepActivityState()) {

    init {
        answerRepo.flowAnswersByStepId(stepId).launchCollectJob { stepLogAnswers ->
            setState { it.copy(stepLogAnswers = stepLogAnswers) }
        }
    }
}

data class StepActivityState(
    val stepLogAnswers: Map<StepLog, List<Answer>> = emptyMap()
)