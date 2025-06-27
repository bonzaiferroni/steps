package ponder.steps.io

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ponder.steps.model.data.Answer
import ponder.steps.model.data.DataType
import ponder.steps.model.data.NewAnswer
import ponder.steps.model.data.StepOutcome
import ponder.steps.model.data.Question
import ponder.steps.model.data.TrekId
import ponder.steps.model.data.TrekItem
import ponder.steps.model.data.TrekStep

interface TrekRepository {

    /**
     * Create new treks for active intents without a corresponding trek
     */
    suspend fun syncTreksWithIntents()

    /**
     * Complete the current step of the trek
     * @param trekId The id of the trek to complete the step for
     * @return True if the step was completed successfully, false otherwise
     */
    suspend fun setOutcome(
        trekId: String,
        stepId: String,
        pathStepId: String?,
        outcome: StepOutcome?
    ): String?

    suspend fun completeTrek(trekId: String): Boolean

    suspend fun isFinished(trekId: String): Boolean

    suspend fun createSubTrek(trekId: String, pathStepId: String): String

    fun flowTrekStepById(trekId: String): Flow<TrekStep>

    fun flowTrekStepsBySuperId(superId: String): Flow<List<TrekStep>>

    fun flowRootTrekSteps(start: Instant, end: Instant): Flow<List<TrekStep>>

    suspend fun createAnswer(trekId: TrekId, answer: NewAnswer): Boolean
}