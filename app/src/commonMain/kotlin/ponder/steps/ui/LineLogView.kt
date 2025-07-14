package ponder.steps.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import kabinet.utils.startOfDay
import kabinet.utils.toLocalDateTime
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import pondui.ui.controls.Divider
import pondui.ui.controls.LazyColumn
import pondui.ui.controls.Text
import pondui.ui.controls.actionable
import pondui.ui.theme.Pond
import pondui.utils.lighten
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

@Composable
fun LineLogView() {
    val viewModel = viewModel { LineLogModel() }
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.setParameters(Clock.startOfDay(), Clock.startOfDay() + 1.days)
    }

    // LazyColumn’s scroll state
    val listState = rememberLazyListState()
    val tz = TimeZone.currentSystemDefault()

    // one hour row’s height in dp
    val hourHeightDp = (60f / state.minPerPx).dp

    Box(modifier = Modifier.fillMaxSize()) {
        // 1) the scrolling background of hours
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            val totalHours = ((state.end - state.start).inWholeHours + 1).toInt()
            items(totalHours) { idx ->
                val hourInstant = state.start + idx.hours
                val hourLabel = hourInstant.toLocalDateTime(tz).hour
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(hourHeightDp)
                ) {
                    Text(
                        text = "$hourLabel:00",
                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).zIndex(1f)
                    )
                    Divider(
                        color = Pond.colors.contentSky.copy(.2f),
                        height = 3.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
        }

        // 2) overlay bars
        RenderLogLines(
            hourHeightDp = hourHeightDp,
            state = state,
            listState = listState
        )
    }
}

@Composable
fun RenderLogLines(
    hourHeightDp: Dp,
    state: LineLogState,
    listState: LazyListState,
) {
    val density = LocalDensity.current
    val hourHeightPx = with(density) { hourHeightDp.toPx() }

    Box(modifier = Modifier.fillMaxSize()) {

        val tick by produceState(initialValue = 0) {
            while (true) {
                delay(10_000)
                value++
            }
        }

        // compute current scroll in px
        val scrollPx = remember {
            derivedStateOf {
                listState.firstVisibleItemIndex * hourHeightPx +
                        listState.firstVisibleItemScrollOffset
            }
        }.value

        val now = Clock.System.now()

        // currentLine
        val minsNowFromStart = (now - state.start).inWholeMinutes.toFloat()
        val lineHeight = with(density) { (minsNowFromStart / state.minPerPx).dp }
        Divider(
            color = Pond.colors.selected.lighten(.2f),
            height = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = lineHeight)
        )

        state.lines.forEach { line ->
            // position and size in px
            val minsFromStart = (line.startAt - state.start).inWholeMinutes.toFloat()
            val durationMins = ((line.endAt ?: now) - line.startAt).inWholeMinutes.toFloat().coerceAtLeast(1f)
            val yPx = minsFromStart / state.minPerPx
            val hPx = durationMins / state.minPerPx

            Box(
                modifier = Modifier
                    .offset(
                        x = LOG_LANE_WIDTH.dp * line.lane + LOG_LANE_GAP.dp * line.lane,
                        y = with(density) { (yPx - scrollPx).toDp() }
                    )
                    .width(LOG_LANE_WIDTH.dp)
                    .height(with(density) { hPx.toDp() })
                    .clip(Pond.ruler.pill)
                    .background(Pond.colors.secondary.lighten(.2f))
                    .actionable { println("Ey") }
                    .padding(2.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                StepImage(line.imgUrl, modifier = Modifier.clip(Pond.ruler.pill))
            }
        }
    }
}
