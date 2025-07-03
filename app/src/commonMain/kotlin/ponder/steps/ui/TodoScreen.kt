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

    LaunchedEffect(state.stackIndex) {
        val index = state.stackIndex
        if (index != null) {
            pagerState.animateScrollToPage(index + 1, animationSpec = tween(500))
        } else {
            pagerState.animateScrollToPage(0, animationSpec = tween(500))
        }
    }

    Column(1, horizontalAlignment = Alignment.CenterHorizontally) {

        TopBarSpacer()

        Row(0, modifier = Modifier.animateContentSize()) {
            val thumbUrlSize = 60.dp
            val isRootSelected = state.breadcrumbUrls.isEmpty()
            StepImage(
                url = null,
                modifier = Modifier.size(thumbUrlSize)
                    .selected(isRootSelected, radius = thumbUrlSize / 2)
                    .clip(CircleShape)
                    .actionable(isEnabled = !isRootSelected) { viewModel.navToPath(null, false) }
            )
            state.breadcrumbUrls.forEachIndexed { i, trekImageUrl ->
                val isCrumbSelected = state.breadcrumbUrls.size - 1 == i
                StepImage(
                    url = trekImageUrl.url,
                    modifier = Modifier.size(thumbUrlSize)
                        .selected(isCrumbSelected, radius = thumbUrlSize / 2)
                        .clip(CircleShape)
                        .actionable(isEnabled = !isCrumbSelected) {
                            val trekId = state.trekId ?: return@actionable
                            viewModel.navToPath(TrekPath(trekId, trekImageUrl.stepId), false)
                        }
                )
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
                    val trekId = state.trekId
                    if (trekId != null && state.stack.size > pageIndex) {
                        val pathId = state.stack[pageIndex]
                        TodoPathView(
                            trekId = trekId,
                            pathId = pathId,
                            isActive = pageIndex == state.stackIndex,
                            breadcrumbs = state.stack,
                            navToPath = viewModel::navToPath
                        )
                    }
                }
            }
        }
    }
}