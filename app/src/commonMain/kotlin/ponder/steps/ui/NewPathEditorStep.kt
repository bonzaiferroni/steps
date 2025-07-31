package ponder.steps.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowDown
import compose.icons.tablericons.ArrowUp
import pondui.ui.behavior.AlignX
import pondui.ui.behavior.drawLabel
import pondui.ui.controls.Button
import pondui.ui.controls.Column
import pondui.ui.controls.EditText
import pondui.ui.theme.Pond
import pondui.utils.electrify


@Composable
fun LazyItemScope.NewPathEditorStep(
    newStepLabel: String,
    isLastPosition: Boolean,
    viewModel: PathEditorModel,
) {
    val lineColor = Pond.colors.creation.electrify()
    Column(
        spacingUnits = 0,
        modifier = Modifier.fillMaxWidth()
            .animateItem()
            .padding(Pond.ruler.unitPadding),
    ) {
        PathMapItemPart(
            verticalAlignment = Alignment.Top,
            lineSlot = {
                StepLineCircle(lineColor) {
                    StepImage(
                        url = null,
                        modifier = Modifier.fillMaxWidth()
                            .drawLabel("new", alignX = AlignX.Center)
                            .clip(CircleShape)
                    )
                }
            }
        ) {
            EditText(
                text = newStepLabel,
                placeholder = "new step label",
                maxLines = 2,
                style = Pond.typo.h5,
                isContainerVisible = true,
                onAcceptEdit = viewModel::addNewStep,
                modifier = Modifier.weight(1f)
            )
            Column(
                spacingUnits = 0,
            ) {
                Button(
                    imageVector = TablerIcons.ArrowUp,
                    background = Pond.colors.action,
                    shape = Pond.ruler.roundTop,
                ) { viewModel.moveNewStep(-1) }
                Button(
                    imageVector = TablerIcons.ArrowDown,
                    background = Pond.colors.action,
                    shape = Pond.ruler.roundBottom
                ) { viewModel.moveNewStep(1) }
            }
        }
        if (!isLastPosition) {
            StepLineTail(lineColor)
        }
    }
}