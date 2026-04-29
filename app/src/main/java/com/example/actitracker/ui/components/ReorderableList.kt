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
    
    // We use a stable MutableStateList and update its content instead of replacing the instance
    val localItems = remember { mutableStateListOf<T>() }
    
    // Sync localItems with external items only when not dragging
    LaunchedEffect(items) {
        if (draggingItemKey == null) {
            localItems.clear()
            localItems.addAll(items)
        }
    }

    // Wrap checkForSwaps in a state to ensure LaunchEffect always uses the latest logic
    val currentCheckForSwaps by rememberUpdatedState {
        val draggingKey = draggingItemKey ?: return@rememberUpdatedState
        val layoutInfo = state.layoutInfo
        val draggingInfo = layoutInfo.visibleItemsInfo.find { it.key == draggingKey } ?: return@rememberUpdatedState
        
        val targetItemTop = dragY - touchOffsetWithinItem
        val draggingCenter = targetItemTop + draggingInfo.size / 2
        
        val targetItem = layoutInfo.visibleItemsInfo.find { item ->
            if (item.key == draggingKey) return@find false
            
            val itemCenter = item.offset + item.size / 2
            if (item.index > draggingInfo.index) {
                draggingCenter > itemCenter
            } else {
                draggingCenter < itemCenter
            }
        }
        
        if (targetItem != null) {
            val fromIndex = localItems.indexOfFirst { itemKey(it) == draggingKey }
            val toIndex = localItems.indexOfFirst { itemKey(it) == targetItem.key }
            if (fromIndex != -1 && toIndex != -1) {
                localItems.add(toIndex, localItems.removeAt(fromIndex))
            }
        }
    }

    // Auto-scroll logic
    LaunchedEffect(draggingItemKey) {
        while (draggingItemKey != null && isActive) {
            val layoutInfo = state.layoutInfo
            val viewportTop = layoutInfo.viewportStartOffset.toFloat()
            val viewportBottom = layoutInfo.viewportEndOffset.toFloat()
            
            val edgeTolerance = 150f
            var scrollAmount = 0f
            
            if (dragY < viewportTop + edgeTolerance) {
                scrollAmount = (dragY - (viewportTop + edgeTolerance)) / 2.5f
            } else if (dragY > viewportBottom - edgeTolerance) {
                scrollAmount = (dragY - (viewportBottom - edgeTolerance)) / 2.5f
            }
            
            if (scrollAmount != 0f) {
                state.scrollBy(scrollAmount)
                currentCheckForSwaps()
            }
            delay(10)
        }
    }

    Box(modifier = modifier
        .pointerInput(items) { // Restart pointerInput if items list object changes (e.g. after database update)
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
                    currentCheckForSwaps()
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
                val isDragging = draggingItemKey == itemKey(item)
                val shadowElevation by animateDpAsState(if (isDragging) 12.dp else 0.dp, label = "")
                
                val draggingModifier = if (isDragging) {
                    Modifier
                        .zIndex(10f)
                        .graphicsLayer {
                            val info = state.layoutInfo.visibleItemsInfo.find { it.key == itemKey(item) }
                            if (info != null) {
                                translationY = (dragY - touchOffsetWithinItem) - info.offset
                            }
                            scaleX = 1.04f
                            scaleY = 1.04f
                        }
                        .shadow(shadowElevation)
                } else {
                    Modifier.animateItem()
                }
                
                Box(modifier = draggingModifier.fillMaxWidth()) {
                    itemContent(item, isDragging)
                }
            }
        }
    }
}
