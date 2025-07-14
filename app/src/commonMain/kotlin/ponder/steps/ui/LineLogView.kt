package ponder.steps.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kabinet.utils.startOfDay
import kotlinx.datetime.Clock
import pondui.ui.theme.Pond
import pondui.utils.lighten
import kotlin.time.Duration.Companion.days

@Composable
fun LineLogView(

) {
    val viewModel = viewModel { LineLogModel() }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.setParameters(Clock.startOfDay(), Clock.startOfDay() + 1.days)
    }

    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // determine total height so scroll works
        val totalMins = (state.end - state.start).inWholeMinutes.toFloat()
        val totalHeight = with(density) { (totalMins / state.minPerPx).toDp() }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(totalHeight)
        )

        state.lines.forEach { line ->
            val minsFromStart = (line.startAt - state.start).inWholeMinutes.toFloat().coerceAtLeast(0f)
            val durationMins = (line.endAt - line.startAt).inWholeMinutes.toFloat().coerceAtLeast(1f)

            Box(
                modifier = Modifier
                    .offset(
                        x = LOG_LANE_WIDTH.dp * line.lane + LOG_LANE_GAP.dp * line.lane,
                        y = with(density) { (minsFromStart / state.minPerPx).toDp() }
                    )
                    .width(LOG_LANE_WIDTH.dp)
                    .height(with(density) { (durationMins / state.minPerPx).toDp() })
                    .clip(Pond.ruler.pill)
                    .background(Pond.colors.secondary.lighten(.2f))
                    .padding(1.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                StepImage(line.imgUrl, modifier = Modifier.clip(Pond.ruler.pill))
            }
        }
    }
}