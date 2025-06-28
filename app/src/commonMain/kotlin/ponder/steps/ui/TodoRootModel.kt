package ponder.steps.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kabinet.utils.startOfDay
import kotlinx.datetime.Clock
import ponder.steps.model.data.TrekId
import kotlin.time.Duration.Companion.days

class TodoRootModel(
    loadTrek: (TrekId?) -> Unit
): ViewModel() {

    val treks = object: TrekStepListModel(loadTrek, viewModelScope) {
        init {
            val start = Clock.startOfDay()
            val end = start + 1.days
            trekRepo.flowRootTrekSteps(start, end).launchCollect { trekSteps ->
                setState { it.copy(steps = trekSteps.sortedByDescending { trek -> trek.availableAt }) }
            }
            stepLogRepo.flowRootLogs(start, end).launchCollect(::setLogs)
            questionRepo.flowRootQuestions(start, end).launchCollect(::setQuestions)
            answerRepo.flowRootAnswers(start, end).launchCollect(::setAnswers)
        }
    }
}
