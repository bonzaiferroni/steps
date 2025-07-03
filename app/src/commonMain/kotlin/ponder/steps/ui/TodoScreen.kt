package ponder.steps.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.viewmodel.compose.viewModel
import pondui.ui.behavior.selected
import pondui.ui.controls.Column
import pondui.ui.controls.Row
import pondui.ui.controls.TopBarSpacer
import pondui.ui.controls.actionable
import kotlin.math.absoluteValue

@Composable
fun TodoScreen() {
    val viewModel = viewModel { TodoModel() }
    val state by viewModel.state.collectAsState()
    val activity by viewModel.trekStarter.state.collectAsState()

    val pagerState = rememberPagerState(pageCount = {
        10
    })

    LaunchedEffect(state.pageIndex) {
        val index = state.pageIndex
        if (index != null) {
            pagerState.animateScrollToPage(index + 1, animationSpec = tween(500))
        } else {
            pagerState.animateScrollToPage(0, animationSpec = tween(500))
        }
    }

    Column(1, horizontalAlignment = Alignment.CenterHorizontally) {

        TopBarSpacer()

        Row(0, modifier = Modifier.animateContentSize()) {
            val trekPath = state.trekPath
            val pathSteps = trekPath?.breadcrumbs
            val thumbUrlSize = 60.dp
            val isRootSelected = state.pageIndex == null
            StepImage(
                url = null,
                modifier = Modifier.size(thumbUrlSize)
                    .selected(isRootSelected, radius = thumbUrlSize / 2)
                    .clip(CircleShape)
                    .actionable(isEnabled = !isRootSelected) { viewModel.navToPath(null, false) }
            )
            val pageIndex = state.pageIndex
            if (pageIndex != null && pathSteps != null) {
                pathSteps.forEachIndexed { i, step ->
                    val isSelected = i == state.pageIndex
                    if (i > pageIndex) return@forEachIndexed
                    StepImage(
                        url = step.thumbUrl,
                        modifier = Modifier.size(thumbUrlSize)
                            .selected(isSelected, radius = thumbUrlSize / 2)
                            .clip(CircleShape)
                            .actionable(isEnabled = !isSelected) {
                                viewModel.navToPath(trekPath.toSubPath(step.id), false)
                            }
                    )
                }
            }

        }
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.Top) { page ->
            Box(modifier = Modifier.graphicsLayer {
                val offset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                val offsetAbs = offset.absoluteValue
                val direction = offset / (offsetAbs.takeIf { it > 0 } ?: 1f)

                val animation = lerp(
                    start = 0.5f,
                    stop = 1f,
                    fraction = 1f - offsetAbs.coerceIn(0f, 1f)
                )
                alpha = animation
                scaleY = animation
                scaleX = animation
                val rotation = 90 * (1 - animation) * -direction
                // translationX = -rotation
                rotationY = rotation
            }) {
                if (page == 0) {
                    TodoRootView(viewModel::navToPath)
                } else {
                    val pageIndex = page - 1
                    if (state.pageStack.size > pageIndex) {
                        val trekPath = state.pageStack[pageIndex]
                        TodoPathView(
                            trekPath = trekPath,
                            isActive = pageIndex == state.pageIndex,
                            navToPath = viewModel::navToPath
                        )
                    }
                }
            }
        }
    }
}