package ponder.steps.ui

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ponder.steps.io.AiClient
import ponder.steps.io.QuestionSource
import ponder.steps.io.LocalStepRepository
import ponder.steps.io.LocalTagRepository
import ponder.steps.io.MaterialSource
import ponder.steps.io.StepRepository
import ponder.steps.model.data.NewStep
import ponder.steps.model.data.Question
import ponder.steps.model.data.Material
import ponder.steps.model.data.MaterialType
import ponder.steps.model.data.MaterialUnit
import ponder.steps.model.data.Step
import ponder.steps.model.data.StepId
import ponder.steps.model.data.StepSuggestRequest
import ponder.steps.model.data.StepWithDescription
import ponder.steps.model.data.TagId
import ponder.steps.model.data.UnitType
import ponder.steps.model.data.StepMaterialId
import ponder.steps.model.data.StepMaterialJoin
import pondui.LocalValueRepository
import pondui.ValueRepository
import pondui.ui.core.StateModel
import pondui.ui.core.ViewState

@Stable
class PathEditorModel(
    val stepRepo: StepRepository = LocalStepRepository(),
    val aiClient: AiClient = AiClient(),
    val valueRepo: ValueRepository = LocalValueRepository(),
    val questionRepo: QuestionSource = QuestionSource(),
    val tagRepo: LocalTagRepository = LocalTagRepository(),
    val materialSource: MaterialSource = MaterialSource(),
): StateModel<PathEditorState>() {
    override val state = ViewState(PathEditorState())

    val messenger = MessengerModel(this)
    private val pathContextState = ViewState(PathContextState())
    val pathContext = PathContextModel(this, pathContextState)
    private val contextStep get() = pathContextState.value.step

    fun setParameters(pathId: StepId) {
        pathContext.setParameters(pathId, null)
    }

    fun editStep(step: Step) {
        viewModelScope.launch {
            stepRepo.updateStep(step)
        }
    }

    fun removeStepFromPath(step: Step) {
        val path = contextStep ?: return
        val position = step.position ?: return
        viewModelScope.launch {
            stepRepo.removeStepFromPath(path.id, step.id, position)
        }
    }

    fun moveStep(step: Step, delta: Int) {
        val pathStepId = step.pathStepId ?: error("Missing pathStepId")
        viewModelScope.launch {
            stepRepo.moveStepPosition(pathStepId, delta)
        }
    }

    fun suggestNextStep() {
        val path = contextStep ?: return
        val steps = pathContextState.value.steps
        viewModelScope.launch {
            val response = aiClient.suggestStep(StepSuggestRequest(
                pathLabel = path.label,
                pathDescription = path.description,
                precedingSteps = steps.map { StepWithDescription(it.label, it.description) }
            ))
            setState { it.copy(suggestions = response.suggestedSteps) }
        }
    }

    fun toggleAddingStep() {
        setState { it.copy(isAddingStep = !it.isAddingStep) }
    }

    fun createStepFromSuggestion(suggestion: StepWithDescription) {
        viewModelScope.launch {
            createStep(suggestion.label, suggestion.description)
            val suggestions = stateNow.suggestions.filter { it != suggestion }
            setState { it.copy(suggestions = suggestions) }
        }
    }

    private suspend fun createStep(label: String, description: String? = null, position: Int? = null) {
        val path = contextStep ?: return
        val stepId = stepRepo.createStep(NewStep(
            pathId = path.id,
            label = label,
            position = position,
            description = description
        ))
        val theme = path.theme ?: valueRepo.readString(SETTINGS_DEFAULT_IMAGE_THEME)
        if (theme.isNotEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                val step = stepRepo.readStepById(stepId)
                if (step != null) {
                    val defaultTheme = valueRepo.readString(SETTINGS_DEFAULT_IMAGE_THEME)
                    val url = aiClient.generateImage(step, path, defaultTheme)
                    stepRepo.updateStep(step.copy(imgUrl = url.url, thumbUrl = url.thumbUrl))
                }
            }
        }
    }

    fun addDescription() {
        val step = contextStep ?: error("missing step")
        pathContextState.setValue { it.copy(step = step.copy(description = "")) }
    }

    fun setEditQuestionRequest(request: EditQuestionRequest?) = setState { it.copy(editQuestionRequest = request) }

    fun deleteQuestion(question: Question) {
        viewModelScope.launch {
            questionRepo.deleteQuestion(question)
        }
    }

    fun addNewStep(value: String) {
        viewModelScope.launch {
            createStep(value, null, stateNow.newStepPosition)
            setState { it.copy(newStepLabel = "") }
        }
    }

    fun moveNewStep(positionDelta: Int) {
        val pathSize = pathContextState.value.steps.size
        val currentPosition = stateNow.newStepPosition ?: pathSize
        val newPosition = maxOf(0, currentPosition + positionDelta)
        setState { it.copy(newStepPosition = newPosition.takeIf { newPosition < pathSize })}
    }

    fun addNewTag(stepId: StepId) {
        if (!stateNow.isValidNewTagLabel) return

        viewModelScope.launch {
            tagRepo.addTag(stepId, stateNow.newTagLabel)
            setState { it.copy(newStepLabel = "") }
        }
    }

    fun removeTag(stepId: StepId, tagId: TagId) {
        viewModelScope.launch {
            tagRepo.removeTag(stepId, tagId)
        }
    }

    fun setNewMaterialLabel(value: String) {
        if (value.isEmpty()) {
            setState { it.copy(newMaterialLabel = value, materialSuggestions = emptyList()) }
        } else {
            setState { it.copy(newMaterialLabel = value) }
            viewModelScope.launch {
                val materialType = stateNow.newMaterialType
                val materialSuggestions = materialSource.searchMaterials(value, materialType)
                setState { it.copy(materialSuggestions = materialSuggestions) }
                println("assigned suggestions")
            }
        }
    }

    fun addMaterial(material: Material? = null) {
        val stepId = pathContextState.value.step?.id ?: return
        val newMaterialLabel = stateNow.newMaterialLabel.takeIf { it.isNotEmpty() } ?: return
        val quantity = stateNow.newMaterialQuantity
        viewModelScope.launch {
            val materialType = stateNow.newMaterialType; val unitType = stateNow.newUnitType
            val material = material ?: stateNow.matchedMaterial
                ?: materialSource.createNewMaterial(newMaterialLabel, materialType, unitType)
            if (material == null) {
                messenger.setError("Unable to provide material")
                return@launch
            }
            val materialUnit = stateNow.newMaterialUnit
            materialSource.createNewStepMaterial(
                materialId = material.id,
                stepId = stepId,
                quantity = quantity,
                materialUnit = materialUnit
            )
            setState { it.copy(newMaterialLabel = "", newMaterialQuantity = 1f) }
        }
    }

    fun removeMaterial(material: StepMaterialJoin) {
        viewModelScope.launch {
            materialSource.deleteStepMaterialById(material.id)
        }
    }

    fun setStepMaterialQuantity(stepMaterialId: StepMaterialId, quantity: Float) {
        viewModelScope.launch {
            materialSource.updateStepMaterialQuantity(stepMaterialId, quantity)
        }
    }

    fun setNewMaterialTab(value: String) {
        val unitType = when (value) {
            ADD_TOOL_LABEL -> UnitType.Quantity
            else -> stateNow.newUnitType
        }
        setState { it.copy(newMaterialTab = value, newUnitType = unitType) }
    }

    fun setNewUnitType(value: UnitType) {
        setState { it.copy(newUnitType = value, newMaterialUnit = value.defaultUnit) }
    }

    fun setNewMaterialUnit(value: MaterialUnit) {
        setState { it.copy(newMaterialUnit = value) }
    }

    fun setNewMaterialQuantity(value: Float) {
        setState { it.copy(newMaterialQuantity = value) }
    }
}

data class PathEditorState(
    val newTagLabel: String = "",
    val suggestions: List<StepWithDescription> = emptyList(),
    val isAddingStep: Boolean = false,
    val editQuestionRequest: EditQuestionRequest? = null,
    val newStepLabel: String = "",
    val newStepPosition: Int? = null,
    val newMaterialLabel: String = "",
    val newUnitType: UnitType = UnitType.Weight,
    val newMaterialUnit: MaterialUnit = MaterialUnit.Grams,
    val materialSuggestions: List<Material> = emptyList(),
    val newMaterialQuantity: Float = 1f,
    val newMaterialTab: String = ADD_TOOL_LABEL
) {
    val isValidNewTagLabel get() = newTagLabel.isNotBlank()
    val matchedMaterial get() = materialSuggestions.firstOrNull()?.takeIf { it.label.equals(newMaterialLabel, true) }
    val newMaterialType: MaterialType get() = when (newMaterialTab) {
        ADD_TOOL_LABEL -> MaterialType.Tool
        else -> MaterialType.Ingredient
    }
}

const val ADD_TOOL_LABEL = "add tool"
const val ADD_INGREDIENT_LABEL = "add ingredient"