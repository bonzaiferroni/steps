package ponder.steps.ui

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ponder.steps.db.TodoStep
import ponder.steps.db.TrekPointId
import ponder.steps.io.LocalStepLogRepository
import ponder.steps.io.LocalTrekRepository
import pondui.ui.core.StateModel
import java.util.PriorityQueue
import kotlin.time.Duration.Companion.minutes

class LineLogModel(
    private val stepLogRepo: LocalStepLogRepository = LocalStepLogRepository(),
    private val trekRepo: LocalTrekRepository = LocalTrekRepository(),
): StateModel<LineLogState>(LineLogState()) {

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
                val minMinutes = stateNow.minPerPx * LOG_LANE_WIDTH
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
                    lane      = lane,
                    trekPointId    = ts.trekPointId,
                    startAt   = lineStart,
                    endAt     = lineEnd,
                    imgUrl    = ts.step.thumbUrl,
                    isComplete= ts.isComplete ?: false
                )
            }

        setState {
            it.copy(lines = logLines)
        }
    }
}

data class LineLogState(
    val start: Instant = Instant.DISTANT_PAST,
    val end: Instant = Instant.DISTANT_PAST,
    val minPerPx: Float = 1f,
    val lines: List<LogLine> = emptyList()
)

data class LogLine(
    val lane: Int,
    val trekPointId: TrekPointId,
    val startAt: Instant,
    val endAt: Instant,
    val imgUrl: String?,
    val isComplete: Boolean,
)

const val LOG_LANE_WIDTH = 30
const val LOG_LANE_GAP = 5