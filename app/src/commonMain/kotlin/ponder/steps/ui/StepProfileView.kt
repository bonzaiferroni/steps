package ponder.steps.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.collections.immutable.persistentListOf
import ponder.steps.model.data.Step
import pondui.ui.behavior.magic
import pondui.ui.controls.*
import pondui.ui.theme.Pond
import steps.app.generated.resources.Res

@Composable
fun StepProfileView(step: Step) {
    Column(1) {
        Card(
            innerPadding = 0.dp,
            modifier = Modifier.aspectRatio(3f)
        ) {
            Row {
                AsyncImage(
                    model = step.imgUrl?.let { "http://localhost:8080/${it}" } ?: Res.getUri("drawable/horse.png"),
                    contentDescription = null,
                    modifier = Modifier.weight(1f)
                        .aspectRatio(1f)
                        .magic(offsetX = -20, durationMillis = 500)
                )
                Column(
                    spacingUnits = 1,
                    modifier = Modifier.weight(2f)
                        .padding(Pond.ruler.unitPadding)
                        .magic(offsetX = 20, durationMillis = 500)
                ) {
                    H1(step.label)
                }
            }
        }
        Tabs {
            tab("Steps") {
                Text("Steps go here")
            }
            tab("Activity") {
                Text("No activity")
            }
        }
    }
}