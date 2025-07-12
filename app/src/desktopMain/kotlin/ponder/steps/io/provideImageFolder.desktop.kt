package ponder.steps.io

import androidx.compose.runtime.Composable
import java.io.File

@Composable
actual fun provideImageFolder(): File {
    return File("image_cache").apply {
        if (!exists()) mkdirs()
    }
}