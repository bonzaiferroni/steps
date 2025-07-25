package ponder.steps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import ponder.steps.db.getDatabaseBuilder
import ponder.steps.db.getRoomDatabase
import pondui.ui.controls.AppWindow
import pondui.ui.controls.LocalAppWindow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            _db = getRoomDatabase(getDatabaseBuilder(LocalContext.current))
            val baseDensity = LocalDensity.current.density
            CompositionLocalProvider(
                LocalDensity provides Density(baseDensity * 1.2f, LocalDensity.current.fontScale)
            ) {
                val cfg = LocalConfiguration.current
                val density = LocalDensity.current
                val appWindow = remember(cfg) {
                    AppWindow(cfg.screenWidthDp, cfg.screenHeightDp, density)
                }
                CompositionLocalProvider(LocalAppWindow provides appWindow) {
                    App({ }, { })
                }
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App({ }, { })
}