package ponder.steps.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import pondui.ui.core.StateScope

//class ItemSwiper<T>: StateScope<ItemSwiperState<T>>(ItemSwiperState()) {
//    fun setAnimation(value: Float) {
//        setState { it.copy(animation = value) }
//    }
//}
//
//data class ItemSwiperState<T>(
//    val item: T? = null,
//    val swipeKey: String? = null,
//    val animation: Float = 0f
//)
//
//@Composable
//fun <T> rememberSwipeNav(
//    contextKey: String,
//    canSwipeRight: () -> Boolean,
//    onSwipeLeft: (T) -> Unit,
//    onSwipeRight: (T) -> Unit,
//): ItemSwiper<T> {
//    var offsetX by remember { mutableStateOf(0f) }
//    var draggedItem by remember (contextKey) { mutableStateOf<T?>(null) }
//    val dragAnimator = remember { Animatable(0f) }
//    val dragAnimation by dragAnimator.asState()
//    var animatedItem by remember { mutableStateOf<T?>(null) }
//    val canSwipeRight = canSwipeRight()
//    val swiper = remember { ItemSwiper<T>() }
//
//    LaunchedEffect(offsetX, draggedItem) {
//        if (offsetX != 0f && draggedItem == null) {
//            dragAnimator.animateTo(0f)
//            animatedItem = null
//            offsetX = 0f
//        } else {
//            animatedItem = draggedItem
//            dragAnimator.snapTo(offsetX)
//        }
//    }
//    LaunchedEffect(offsetX) {
//        val item = draggedItem ?: return@LaunchedEffect
//        if (offsetX < -100) {
//            onSwipeLeft(item)
//        }
//        if (offsetX > 100) {
//            onSwipeRight(item)
//        }
//    }
//
//
//}

//@Composable
//fun Modifier.itemSwipe(key: String, itemSwiper: ItemSwiper): Modifier {
//
//    val state by itemSwiper.state.collectAsState()
//
//    return this.graphicsLayer {
//        if (key == state.swipeKey)
//            translationX = dragAnimation
//        else
//            alpha = (100 + dragAnimation) / 100
//    }
//        .ifTrue(canDragLeft) {
//            pointerInput(Unit) {
//                detectDragGestures(
//                    onDragStart = { offset ->
//                        draggedItem = trekStep
//                    },
//                    onDrag = { change, dragAmount ->
//                        offsetX += dragAmount.x
//                    },
//                    onDragEnd = {
//                        draggedItem = null
//                    }
//                )
//            }
//        }
//}

