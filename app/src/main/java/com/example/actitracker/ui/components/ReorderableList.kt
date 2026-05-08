package com.example.actitracker.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun <T> ReorderableLazyColumn(
    items: List<T>,
    itemKey: (T) -> Any,
    onReorder: (List<T>) -> Unit,
    state: LazyListState,
    modifier: Modifier = Modifier,
    itemContent: @Composable LazyItemScope.(item: T, isDragging: Boolean) -> Unit
) {
    var draggingItemKey by remember { mutableStateOf<Any?>(null) }
    var dragY by remember { mutableFloatStateOf(0f) }
    var touchOffsetWithinItem by remember { mutableFloatStateOf(0f) }
    
    val localItems = remember { mutableStateListOf<T>() }
    
    // Synchronization with external list
    LaunchedEffect(items) {
        if (draggingItemKey == null) {
            localItems.clear()
            localItems.addAll(items)
        }
    }

    // Main swap logic
    val checkForSwaps = {
        val draggingKey = draggingItemKey
        if (draggingKey != null) {
            val layoutInfo = state.layoutInfo
            val draggingInfo = layoutInfo.visibleItemsInfo.find { it.key == draggingKey }
            
            if (draggingInfo != null) {
                val currentItemTop = dragY - touchOffsetWithinItem
                val draggingCenter = currentItemTop + draggingInfo.size / 2

                layoutInfo.visibleItemsInfo
                    .find { item ->
                        if (item.key == draggingKey) return@find false
                        val itemCenter = item.offset + item.size / 2
                        
                        if (item.index > draggingInfo.index) {
                            draggingCenter > itemCenter
                        } else {
                            draggingCenter < itemCenter
                        }
                    }
                    ?.let { targetItem ->
                        val fromIndex = localItems.indexOfFirst { itemKey(it) == draggingKey }
                        val toIndex = localItems.indexOfFirst { itemKey(it) == targetItem.key }
                        
                        if (fromIndex != -1 && toIndex != -1) {
                            // Fix "step jumps": if we swap elements at position 0 and we are at the top
                            val isAtTop = state.firstVisibleItemIndex == 0 && state.firstVisibleItemScrollOffset == 0
                            
                            localItems.add(toIndex, localItems.removeAt(fromIndex))
                            
                            if (isAtTop && (fromIndex == 0 || toIndex == 0)) {
                                // Force scroll reset to 0 to avoid LazyColumn jumping
                                state.requestScrollToItem(0)
                            }
                        }
                    }
            }
        }
    }

    // Auto-scroll while dragging
    LaunchedEffect(draggingItemKey) {
        while (draggingItemKey != null && isActive) {
            val layoutInfo = state.layoutInfo
            val viewportHeight = layoutInfo.viewportSize.height.toFloat()
            val edgeTolerance = 100f
            var scrollAmount = 0f
            
            if (dragY < edgeTolerance) {
                if (state.firstVisibleItemIndex > 0 || state.firstVisibleItemScrollOffset > 0) {
                    scrollAmount = (dragY - edgeTolerance) / 2f
                }
            } else if (dragY > viewportHeight - edgeTolerance) {
                if (state.canScrollForward) {
                    scrollAmount = (dragY - (viewportHeight - edgeTolerance)) / 2f
                }
            }
            
            if (scrollAmount != 0f) {
                state.scrollBy(scrollAmount)
                checkForSwaps()
            }
            delay(10)
        }
    }

    Box(modifier = modifier
        .pointerInput(Unit) {
            detectDragGesturesAfterLongPress(
                onDragStart = { offset ->
                    state.layoutInfo.visibleItemsInfo
                        .firstOrNull { offset.y.toInt() in it.offset..(it.offset + it.size) }
                        ?.also { item ->
                            draggingItemKey = item.key
                            dragY = offset.y
                            touchOffsetWithinItem = offset.y - item.offset
                        }
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    dragY += dragAmount.y
                    checkForSwaps()
                },
                onDragEnd = {
                    onReorder(localItems.toList())
                    draggingItemKey = null
                },
                onDragCancel = {
                    draggingItemKey = null
                }
            )
        }
    ) {
        LazyColumn(state = state, modifier = Modifier.fillMaxWidth()) {
            items(localItems, key = { itemKey(it) }) { item ->
                val key = itemKey(item)
                val isDragging = draggingItemKey == key
                val shadowElevation by animateDpAsState(if (isDragging) 8.dp else 0.dp, label = "")
                
                val draggingModifier = if (isDragging) {
                    Modifier
                        .zIndex(10f)
                        .graphicsLayer {
                            // Find the current offset of the element in the list to calculate the translation relative to it
                            val info = state.layoutInfo.visibleItemsInfo.find { it.key == key }
                            if (info != null) {
                                translationY = (dragY - touchOffsetWithinItem) - info.offset
                            }
                            scaleX = 1.05f
                            scaleY = 1.05f
                        }
                        .shadow(shadowElevation)
                } else {
                    Modifier.animateItem() // Ensures smooth reordering of neighbors
                }

                Box(modifier = draggingModifier.fillMaxWidth()) {
                    itemContent(item, isDragging)
                }
            }
        }
    }
}
