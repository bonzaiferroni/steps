package ponder.steps.ui

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import ponder.steps.io.LocalQuestionRepository
import ponder.steps.io.LocalStepRepository
import ponder.steps.io.StepRepository
import ponder.steps.model.data.Question
import ponder.steps.model.data.Step
import ponder.steps.model.data.StepId
import pondui.ui.core.SubModel

class PathContextModel(
    viewModel: ViewModel,
    val stepRepo: StepRepository = LocalStepRepository(),
    val questionRepo: LocalQuestionRepository = LocalQuestionRepository()
): SubModel<PathContextState>(PathContextState(), viewModel) {

    fun setParameters(pathId: StepId) {
        clearJobs()
        stepRepo.flowStep(pathId).launchCollect { step ->
            setState { it.copy(step = step,) }
        }
        stepRepo.flowPathSteps(pathId).launchCollect { steps ->
            setState { it.copy(steps = steps.sortedBy { pStep -> pStep.position }) }
        }
        questionRepo.flowPathQuestions(pathId).launchCollect { questions ->
            setState { it.copy(questions = questions) }
        }
    }
}

@Stable
data class PathContextState(
    val step: Step? = null,
    val steps: List<Step> = emptyList(),
    val questions: Map<StepId, List<Question>> = emptyMap(),
)