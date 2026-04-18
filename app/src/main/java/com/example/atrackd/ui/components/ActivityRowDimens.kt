package com.example.atrackd.ui.components

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// internal — доступен внутри модуля, не виден снаружи
internal object ActivityRowDimens {
    val minRowHeight = 48.dp
    val dotSize = 4.dp // Должно совпадать с @dimen/activity_dot_size
    val dotSpacing = 4.dp
    val iconCarouselSpacing = 2.dp // Должно совпадать с @dimen/activity_icon_dot_spacing
    val activityWholeRowVerticalPadding = 4.dp
    val activityWholeRowHorizontalPadding = 16.dp
    val headerFontSize = 16.sp
    val firstStartDayTimeFontSize = headerFontSize * 0.8f
    const val ACTIVITY_ROW_ICON_SIZE = 24
    val activityRowHorizontalSpacerSize = 12.dp
    val currentTaskPadding = 8.dp
    val currentTaskBorder = 1.dp
    val touchTargetSize = 48.dp;
    val timerFontSize = 16.sp
}