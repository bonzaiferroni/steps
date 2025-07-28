package ponder.steps.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.Plus
import kabinet.utils.amPmLabel
import kabinet.utils.hour12
import kabinet.utils.startOfDay
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import pondui.ui.behavior.MagicItem
import pondui.ui.behavior.selected
import pondui.ui.controls.BottomBarSpacer
import pondui.ui.controls.Button
import pondui.ui.controls.Checkbox
import pondui.ui.controls.Column
import pondui.ui.controls.Divider
import pondui.ui.controls.Label
import pondui.ui.controls.Row
import pondui.ui.controls.Text
import pondui.ui.controls.TopBarSpacer
import pondui.ui.controls.actionable
import pondui.ui.nav.ContextMenu
import pondui.ui.theme.Pond
import pondui.ui.theme.ProvideBookColors
import pondui.utils.lighten
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Composable
fun LineLogView() {
    val viewModel = viewModel { LineLogModel() }
    val state by viewModel.stateFlow.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.setParameters(Clock.startOfDay(), Clock.startOfDay() + 1.days)
    }

    AddStepCloud(
        title = "Add Step",
        isVisible = state.isAddingStep,
        createIntent = true,
        pathId = null,
        dismiss = viewModel::toggleIsAddingStep
    )

    // LazyColumn’s scroll state
    val scrollState = rememberScrollState()

    Box {
        Column(
            spacingUnits = 1,
            modifier = Modifier.fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            TopBarSpacer()
            Box(
            ) {
                RenderMarkers(state)

                // 2) overlay bars
                RenderLogLines(
                    state = state,
                    viewModel = viewModel,
                ) { scrollState.scrollTo(scrollState.value + 1) }
            }
            BottomBarSpacer()
        }
        Column(1, modifier = Modifier.align(Alignment.BottomEnd)) {
            Button(TablerIcons.Plus, onClick = viewModel::toggleIsAddingStep)
            BottomBarSpacer()
        }
    }
}

@Composable
fun RenderMarkers(
    state: LineLogState,
) {
    val hourHeightDp = state.dpPerHour.dp
    val tz = TimeZone.currentSystemDefault()
    // 1) draw the hour‐rows behind
    Column(Modifier.fillMaxWidth()) {

        val totalHours = ((state.end - state.start).inWholeHours + 1).toInt()
        for (h in 0 until totalHours) {
            val hourInstant = state.start + h.hours
            val hour = hourInstant.toLocalDateTime(tz)
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(hourHeightDp)
            ) {
                Text(
                    text = "${hour.hour12}:00 ${hour.amPmLabel}",
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
}

@Composable
fun RenderLogLines(
    state: LineLogState,
    viewModel: LineLogModel,
    onTick: suspend () -> Unit
) {
    val density = LocalDensity.current
    val tick by produceState(initialValue = 0) {
        while (true) {
            delay((60_000 * with(density) { state.minPerDp.dp.toPx() }).toLong())
            value++
        }
    }

    LaunchedEffect(tick) {
        onTick()
    }

    val now = Clock.System.now()

    // currentLine
    val minsNowFromStart = (now - state.start).inWholeMinutes.toFloat()
    val lineHeight = with(density) { (minsNowFromStart / state.minPerDp).dp }
    Divider(
        color = Pond.colors.creation.lighten(.2f),
        height = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = lineHeight)
    )

    val minMinutes = (state.minPerDp * LOG_LANE_WIDTH).toInt()

    state.lines.forEach { line ->
        // position and size in px
        val trekStart = line.startAt ?: now
        val trekEnd = line.endAt ?: now
        val trekDuration = (trekEnd - trekStart)
        val lineMinutes = maxOf(minMinutes.toLong(), trekDuration.inWholeMinutes)
        val lineStart = trekEnd - lineMinutes.minutes
        val minsFromStart = (lineStart - state.start).inWholeMinutes
        val yOffsetDp = (minsFromStart / state.minPerDp).dp
        val xOffsetDp = LOG_LANE_WIDTH.dp * line.lane + LOG_LANE_GAP.dp * line.lane
        val heightDp = (lineMinutes / state.minPerDp).dp

        Box(
            modifier = Modifier
                .width(LOG_LANE_WIDTH.dp)
                .height(heightDp)
                .graphicsLayer {
                    translationX = with(density) { xOffsetDp.toPx() }
                    translationY = with(density) { yOffsetDp.toPx() }
                }
                .selected(state.openMenuId == line.trekPointId)
                .clip(Pond.ruler.pill)
                .background(Pond.colors.action.lighten(.2f))
                .focusProperties { canFocus = false }
                .actionable { viewModel.setOpenMenuId(line.trekPointId) }
                .padding(2.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            StepImage(line.imgUrl, modifier = Modifier.clip(Pond.ruler.pill))
            ContextMenu(
                isVisible = state.openMenuId == line.trekPointId,
                onDismiss = { viewModel.setOpenMenuId(null) }
            ) {
                ProvideBookColors {
                    MagicItem(
                        item = state.nextStep,
                        offsetX = 20.dp,
                        isVisibleInit = true,
                        modifier = Modifier.padding(
                            horizontal = Pond.ruler.unitSpacing,
                            vertical = Pond.ruler.doubleSpacing
                        )
                    ) { nextStep ->
                        val nextStep = nextStep ?: return@MagicItem
                        val step = nextStep.step;
                        val question = nextStep.question
                        if (question != null) {
                            QuestionRow(step.label, question) { answerText ->
                                viewModel.answerQuestion(nextStep, answerText)
                            }
                        } else {
                            Row(
                                spacingUnits = 1,
                                modifier = Modifier
                                    .height(70.dp)
                                    .fillMaxWidth()
                                    .clip(Pond.ruler.pill)
                                    .background(Pond.colors.surfaceBook)
                            ) {
                                StepImage(
                                    url = step.thumbUrl,
                                    modifier = Modifier.fillMaxHeight()
                                        .clip(CircleShape),
                                )
                                step.position?.let { Label("${it + 1}.", Pond.typo.bodyLarge) }
                                Column(0, modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = step.label,
                                        style = Pond.typo.bodyLarge,
                                        maxLines = 2,
                                    )
                                }
                                Checkbox(false, modifier = Modifier.padding(end = 20.dp)) {
                                    viewModel.setComplete(nextStep)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

