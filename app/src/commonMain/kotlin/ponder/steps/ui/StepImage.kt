package ponder.steps.ui

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil3.compose.AsyncImage
import ponder.steps.model.data.Step
import pondui.ui.behavior.magic
import steps.app.generated.resources.Res

@Composable
fun StepImage(
    url: String?,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = url?.let { "http://192.168.1.100:8080/${it}" } ?: Res.getUri("drawable/horse.png"),
        contentDescription = null,
        modifier = modifier.aspectRatio(1f)
    )
}