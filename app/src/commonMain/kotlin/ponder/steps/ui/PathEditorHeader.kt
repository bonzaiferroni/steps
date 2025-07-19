package ponder.steps.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ponder.steps.model.data.Step
import pondui.ui.behavior.Magic
import pondui.ui.controls.Button
import pondui.ui.controls.Column
import pondui.ui.controls.EditText
import pondui.ui.controls.LabeledPart
import pondui.ui.controls.PartLabel
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
            LabeledPart(
                label = "Image",
                modifier = Modifier
                    .fillMaxWidth(.33f)
                    .widthIn(max = 200.dp)
            ) {
                StepImage(
                    url = pathStep.imgUrl,
                    modifier = Modifier.fillMaxSize()
                        .aspectRatio(1f)
                        .clip(Pond.ruler.unitCorners)
                )
            }
            LabeledPart(
                label = "Label",
                modifier = Modifier
            ) {
                EditText(
                    text = pathStep.label,
                    placeholder = "Step label",
                    style = Pond.typo.h1.addShadow(),
                    isContainerVisible = true,
                ) { viewModel.editStep(pathStep.copy(label = it)) }
            }
        }
        Magic(pathStep.description == null) {
            Row(1) {
                Button("Add description", onClick = viewModel::addDescription)
            }
        }
        Magic(pathStep.description != null) {
            LabeledPart("Description") {
                EditText(
                    text = pathStep.description ?: "",
                    placeholder = "Description",
                    modifier = Modifier.fillMaxWidth(),
                    isContainerVisible = true,
                ) { viewModel.editStep(pathStep.copy(description = it)) }
            }
        }
        PartLabel("Steps") {
            Text("ey")
        }
    }
}