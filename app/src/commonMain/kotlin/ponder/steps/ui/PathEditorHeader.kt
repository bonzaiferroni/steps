package ponder.steps.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Trash
import kabinet.utils.pluralize
import kotlinx.collections.immutable.toImmutableList
import ponder.steps.model.data.MaterialType
import ponder.steps.model.data.MaterialUnit
import ponder.steps.model.data.UnitType
import ponder.steps.model.data.Step
import ponder.steps.model.data.StepMaterialJoin
import pondui.ui.behavior.AlignX
import pondui.ui.behavior.drawLabel
import pondui.ui.behavior.drawSection
import pondui.ui.behavior.padBottom
import pondui.ui.controls.Button
import pondui.ui.controls.Column
import pondui.ui.controls.DropMenu
import pondui.ui.controls.EditText
import pondui.ui.controls.Expando
import pondui.ui.controls.IntegerMenu
import pondui.ui.controls.MoreMenuItem
import pondui.ui.controls.MoreMenu
import pondui.ui.controls.Row
import pondui.ui.controls.Section
import pondui.ui.controls.Text
import pondui.ui.controls.TextField
import pondui.ui.controls.TextFieldMenu
import pondui.ui.theme.Pond
import pondui.utils.addShadow

@Composable
fun PathEditorHeader(
    pathStep: Step,
    viewModel: PathEditorModel
) {
    val state by viewModel.stateFlow.collectAsState()
    val pathContextState by viewModel.pathContext.stateFlow.collectAsState()
    Column(
        spacingUnits = 2,
        modifier = Modifier.animateContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            spacingUnits = 2,
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            StepImage(
                url = pathStep.imgUrl,
                modifier = Modifier
                    .drawLabel("step image", addPadding = true, alignX = AlignX.Center)
                    .fillMaxWidth(.33f)
                    .widthIn(max = 200.dp)
                    .aspectRatio(1f)
                    .clip(Pond.ruler.unitCorners)
            )

            EditText(
                text = pathStep.label,
                placeholder = "Step label",
                style = Pond.typo.h1.addShadow(),
                isContainerVisible = true,
                modifier = Modifier.drawLabel("step label", addPadding = true)
            ) { viewModel.editStep(pathStep.copy(label = it)) }
        }
        EditText(
            text = pathStep.description ?: "",
            placeholder = "Description",
            modifier = Modifier.fillMaxWidth().drawLabel("description"),
            isContainerVisible = true,
        ) { viewModel.editStep(pathStep.copy(description = it)) }

        val materials = pathContextState.stepMaterials
        val tools = remember(materials) { materials.filter { it.materialType == MaterialType.Tool } }
        val ingredients = remember(materials) { materials.filter { it.materialType == MaterialType.Ingredient } }

        if (tools.isNotEmpty()) {
            Column(
                spacingUnits = 1,
                modifier = Modifier.drawSection("${tools.size} tool${pluralize(tools.size)}")
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                for (stepMaterial in tools) {
                    MaterialRow(stepMaterial, viewModel)
                }
            }
        }
        if (ingredients.isNotEmpty()) {
            Column(
                spacingUnits = 1,
                modifier = Modifier.drawSection("${ingredients.size} ingredient${pluralize(ingredients.size)}")
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                for (stepMaterial in ingredients) {
                    MaterialRow(stepMaterial, viewModel)
                }
            }
        }
        Section() {
            Column(
                spacingUnits = 2,
                modifier = Modifier.width(IntrinsicSize.Max),
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    spacingUnits = 1,
                ) {
                    DropMenu(
                        selected = state.newMaterialType,
                        onSelect = viewModel::setNewMaterialType,
                    )

                    val matchedMaterial = state.matchedMaterial
                    val labelColor = when (matchedMaterial) {
                        null -> Pond.colors.creationVoid
                        else -> Pond.colors.selectionVoid
                    }
                    val label = when (matchedMaterial) {
                        null -> when (state.newMaterialLabel.isNotEmpty()) {
                            true -> "add ${state.newMaterialLabel}"
                            else -> "add ${state.newMaterialType.label.lowercase()}"
                        }

                        else -> "add ${matchedMaterial.label}"
                    }
                    TextFieldMenu(
                        text = state.newMaterialLabel,
                        items = state.materialSuggestions,
                        onTextChanged = viewModel::setNewMaterialLabel,
                        onEnterPressed = viewModel::addNewResource,
                        onChooseSuggestion = viewModel::addNewResource,
                        modifier = Modifier.weight(1f).drawLabel(label, labelColor)
                    ) { material ->
                        Text(material.label)
                    }
                }
                if (state.newMaterialType == MaterialType.Ingredient) {
                    Row(1) {
                        DropMenu(
                            selected = state.newUnitType,
                            onSelect = viewModel::setNewUnitType,
                        )
                        if (state.newUnitType != UnitType.Quantity) {
                            val materialUnitOptions = remember(state.newUnitType) {
                                MaterialUnit.entries.filter { println(it.unitType); it.unitType == state.newUnitType }.map { it.label }
                                    .toImmutableList()
                            }
                            DropMenu(
                                selected = state.newMaterialUnit.label,
                                options = materialUnitOptions,
                                onSelect = { value ->
                                    MaterialUnit.fromLabel(value)?.let { viewModel.setNewMaterialUnit(it) }
                                },
                            )
                        }
                        TextField(
                            text = state.newMaterialQuantity,
                            onTextChanged = viewModel::setNewMaterialQuantity,
                            label = state.newMaterialUnit.label,
                            modifier = Modifier.width(100.dp)
                        )
                    }
                }
            }
        }
        Row(1, modifier = Modifier.animateContentSize().padBottom(1)) {
            Button("Add step") { }
        }
    }
}

@Composable
fun MaterialRow(
    stepMaterial: StepMaterialJoin,
    viewModel: PathEditorModel,
) {
    Row(1) {
        when (stepMaterial.materialType) {
            MaterialType.Ingredient -> when (stepMaterial.materialUnit.unitType) {
                UnitType.Quantity -> IngredientQuantityRow(stepMaterial, viewModel)
                else -> IngredientRow(stepMaterial)
            }

            MaterialType.Tool -> ToolRow(stepMaterial)
        }
        Expando()
        MoreMenu {
            MoreMenuItem(
                label = "remove",
                color = Pond.localColors.dangerContent,
                icon = TablerIcons.Trash
            ) { viewModel.removeMaterial(stepMaterial) }
        }
    }
}

@Composable
fun IngredientQuantityRow(
    stepMaterial: StepMaterialJoin,
    viewModel: PathEditorModel,
) {
    Row(1) {
        IntegerMenu(stepMaterial.quantity.toInt()) { viewModel.setStepMaterialQuantity(stepMaterial.id, it.toFloat()) }
        Text(stepMaterial.label.pluralize(stepMaterial.quantity))
    }
}

@Composable
fun IngredientRow(stepMaterial: StepMaterialJoin) {
    Row(1) {
        Text(stepMaterial.label)
    }
}

@Composable
fun ToolRow(stepMaterial: StepMaterialJoin) {
    Text(stepMaterial.label)
}