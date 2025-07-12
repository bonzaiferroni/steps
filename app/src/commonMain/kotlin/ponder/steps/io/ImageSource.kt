package ponder.steps.io

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import pondui.APP_API_URL
import pondui.io.globalKtorClient
import java.io.File

class ImageSource(
    private val imageFolder: File,
    private val client: HttpClient = globalKtorClient
) {
    suspend fun provideImageBitmap(url: String): ImageBitmap {
        val fileName = url.substringAfterLast('/')
        val imageFile = File(imageFolder, fileName)

        val bytes = if (imageFile.exists()) {
            // println("reading image from file")
            imageFile.readBytes()
        } else {
            // println("reading image from url")
            val bytes: ByteArray = client.get("$APP_API_URL/$url").body()
            imageFile.writeBytes(bytes)
            bytes
        }

        return bytes.decodeToImageBitmap()
    }
}

@Composable
fun ProvideImageSource(content: @Composable () -> Unit) {
    val imageFolder = provideImageFolder()
    val imageSource = remember { ImageSource(imageFolder) }
    CompositionLocalProvider(LocalImageSource provides imageSource) {
        content()
    }
}

val LocalImageSource = staticCompositionLocalOf<ImageSource> {
    error("No image source provided")
}
