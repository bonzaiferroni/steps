package ponder.steps.ui

import androidx.compose.runtime.Stable
import kabinet.utils.startOfDay
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.datetime.Clock
import ponder.steps.io.LocalTagRepository
import ponder.steps.io.LocalTrekRepository
import ponder.steps.io.TrekRepository
import ponder.steps.model.data.StepId
import ponder.steps.model.data.Tag
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
        val start = Clock.startOfDay()
        val end = start + 1.days
        trekRepo.flowRootTrekSteps(start, end).launchCollect { trekSteps ->
            treks.setTrekSteps(trekSteps.sortedByDescending { trek -> trek.availableAt })
            flowTags(trekSteps)
        }
    }

    private var tagJob: Job? = null

    private fun flowTags(trekSteps: List<TrekStep>) {
        tagJob?.cancel()
        tagJob = tagRepo.flowTagsByStepIds(trekSteps.map { it.stepId }).launchCollect { tags ->
            val tagSet = tags.values.flatten().toImmutableList()
            setState { it.copy(tags = tags, tagSet = tagSet) }
        }
    }

    fun clickTag(tag: Tag) {
        if (tag != stateNow.selectedTag) {
            treks.setFilter {
                val tags = stateNow.tags[it.stepId] ?: return@setFilter false
                tags.contains(tag)
            }
            setState { it.copy(selectedTag = tag) }
        } else {
            treks.setFilter(null)
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