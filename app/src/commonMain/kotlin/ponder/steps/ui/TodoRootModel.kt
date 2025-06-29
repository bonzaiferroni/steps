package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kabinet.utils.startOfDay
import kotlinx.datetime.Clock
import ponder.steps.db.TagCount
import ponder.steps.io.AnswerRepository
import ponder.steps.io.LocalAnswerRepository
import ponder.steps.io.LocalQuestionRepository
import ponder.steps.io.LocalStepLogRepository
import ponder.steps.io.LocalTagRepository
import ponder.steps.io.LocalTrekRepository
import ponder.steps.io.QuestionRepository
import ponder.steps.io.StepLogRepository
import ponder.steps.io.TrekRepository
import ponder.steps.model.data.Tag
import ponder.steps.model.data.TagId
import ponder.steps.model.data.TrekId
import ponder.steps.model.data.TrekStep
import pondui.ui.core.StateModel
import kotlin.time.Duration.Companion.days

class TodoRootModel(
    loadTrek: (TrekId?, Boolean) -> Unit,
    private val tagRepo: LocalTagRepository = LocalTagRepository(),
    private val trekRepo: TrekRepository = LocalTrekRepository(),
): StateModel<TodoRootState>(TodoRootState()) {

    val treks = TrekStepListModel(this, loadTrek)

    init {
        tagRepo.flowTopTagCounts().launchCollect { tags ->
            setState { it.copy(tags = tags) }
        }

        val start = Clock.startOfDay()
        val end = start + 1.days
        trekRepo.flowRootTrekSteps(start, end).launchCollect { trekSteps ->
            treks.setTrekSteps(trekSteps.sortedByDescending { trek -> trek.availableAt })
            flowTags(trekSteps)
        }
    }

    private fun flowTags(trekSteps: List<TrekStep>) {
        // tagRepo.flowTagsByStepIds(trekSteps.map { it.stepId })
    }
}

data class TodoRootState(
    val tagId: TagId? = null,
    val tags: List<TagCount> = emptyList(),
)