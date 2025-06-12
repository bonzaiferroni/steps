package ponder.steps.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.painterResource
import ponder.steps.Greeting
import ponder.steps.HelloRoute
import pondui.PlayWave
import pondui.ui.controls.Button
import pondui.ui.controls.MenuWheel
import pondui.ui.controls.RouteButton
import pondui.ui.controls.Text
import pondui.ui.controls.Scaffold
import pondui.ui.theme.Pond
import steps.app.generated.resources.Res
import steps.app.generated.resources.compose_multiplatform
import pondui.WavePlayer

@Composable
fun StartScreen() {
    Scaffold {
        var showContent by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Pond.ruler.columnSpaced
        ) {
            RouteButton("Go to Hello") { HelloRoute }
            Button(onClick = { showContent = !showContent }) {
                Text("Show platform")
            }
            AnimatedVisibility(showContent) {
                val greeting = remember { Greeting().greet() }
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painterResource(Res.drawable.compose_multiplatform), null)
                    Text("Compose: $greeting")
                }
            }
            val options = (0..10).toImmutableList()
            var selectedOption by remember { mutableStateOf(options.first()) }
            MenuWheel(
                selectedItem = selectedOption,
                options = options,
                label = "things",
            ) {
                selectedOption = it
            }
            Text("You picked $selectedOption")

            PlayWave("Watch_a_movie-UnviiMf.wav")

//            LaunchedEffect(Unit) {
//                // val resource = "8-Bit-Noise-1.wav"
//                val resource =
//                // val url = Path(resource).toUri().toURL()
//                val audio = Audio(resource) // loads the audio file
//                audio.load()
//                audio.play()
//                // immediately upon execution
//            }
        }
    }
}