package ponder.steps.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import pondui.ui.behavior.magic
import pondui.ui.controls.Button
import pondui.ui.controls.Column
import pondui.ui.controls.FlowRow
import pondui.ui.controls.H1
import pondui.ui.controls.H2
import pondui.ui.theme.Pond

@Composable
fun FocusView() {
    val viewModel = viewModel { FocusModel() }
    val state by viewModel.state.collectAsState()

    Column(1, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        val focus = state.focus
        Box() {
            H2("All done!", modifier = Modifier.magic(focus == null, offsetX = (-100).dp))
            val intentLabel = focus?.intentLabel
            val cachedValue by produceState(initialValue = intentLabel ?: "") {
                if (intentLabel != null) value = intentLabel
            }

            H2(cachedValue, modifier = Modifier.magic(focus != null, offsetX = 100.dp))
        }

        if (focus == null) return@Column

        AsyncImage(
            model = "http://localhost:8080/${focus.imgUrl ?: "img/horse.png"}",
            contentDescription = null,
            modifier = Modifier.clip(Pond.ruler.pill)
                .height(400.dp)
                .aspectRatio(1f)
                .magic(rotationZ = 180, scale = true, durationMillis = 500)
        )

        H1(focus.stepLabel)
        Box {
            Button(
                text = "Start",
                onClick = viewModel::startTrek,
                modifier = Modifier.magic(focus.startedAt == null, offsetY = 30.dp)
            )
            val completeButtonText = if (focus.stepIndex + 1 == focus.stepCount) "Complete Trek"
            else "Complete Step"
            Button(
                text = completeButtonText,
                onClick = viewModel::completeStep,
                modifier = Modifier.magic(focus.startedAt != null, offsetY = (-30).dp)
            )
        }
        if (focus.startedAt == null) return@Column
        FlowRow(1, 2) {

            Button(
                "Step into", background = Pond.colors.secondary, onClick = viewModel::stepIntoPath,
                modifier = Modifier.weight(1f).magic(focus.stepPathSize > 0, rotationX = 90)
            )
            Button(
                text = "Pause", background = Pond.colors.secondary,
                modifier = Modifier.weight(1f), onClick = viewModel::pauseTrek
            )
        }
    }
}