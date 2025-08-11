package ponder.steps.ui

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import kabinet.utils.startOfDay
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import ponder.steps.db.TodoStep
import ponder.steps.db.TrekPointId
import ponder.steps.io.LocalAnswerRepository
import ponder.steps.io.QuestionSource
import ponder.steps.io.LocalStepLogRepository
import ponder.steps.io.LocalStepRepository
import ponder.steps.io.LocalTagRepository
import ponder.steps.io.LocalTrekRepository
import ponder.steps.io.StepOutcome
import ponder.steps.model.data.*
import pondui.ui.core.StateModel
import pondui.ui.core.ModelState

class TodoRootModel(
    navToTrekPath: (TrekPath?, Boolean) -> Unit,
    private val tagRepo: LocalTagRepository = LocalTagRepository(),
    private val trekRepo: LocalTrekRepository = LocalTrekRepository(),
    private val stepRepo: LocalStepRepository = LocalStepRepository(),
    private val questionRepo: QuestionSource = QuestionSource(),
    private val answerRepo: LocalAnswerRepository = LocalAnswerRepository(),
    private val stepLogRepo: LocalStepLogRepository = LocalStepLogRepository(),
) : StateModel<TodoRootState>() {

    override val state = ModelState(TodoRootState())

    private var allSteps: List<TodoStep> = emptyList()
    var stepFilter: ((TodoStep) -> Boolean)? = null

    init {
        val start = Clock.startOfDay()

        trekRepo.flowRootTodoSteps(start).launchCollect { todoSteps ->
            allSteps = todoSteps.sortedWith(
                compareBy<TodoStep> { if (it.isComplete != true) 0 else 1 }
                    .thenByDescending { it.trekPointId }
            )
            refreshSteps()
        }
        stepLogRepo.flowRootLogs(start).launchCollect { stepLogs -> setState { it.copy(stepLogs = stepLogs) } }
        questionRepo.flowRootQuestions(start).launchCollect { questions -> setState { it.copy(questions = questions) } }
        answerRepo.flowRootAnswers(start).launchCollect { answers -> setState { it.copy(answers = answers) } }
        trekRepo.flowRootProgress(start).launchCollect { progresses -> setState { it.copy(progresses = progresses) } }

        tagRepo.flowRootTags(start).launchCollect { tags ->
            val tagSet = tags.values.flatten().toImmutableList()
            setState { it.copy(tags = tags, tagSet = tagSet) }
        }
    }

    fun clickTag(tag: Tag) {
        if (tag != stateNow.selectedTag) {
            setFilter {
                val tags = stateNow.tags[it.step.id] ?: return@setFilter false
                tags.contains(tag)
            }
            setState { it.copy(selectedTag = tag) }
        } else {
            setFilter(null)
            setState { it.copy(selectedTag = null) }
        }
    }

    private fun refreshSteps() {
        val filteredSteps = stepFilter?.let { filter -> allSteps.filter { filter(it) } } ?: allSteps
        setState { it.copy(todoSteps = filteredSteps) }
    }

    fun toggleAddItem() {
        setState { it.copy(isAddingItem = !it.isAddingItem) }
    }

    fun setOutcome(trekPointId: TrekPointId, step: Step, outcome: StepOutcome? = null) {
        viewModelScope.launch {
            trekRepo.setFinished(trekPointId, step, outcome, null)
        }
    }

    fun answerQuestion(trekPointId: TrekPointId, step: Step, stepLog: StepLog, question: Question, answerText: String?) {
        viewModelScope.launch {
            trekRepo.createAnswer(
                trekPointId = trekPointId,
                step = step,
                answer = NewAnswer(stepLog.id, question.id, answerText, question.type),
                breadcrumbs = null
            )
        }
    }

    fun setFilter(filter: ((TodoStep) -> Boolean)?) {
        stepFilter = filter
        refreshSteps()
    }
}

@Stable
data class TodoRootState(
    val selectedTag: Tag? = null,
    val tags: Map<StepId, List<Tag>> = emptyMap(),
    val tagSet: ImmutableList<Tag> = persistentListOf(),
    val todoSteps: List<TodoStep> = emptyList(),
    val stepLogs: List<StepLog> = emptyList(),
    val questions: Map<StepId, List<Question>> = emptyMap(),
    val answers: Map<StepLogId, List<Answer>> = emptyMap(),
    val progresses: Map<String, Int> = emptyMap(),
    val isAddingItem: Boolean = false,
) {
    fun getLog(todoStep: TodoStep) = todoStep.trekId?.let { trekId -> stepLogs.firstOrNull { it.trekId == trekId  } }

    fun getAnswers(stepLogId: StepLogId) = answers[stepLogId] ?: emptyList()

    val progress get() = stepLogs.size
    val totalSteps get() = todoSteps.size
    val progressRatio get() = progress / (totalSteps.takeIf { it > 0 } ?: 1).toFloat()
}