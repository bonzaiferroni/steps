package ponder.steps.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ponder.steps.model.data.IntentId
import pondui.ui.controls.Button
import pondui.ui.controls.Column
import pondui.ui.controls.Row
import pondui.ui.controls.Text
import pondui.ui.controls.TitleCloud

@Composable
fun EditIntentCloud(
    intentId: IntentId?,
    isVisible: Boolean,
    dismiss: () -> Unit
) {
    val viewModel = viewModel { EditIntentCloudModel(dismiss) }
    val state by viewModel.stateFlow.collectAsState()

    LaunchedEffect(intentId) {
        if (intentId != null) viewModel.setParameters(intentId)
    }

    TitleCloud(
        title = "Adjust intent",
        isVisible = isVisible,
        onDismiss = dismiss
    ) {
        Column(
            gap = 1,
            modifier = Modifier.height(400.dp),
        ) {
            Row(
                gap = 1,
                modifier = Modifier.height(44.dp)
                    .fillMaxWidth()
            ) {
                StepImage(
                    url = state.imgUrl,
                    modifier = Modifier.height(40.dp)
                        .clip(CircleShape)
                )
                state.label?.let {
                    Text(
                        text = it,
                        maxLines = 2,
                        modifier = Modifier.weight(1f)
                    )
                }
                Button("Adjust", onClick = viewModel::confirm)
            }
            EditIntentView(viewModel.editIntent)
        }
    }
}