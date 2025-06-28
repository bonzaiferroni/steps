package ponder.steps.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kabinet.utils.startOfDay
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.days

class TodoRootModel(): ViewModel() {

    val treks = object: TrekStepListModel(viewModelScope) {
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
