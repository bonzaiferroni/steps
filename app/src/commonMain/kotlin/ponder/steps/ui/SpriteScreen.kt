package ponder.steps.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ponder.steps.appDb
import ponder.steps.db.Sprite
import pondui.ui.behavior.magic
import pondui.ui.controls.Button
import pondui.ui.controls.Column
import pondui.ui.controls.Scaffold
import pondui.ui.controls.Text
import pondui.ui.controls.TextField
import pondui.ui.nav.AppRoute

@Composable
fun SpriteScreen() {

    val dao = remember { appDb!!.getSpriteDao() }
    val spriteFlow = remember { dao.getAllAsFlow() }

    val scope = rememberCoroutineScope()
    val sprites by remember(spriteFlow) {
        spriteFlow.stateIn(
            scope = scope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )
    }.collectAsState()

    var name by remember { mutableStateOf("") }

    Scaffold {
        TextField(name, onTextChanged = { name = it })
        Button("Add") {
            scope.launch {
                dao.insert(Sprite(name = name, speed = (0..10).random()))
            }
        }

        Column(1) {
            for (sprite in sprites) {
                Text("${sprite.name} (${sprite.speed})", modifier = Modifier.magic(offsetX = 20.dp))
            }
        }
    }
}

@Serializable
object SpriteRoute: AppRoute("Sprites")