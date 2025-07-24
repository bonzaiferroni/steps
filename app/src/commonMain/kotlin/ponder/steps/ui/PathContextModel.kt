package ponder.steps.ui

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ponder.steps.db.TodoStep
import ponder.steps.db.TrekPointId
import ponder.steps.io.LocalAnswerRepository
import ponder.steps.io.LocalStepLogRepository
import ponder.steps.io.QuestionSource
import ponder.steps.io.LocalStepRepository
import ponder.steps.io.LocalTagRepository
import ponder.steps.io.LocalTrekRepository
import ponder.steps.io.StepOutcome
import ponder.steps.io.StepRepository
import ponder.steps.model.data.Answer
import ponder.steps.model.data.NewAnswer
import ponder.steps.model.data.Question
import ponder.steps.model.data.Step
import ponder.steps.model.data.StepId
import ponder.steps.model.data.StepLog
import ponder.steps.model.data.StepLogId
import ponder.steps.model.data.Tag
import ponder.steps.model.data.TagId
import ponder.steps.model.data.TrekId
import pondui.ui.core.SubModel
import pondui.ui.core.ViewState

class PathContextModel(
    override val viewModel: ViewModel,
    override val state: ViewState<PathContextState>,
    private val stepRepo: StepRepository = LocalStepRepository(),
    private val questionRepo: QuestionSource = QuestionSource(),
    private val tagRepo: LocalTagRepository = LocalTagRepository(),
    private val stepLogRepo: LocalStepLogRepository = LocalStepLogRepository(),
    private val answersRepo: LocalAnswerRepository = LocalAnswerRepository(),
    private val trekRepo: LocalTrekRepository = LocalTrekRepository(),
): SubModel<PathContextState>() {

    fun setParameters(pathId: StepId, trekPath: TrekPath?) {
        clearJobs()
        setState { it.copy(trekPath = trekPath) }
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
        trekPath?.let { trekPath ->
            val trekPointId = trekPath.trekPointId
            stepLogRepo.flowPathLogsByTrekPointId(pathId, trekPointId).launchCollect { stepLogs ->
                setState { it.copy(stepLogs = stepLogs) }
            }
            answersRepo.flowPathAnswersByTrekId(pathId, trekPointId).launchCollect { answers ->
                setState { it.copy(answers = answers) }
            }
            trekRepo.flowPathProgresses(pathId, trekPointId).launchCollect { progresses ->
                setState { it.copy(progresses = progresses) }
            }
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

    fun setOutcome(step: Step, outcome: StepOutcome? = null) {
        val trekPointId = stateNow.trekPath?.trekPointId ?: return
        viewModelScope.launch {
            trekRepo.setFinished(trekPointId, step, outcome, stateNow.trekPath?.breadcrumbs)
        }
    }

    fun answerQuestion(step: Step, stepLog: StepLog, question: Question, answerText: String?) {
        println(stateNow.trekPath?.trekPointId)
        val trekPointId = stateNow.trekPath?.trekPointId ?: return
        viewModelScope.launch {
            trekRepo.createAnswer(
                trekPointId = trekPointId,
                step = step,
                answer = NewAnswer(stepLog.id, question.id, answerText, question.type),
                breadcrumbs = stateNow.trekPath?.breadcrumbs
            )
        }
    }
}

@Stable
data class PathContextState(
    val trekPath: TrekPath? = null,
    val step: Step? = null,
    val steps: List<Step> = emptyList(),
    val questions: Map<StepId, List<Question>> = emptyMap(),
    val tags: List<Tag> = emptyList(),
    val selectedStepId: String? = null,
    val stepLogs: List<StepLog> = emptyList(),
    val answers: Map<StepLogId, List<Answer>> = emptyMap(),
    val progresses: Map<String, Int> = emptyMap(),
) {
    fun getLog(step: Step) = stepLogs.firstOrNull { it.pathStepId == step.pathStepId }
    fun getAnswers(stepLogId: StepLogId) = answers[stepLogId] ?: emptyList()
    fun getProgress(step: Step) = step.pathStepId?.let { pathStepId -> progresses[pathStepId] }

    val progress get() = stepLogs.size
    val totalSteps get() = steps.size
    val progressRatio get() = totalSteps.takeIf { it > 0 }?.let { progress / it.toFloat() }
}