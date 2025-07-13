package ponder.steps.io

import kotlinx.coroutines.flow.Flow
import ponder.steps.model.data.NewStep
import ponder.steps.model.data.Step

interface StepRepository {

    /**
     * Create a new Step.
     *
     * @param newStep The NewStep data to create the Step from
     * @return The id of the newly created Step
     */
    suspend fun createStep(newStep: NewStep): String

    /**
     * Update a step.
     *
     * @param step The Step to update
     * @return True if the update was successful, false otherwise
     */
    suspend fun updateStep(step: Step): Boolean

    /**
     * Add a Step to a Path.
     *
     * @param pathId The id of the Path to which to add the Step
     * @param stepId The id of the Step to add
     * @param position The position in the Path where the Step should be added, null for end
     * @return True if the addition was successful, false otherwise
     */
    suspend fun addStepToPath(pathId: String, stepId: String, position: Int?): Boolean

    /**
     * Move a step's position within a path.
     *
     * @param pathId The id of the path containing the step
     * @param stepId The id of the Step to move
     * @param delta The change in position (positive to move down, negative to move up)
     * @return True if the move was successful, false otherwise
     */
    suspend fun moveStepPosition(pathId: String, stepId: String, delta: Int): Boolean

    /**
     * Delete a step.
     *
     * @param stepId The id of the Step to move
     * @return True if the deletion was successful, false otherwise
     */
    suspend fun deleteStep(stepId: String): Boolean

    /**
     * Remove a Step from a Path.
     *
     * @param pathId The id of the Path from which to remove the Step
     * @param stepId The id of the Step to remove
     * @param position The position of the Step in the Path to remove
     * @return True if the removal was successful, false otherwise
     */
    suspend fun removeStepFromPath(pathId: String, stepId: String, position: Int): Boolean

    /**
     * Read a Step by id.
     *
     * @param stepId The id of the Step to read
     * @return The Step with the given id, or null if not found
     */
    suspend fun readStepById(stepId: String): Step?

    /**
     * Flow a step by id
     *
     * @param stepId The id of the Step to flow
     * @return A Flow that emits the Step with the given id, or null if not found
     */
    fun flowStep(stepId: String): Flow<Step>

    /**
     * Read the child steps associated with a path. A path is also a step that has associated children.
     *
     * @param pathId The id of the parent step
     * @return The child Steps associated with the given path id, empty if none are found
     */
    suspend fun readPathSteps(pathId: String): List<Step>

    /**
     * Flow the child steps associated with a path.
     *
     * @param pathId The id of the parent step
     * @return A Flow that emits a list of child Steps associated with the given path id
     */
    fun flowPathSteps(pathId: String): Flow<List<Step>>

    /**
     * Read the root steps, which are steps that do not have a parent path.
     *
     * @return The root Steps, empty if none are found
     */
    suspend fun readRootSteps(limit: Int = 20): List<Step>

    /**
     * Flow the root steps, which are steps that do not have a parent path.
     *
     * @return A Flow that emits a list of root Steps
     */
    fun flowRootSteps(limit: Int = 20): Flow<List<Step>>

    /**
     * Search for steps by text.
     *
     * @param text The text to search for in step labels
     * @return A list of Steps that match the search criteria
     */
    suspend fun readSearch(text: String, limit: Int = 20): List<Step>

    /**
     * Flow step search results by text.
     *
     * @param text The text to search for in step labels
     * @return A Flow that emits a list of Steps that match the search criteria
     */
    fun flowSearch(text: String, limit: Int =20): Flow<List<Step>>

    suspend fun isValidPathStep(pathId: String, stepId: String): Boolean
}