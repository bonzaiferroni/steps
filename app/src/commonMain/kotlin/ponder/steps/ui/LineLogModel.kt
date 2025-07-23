package ponder.steps.ui

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ponder.steps.db.TodoStep
import ponder.steps.db.TrekPointId
import ponder.steps.io.LocalStepLogRepository
import ponder.steps.io.LocalTrekRepository
import ponder.steps.io.NextStep
import ponder.steps.io.StepOutcome
import ponder.steps.model.data.NewAnswer
import pondui.ui.core.StateModel
import pondui.ui.core.ViewState
import java.util.PriorityQueue
import kotlin.time.Duration.Companion.minutes

class LineLogModel(
    private val stepLogRepo: LocalStepLogRepository = LocalStepLogRepository(),
    private val trekRepo: LocalTrekRepository = LocalTrekRepository(),
) : StateModel<LineLogState>() {
    override val state = ViewState(LineLogState())

    fun setParameters(start: Instant, end: Instant) {
        setState { it.copy(start = start, end = end) }
        clearJobs()
        trekRepo.flowRootTodoSteps(start).launchCollect { todoSteps ->
            renderSteps(todoSteps)
        }
    }

    private val heap = PriorityQueue<Pair<Instant, Int>>(compareBy { it.first })

    private fun renderSteps(todoSteps: List<TodoStep>) {
        val now = Clock.System.now()
        heap.clear()
        var nextLane = 0

        val logLines = todoSteps
            .sortedBy { it.startedAt ?: now }
            .map { ts ->
                val trekStart = ts.startedAt ?: now
                val trekEnd = ts.finishedAt ?: now
                val trekDuration = (trekEnd - trekStart)
                val minMinutes = stateNow.dpPerHour / 60 * LOG_LANE_WIDTH
                val lineMinutes = maxOf(minMinutes.toLong(), trekDuration.inWholeMinutes)
                val lineStart = trekEnd - lineMinutes.minutes
                val lineEnd = trekEnd

                // pick or create a lane
                val lane = heap.peek()
                    ?.takeIf { it.first <= lineStart }
                    ?.let { (_, idx) ->
                        heap.poll()
                        idx
                    }
                    ?: nextLane.also { nextLane++ }

                heap.add(lineEnd to lane)

                LogLine(
                    lane = lane,
                    trekPointId = ts.trekPointId,
                    startAt = ts.startedAt,
                    endAt = ts.finishedAt,
                    imgUrl = ts.step.thumbUrl,
                    isComplete = ts.isComplete ?: false
                )
            }

        setState {
            it.copy(lines = logLines)
        }
    }

    fun setOpenMenuId(value: TrekPointId?) {
        setState { it.copy(openMenuId = value, nextStep = null) }
        if (value == null) return
        viewModelScope.launch {
            val nextStep = trekRepo.readNextStep(value)
            setState { it.copy(nextStep = nextStep) }
        }
    }

    fun setComplete(nextStep: NextStep) {
        viewModelScope.launch {
            trekRepo.setFinished(nextStep.trekPointId, nextStep.step, StepOutcome.Finished, nextStep.breadcrumbs)
            val nextStep = trekRepo.readNextStep(nextStep.trekPointId)
            setState { it.copy(nextStep = nextStep) }
        }
    }

    fun answerQuestion(nextStep: NextStep, answerText: String?) {
        val trekId = nextStep.trek?.id ?: error("Missing trek")
        val logId = nextStep.stepLog?.id ?: error("Missing stepLog")
        val question = nextStep.question ?: error("Missing question")
        viewModelScope.launch {
            trekRepo.createAnswer(
                trekId = trekId,
                step = nextStep.step,
                answer = NewAnswer(
                    stepLogId = logId,
                    questionId = question.id,
                    value = answerText,
                    type = question.type
                ),
                breadcrumbs = nextStep.breadcrumbs
            )
            val nextStep = trekRepo.readNextStep(nextStep.trekPointId)
            setState { it.copy(nextStep = nextStep) }
        }
    }

    fun toggleIsAddingStep() {
        setState { it.copy(isAddingStep = !it.isAddingStep) }
    }
}

@Stable
data class LineLogState(
    val start: Instant = Instant.DISTANT_PAST,
    val end: Instant = Instant.DISTANT_PAST,
    val dpPerHour: Float = 100f,
    val lines: List<LogLine> = emptyList(),
    val openMenuId: TrekPointId? = null,
    val nextStep: NextStep? = null,
    val isAddingStep: Boolean = false,
) {
    val minPerDp get() = 60 / dpPerHour
}

data class LogLine(
    val lane: Int,
    val trekPointId: TrekPointId,
    val startAt: Instant?,
    val endAt: Instant?,
    val imgUrl: String?,
    val isComplete: Boolean,
)

const val LOG_LANE_WIDTH = 50
const val LOG_LANE_GAP = 5
