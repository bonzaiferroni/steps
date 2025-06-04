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
    step: Step,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = step.imgUrl?.let { "http://localhost:8080/${it}" } ?: Res.getUri("drawable/horse.png"),
        contentDescription = null,
        modifier = modifier.aspectRatio(1f)
    )
}