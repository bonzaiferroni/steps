package ponder.steps.ui

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ponder.steps.io.QuestionSource
import ponder.steps.io.LocalStepRepository
import ponder.steps.io.LocalTagRepository
import ponder.steps.io.StepRepository
import ponder.steps.model.data.Question
import ponder.steps.model.data.Step
import ponder.steps.model.data.StepId
import ponder.steps.model.data.Tag
import ponder.steps.model.data.TagId
import pondui.ui.core.SubModel
import pondui.ui.core.ViewState

class PathContextModel(
    override val viewModel: ViewModel,
    override val state: ViewState<PathContextState>,
    val stepRepo: StepRepository = LocalStepRepository(),
    val questionRepo: QuestionSource = QuestionSource(),
    val tagRepo: LocalTagRepository = LocalTagRepository()
): SubModel<PathContextState>() {

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
        tagRepo.flowTagsByStepId(pathId).launchCollect { tags ->
            setState { it.copy(tags = tags) }
        }
    }

    fun setFocus(stepId: StepId?) {
        setState { it.copy(selectedStepId = stepId) }
    }

    fun toggleFocus(stepId: StepId) {
        when (stepId) {
            stateNow.selectedStepId -> setFocus(null)
            else -> setFocus(stepId)
        }
    }
}

@Stable
data class PathContextState(
    val step: Step? = null,
    val steps: List<Step> = emptyList(),
    val questions: Map<StepId, List<Question>> = emptyMap(),
    val tags: List<Tag> = emptyList(),
    val selectedStepId: String? = null,
)