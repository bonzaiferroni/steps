package ponder.steps.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import compose.icons.TablerIcons
import compose.icons.tablericons.Trash
import kabinet.utils.pluralize
import kotlinx.collections.immutable.toImmutableList
import ponder.steps.model.data.MaterialType
import ponder.steps.model.data.MaterialUnit
import ponder.steps.model.data.StepMaterialJoin
import ponder.steps.model.data.UnitType
import pondui.ui.behavior.drawSection
import pondui.ui.controls.Button
import pondui.ui.controls.Column
import pondui.ui.controls.DropMenu
import pondui.ui.controls.Expando
import pondui.ui.controls.FloatFieldMenu
import pondui.ui.controls.MoreMenu
import pondui.ui.controls.MoreMenuItem
import pondui.ui.controls.Row
import pondui.ui.controls.Section
import pondui.ui.controls.Tab
import pondui.ui.controls.Tabs
import pondui.ui.controls.Text
import pondui.ui.controls.TextFieldMenu
import pondui.ui.theme.Pond

@Composable
fun PathEditorMaterials(
    viewModel: PathEditorModel
) {
    val state by viewModel.stateFlow.collectAsState()
    val pathContextState by viewModel.pathContext.stateFlow.collectAsState()

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

    val matchedMaterial = state.matchedMaterial
    val labelColor = when (matchedMaterial) {
        null -> Pond.colors.creation
        else -> Pond.colors.action
    }
    val label = if (matchedMaterial == null && state.newMaterialLabel.isNotEmpty()) "create" else "add"

    Section() {
        Tabs(
            selectedTab = state.newMaterialTab,
            onChangeTab = viewModel::setNewMaterialTab
        ) {
            Tab(ADD_TOOL_LABEL) {
                Row(1, modifier = Modifier.fillMaxWidth()) {
                    AddMaterialField(viewModel, modifier = Modifier.weight(1f))
                    Button(label, background = labelColor, onClick = viewModel::addMaterial)
                }
            }
            Tab(ADD_INGREDIENT_LABEL) {
                Column(
                    spacingUnits = 1,
                ) {
                    Row(
                        spacingUnits = 1,
                    ) {
                        FloatFieldMenu(
                            value = state.newMaterialQuantity,
                            onValueSelected = viewModel::setNewMaterialQuantity,
                        )
                        AddMaterialField(
                            viewModel = viewModel,
                            modifier = Modifier.weight(1f)
                        )
                        Button(label, background = labelColor, onClick = viewModel::addMaterial)
                    }
                    Row(1) {
                        DropMenu(
                            selected = state.newUnitType,
                            onSelect = viewModel::setNewUnitType,
                        )
                        if (state.newUnitType != UnitType.Quantity) {
                            MaterialUnitMenu(
                                selectedUnit = state.newMaterialUnit,
                            ) { viewModel.setNewMaterialUnit(it) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddMaterialField(
    viewModel: PathEditorModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.stateFlow.collectAsState()

    TextFieldMenu(
        text = state.newMaterialLabel,
        provideOptions = { println("state suggestions: ${state.materialSuggestions.size}"); state.materialSuggestions },
        onTextChanged = viewModel::setNewMaterialLabel,
        onEnterPressed = viewModel::addMaterial,
        onChooseSuggestion = viewModel::addMaterial,
        modifier = modifier
    ) { material ->
        Text(material.label)
    }
}

@Composable
fun MaterialUnitMenu(
    selectedUnit: MaterialUnit,
    modifier: Modifier = Modifier,
    onSelect: (MaterialUnit) -> Unit
) {
    val materialUnitOptions = remember(selectedUnit.unitType) {
        MaterialUnit.entries.filter { it.unitType == selectedUnit.unitType }.map { it.label }
            .toImmutableList()
    }
    DropMenu(
        selected = selectedUnit.label,
        options = materialUnitOptions,
        onSelect = { value ->
            MaterialUnit.fromLabel(value)?.let { onSelect(it) }
        },
    )
}

@Composable
fun MaterialRow(
    stepMaterial: StepMaterialJoin,
    viewModel: PathEditorModel,
) {
    Row(1) {
        when (stepMaterial.materialType) {
            MaterialType.Ingredient -> IngredientQuantityRow(stepMaterial, viewModel)
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
        FloatFieldMenu(
            value = stepMaterial.quantity,
        ) { viewModel.setStepMaterialQuantity(stepMaterial.id, it) }

        val label = if (stepMaterial.materialUnit != MaterialUnit.Quantity) {
            "${stepMaterial.materialUnit.label} of ${stepMaterial.label}"
        } else {
            stepMaterial.label.pluralize(stepMaterial.quantity)
        }
        Text(label)
    }
}

@Composable
fun ToolRow(stepMaterial: StepMaterialJoin) {
    Text(stepMaterial.label)
}