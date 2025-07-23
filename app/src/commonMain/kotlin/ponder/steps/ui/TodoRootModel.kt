package ponder.steps.ui

import androidx.compose.runtime.Stable
import kabinet.utils.startOfDay
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import ponder.steps.db.TodoStep
import ponder.steps.io.LocalAnswerRepository
import ponder.steps.io.QuestionSource
import ponder.steps.io.LocalStepLogRepository
import ponder.steps.io.LocalStepRepository
import ponder.steps.io.LocalTagRepository
import ponder.steps.io.LocalTrekRepository
import ponder.steps.model.data.*
import pondui.ui.core.StateModel
import pondui.ui.core.ViewState

class TodoRootModel(
    navToTrekPath: (TrekPath?, Boolean) -> Unit,
    private val tagRepo: LocalTagRepository = LocalTagRepository(),
    private val trekRepo: LocalTrekRepository = LocalTrekRepository(),
    private val stepRepo: LocalStepRepository = LocalStepRepository(),
    private val questionRepo: QuestionSource = QuestionSource(),
    private val answerRepo: LocalAnswerRepository = LocalAnswerRepository(),
    private val stepLogRepo: LocalStepLogRepository = LocalStepLogRepository(),
) : StateModel<TodoRootState>() {

    override val state = ViewState(TodoRootState())

    val todoList = TodoListModel(
        viewModel = this,
        trekPath = null,
        navToTrekPath = navToTrekPath,
    )

    init {
        val start = Clock.startOfDay()

        todoList.setFlows(
            stepFlow = trekRepo.flowRootTodoSteps(start).map { todoSteps ->
                todoSteps.sortedWith(
                    compareBy<TodoStep> { if (it.isComplete != true) 0 else 1 }
                        .thenByDescending { it.trekPointId }
                )
            },
            stepLogFlow = stepLogRepo.flowRootLogs(start),
            questionFlow = questionRepo.flowRootQuestions(start),
            answerFlow = answerRepo.flowRootAnswers(start),
            progressFlow = trekRepo.flowRootProgress(start)
        )

        tagRepo.flowRootTags(start).launchCollect { tags ->
            val tagSet = tags.values.flatten().toImmutableList()
            setState { it.copy(tags = tags, tagSet = tagSet) }
        }
    }

    fun clickTag(tag: Tag) {
        if (tag != stateNow.selectedTag) {
            todoList.setFilter {
                val tags = stateNow.tags[it.step.id] ?: return@setFilter false
                tags.contains(tag)
            }
            setState { it.copy(selectedTag = tag) }
        } else {
            todoList.setFilter(null)
            setState { it.copy(selectedTag = null) }
        }
    }
}

@Stable
data class TodoRootState(
    val selectedTag: Tag? = null,
    val tags: Map<StepId, List<Tag>> = emptyMap(),
    val tagSet: ImmutableList<Tag> = persistentListOf()
)