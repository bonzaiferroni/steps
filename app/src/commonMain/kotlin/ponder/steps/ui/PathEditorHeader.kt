package ponder.steps.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ponder.steps.model.data.Step
import pondui.ui.behavior.AlignX
import pondui.ui.behavior.AlignY
import pondui.ui.behavior.Magic
import pondui.ui.behavior.drawLabel
import pondui.ui.behavior.padBottom
import pondui.ui.controls.Button
import pondui.ui.controls.Column
import pondui.ui.controls.EditText
import pondui.ui.controls.HeaderLabel
import pondui.ui.controls.Row
import pondui.ui.controls.Text
import pondui.ui.theme.Pond
import pondui.utils.addShadow

@Composable
fun PathEditorHeader(
    pathStep: Step,
    viewModel: PathEditorModel
) {
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
        Magic(pathStep.description != null, isVisibleInit = true) {
            EditText(
                text = pathStep.description ?: "",
                placeholder = "Description",
                modifier = Modifier.fillMaxWidth().drawLabel("description"),
                isContainerVisible = true,
            ) { viewModel.editStep(pathStep.copy(description = it)) }
        }
        Row(1, modifier = Modifier.animateContentSize().padBottom(1)) {
            Magic(pathStep.description == null) {
                Button("Add description", onClick = viewModel::addDescription)
            }
            Button("Add step") { }
        }
    }
}