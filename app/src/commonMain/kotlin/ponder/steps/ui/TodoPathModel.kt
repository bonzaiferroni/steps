package ponder.steps.ui

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.map
import ponder.steps.io.LocalAnswerRepository
import ponder.steps.io.QuestionSource
import ponder.steps.io.LocalStepLogRepository
import ponder.steps.io.LocalStepRepository
import ponder.steps.io.LocalTrekRepository
import ponder.steps.model.data.Step
import pondui.ui.core.StateModel
import pondui.ui.core.ViewState

class TodoPathModel(
    private val trekPath: TrekPath,
    navToTrekPath: (TrekPath?, Boolean) -> Unit,
    private val trekRepo: LocalTrekRepository = LocalTrekRepository(),
    private val stepRepo: LocalStepRepository = LocalStepRepository(),
    private val stepLogRepo: LocalStepLogRepository = LocalStepLogRepository(),
    private val questionsRepo: QuestionSource = QuestionSource(),
    private val answersRepo: LocalAnswerRepository = LocalAnswerRepository(),
) : StateModel<TodoPathState>() {

    override val state = ViewState(TodoPathState())
    private val pathContextState = ViewState(PathContextState())
    val pathContext = PathContextModel(this, pathContextState)

    fun activate() {
        val pathId = trekPath.pathId
        pathContext.setParameters(pathId, trekPath)
    }

    fun deactivate() {
        pathContext.clearJobs()
    }
}

data class TodoPathState(
    val step: Step? = null,
)