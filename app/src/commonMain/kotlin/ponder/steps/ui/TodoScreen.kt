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
                    .actionable(isEnabled = !isRootSelected) { viewModel.loadTrek(null, false) }
            )
            state.breadcrumbUrls.forEachIndexed { i, trekImageUrl ->
                val isCrumbSelected = state.breadcrumbUrls.size - 1 == i
                StepImage(
                    url = trekImageUrl.url,
                    modifier = Modifier.size(thumbUrlSize)
                        .selected(isCrumbSelected, radius = thumbUrlSize / 2)
                        .clip(CircleShape)
                        .actionable(isEnabled = !isCrumbSelected) { viewModel.loadTrek(trekImageUrl.trekId, false) }
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
                    TodoRootView(viewModel::loadTrek)
                } else {
                    val stackIndex = page - 1
                    if (state.stack.size > stackIndex) {
                        val trekId = state.stack[stackIndex]
                        TrekFocusView(trekId, viewModel::loadTrek)
                    }
                }
            }
        }
    }
}