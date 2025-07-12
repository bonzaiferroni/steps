package ponder.steps.io

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import java.io.File

@Composable
actual fun provideImageFolder(): File {
    val context = LocalContext.current
    return File(context.filesDir, "image_cache").apply {
        if (!exists()) mkdirs()
    }
}
