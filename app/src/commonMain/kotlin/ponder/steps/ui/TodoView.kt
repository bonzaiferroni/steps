package ponder.steps.ui

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.absoluteValue

@Composable
fun TodoView() {
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
                val trekId = state.stack[page - 1]
                TrekProfileView(trekId, viewModel::loadTrek)
            }
        }
    }
}