package com.example.actitracker.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private object ScrollbarDefaults {
    val width = 8.dp
    val minThumbHeight = 48.dp
    val cornerRadius = 4.dp
    val color = Color.Gray.copy(alpha = 0.5f)
    val padding = 4.dp
}

fun Modifier.verticalScrollbar(
    state: LazyListState,
    width: Dp = ScrollbarDefaults.width,
    minThumbHeight: Dp = ScrollbarDefaults.minThumbHeight,
    color: Color = ScrollbarDefaults.color
): Modifier = composed {

    val needScrollbar by remember {
        derivedStateOf {
            val totalItems = state.layoutInfo.totalItemsCount
            val visibleItems = state.layoutInfo.visibleItemsInfo.size
            totalItems > visibleItems
        }
    }

    val scrollFraction by remember {
        derivedStateOf {
            val totalItems = state.layoutInfo.totalItemsCount
            if (totalItems == 0) return@derivedStateOf 0f
            val visibleItems = state.layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) return@derivedStateOf 0f

            val firstVisibleIndex = state.firstVisibleItemIndex
            val firstItemOffset = state.firstVisibleItemScrollOffset
            val avgItemSize = visibleItems.sumOf { it.size } / visibleItems.size.toFloat()

            val scrolled = firstVisibleIndex * avgItemSize + firstItemOffset
            val maxScroll = totalItems * avgItemSize - state.layoutInfo.viewportSize.height
            if (maxScroll <= 0f) 0f else (scrolled / maxScroll).coerceIn(0f, 1f)
        }
    }

    val thumbFraction by remember {
        derivedStateOf {
            val totalItems = state.layoutInfo.totalItemsCount
            val visibleItems = state.layoutInfo.visibleItemsInfo.size
            if (totalItems == 0) 1f
            else (visibleItems.toFloat() / totalItems).coerceIn(0.05f, 1f)
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (needScrollbar) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "scrollbar_alpha"
    )

    this.drawWithContent {
        drawContent()

        if (alpha > 0f) {
            val viewportHeight = size.height
            val widthPx = width.toPx()
            val minThumbHeightPx = minThumbHeight.toPx()
            val paddingPx = ScrollbarDefaults.padding.toPx()
            val cornerRadiusPx = ScrollbarDefaults.cornerRadius.toPx()

            val thumbHeight = (thumbFraction * viewportHeight)
                .coerceAtLeast(minThumbHeightPx)

            val maxThumbOffset = viewportHeight - thumbHeight
            val thumbOffset = scrollFraction * maxThumbOffset

            drawRoundRect(
                color = color.copy(alpha = color.alpha * alpha),
                topLeft = Offset(
                    x = size.width - widthPx - paddingPx,
                    y = thumbOffset
                ),
                size = Size(widthPx, thumbHeight),
                cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
            )
        }
    }
}