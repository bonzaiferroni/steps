package ponder.steps.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import org.jetbrains.compose.resources.imageResource
import ponder.steps.io.LocalImageSource
import steps.app.generated.resources.Res
import steps.app.generated.resources.horse

@Composable
fun StepImage(
    url: String?,
    modifier: Modifier = Modifier
) {
    val imageSource = LocalImageSource.current
    val placeholder = defaultImage ?: imageResource(Res.drawable.horse).also { defaultImage = it }
    val bitmapState = remember { mutableStateOf(placeholder) }

    LaunchedEffect(url) {
        if (url != null) {
            bitmapState.value = imageSource.provideImageBitmap(url)
        }
    }

    Image(bitmap = bitmapState.value, contentDescription = null, modifier = modifier.aspectRatio(1f))
}

private var defaultImage: ImageBitmap? = null

// Res.getUri("drawable/horse.png")
// modifier = modifier.aspectRatio(1f)
// contentDescription = null,