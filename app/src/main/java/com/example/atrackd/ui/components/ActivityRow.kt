package com.example.atrackd.ui.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.atrackd.data.model.ActivityItem
import com.example.atrackd.data.model.TagItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ActivityRow(
    activity: ActivityItem,
    isActive: Boolean,
    currentTime: Long,
    activeStartTime: Long?,
    allTags: List<TagItem> = emptyList(),
    contentColor: Color = LocalContentColor.current,
    showTimer: Boolean = true,
    showFirstStart: Boolean = true,
    onClick: () -> Unit
) {
    val liveSeconds = remember(
        activity.elapsedSeconds, isActive, currentTime, activeStartTime
    ) {
        if (isActive && activeStartTime != null) {
            activity.elapsedSeconds + (currentTime - activeStartTime) / 1000
        } else {
            activity.elapsedSeconds
        }
    }

    val activityTags = remember(activity.tagIds, allTags) {
        activity.tagIds.mapNotNull { id -> allTags.find { it.id == id } }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(
                    vertical = ActivityRowDimens.activityWholeRowVerticalPadding,
                    horizontal = ActivityRowDimens.activityWholeRowHorizontalPadding
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = IconMapper.getIcon(activity.icon),
                        contentDescription = null,
                        tint = activity.color,
                        modifier = Modifier.size(ActivityRowDimens.ACTIVITY_ROW_ICON_SIZE.dp)
                    )

                    Spacer(modifier = Modifier.height(ActivityRowDimens.iconCarouselSpacing))

                    Box(
                        modifier = Modifier
                            .height(ActivityRowDimens.dotSize),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        if (isActive) {
                            DotsLoader(color = contentColor)
                        }
                    }
                }

                Spacer(Modifier.width(ActivityRowDimens.activityRowHorizontalSpacerSize))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = activity.name,
                        fontSize = ActivityRowDimens.headerFontSize,
                        fontWeight = FontWeight.Medium,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (showFirstStart) {
                        activity.firstStartDayTime?.let { firstStart ->
                            Text(
                                text = "First start: ${
                                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(
                                        Date(firstStart)
                                    )
                                }",
                                fontSize = ActivityRowDimens.firstStartDayTimeFontSize,
                                color = contentColor.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                Spacer(
                    Modifier.width(
                        (ActivityRowDimens.activityRowHorizontalSpacerSize.value * 0.5)
                            .dp
                    )
                )

                // ⏱️ ТАЙМЕР
                if (showTimer && (isActive || liveSeconds > 0)) {
                    Text(
                        text = formatSeconds(liveSeconds),
                        fontSize = ActivityRowDimens.headerFontSize,
                        fontWeight = FontWeight.Medium,
                        color = contentColor
                    )
                }
            }

            // Tags List - starting from 2/3 of the row width
            if (activityTags.isNotEmpty()) {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val startOffset = maxWidth * (2f / 3f)
                    Row(
                        modifier = Modifier
                            .padding(start = startOffset)
                            .height(24.dp)
                            .align(Alignment.TopStart),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        if (activityTags.size == 1) {
                            val tag = activityTags[0]
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Label,
                                contentDescription = null,
                                tint = tag.color,
                                modifier = Modifier.size(
                                    ActivityRowDimens.ACTIVITY_ROW_ICON_SIZE.dp
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = tag.name,
                                fontSize = ActivityRowDimens.headerFontSize,
                                color = contentColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            activityTags.forEach { tag ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Label,
                                        contentDescription = null,
                                        tint = tag.color,
                                        modifier = Modifier.size(
                                            (ActivityRowDimens.ACTIVITY_ROW_ICON_SIZE * 0.7).dp
                                        )
                                    )
                                    Text(
                                        text = tag.name.take(1).uppercase(),
                                        fontSize =
                                            (ActivityRowDimens.headerFontSize.value * 0.7).sp,
                                        color = contentColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 8.dp),
            thickness = 1.dp,
            color = contentColor.copy(alpha = 0.1f)
        )
    }
}

fun formatSeconds(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%02d:%02d:%02d".format(h, m, s)
    else "%02d:%02d".format(m, s)
}

private val EllipsisMoveEasing = CubicBezierEasing(0.25f, 1f, 0.75f, 1f)

@Composable
fun DotsLoader(
    dotSize: Dp = ActivityRowDimens.dotSize,
    color: Color = Color.Gray
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ellipsis")

    val rawProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 500,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "ellipsis-raw"
    )

    val animFraction = 350f / 500f
    val progress = if (rawProgress >= animFraction) {
        1f
    } else {
        val linearInPhase = rawProgress / animFraction
        EllipsisMoveEasing.transform(linearInPhase)
    }

    // Расстояние между центрами соседних точек
    val spacing = dotSize + ActivityRowDimens.dotSpacing
    val density = LocalDensity.current
    val spacingPx = with(density) { spacing.toPx() }

    // Ширина контейнера = ровно 3 точки с промежутками
    val containerWidth = dotSize + spacing * 2

    Box(
        modifier = Modifier
            .width(containerWidth)
            .height(dotSize),
        contentAlignment = Alignment.CenterStart
    ) {
        EllipsisDot(
            dotSize = dotSize,
            color = color,
            offsetXPx = 0f,
            scale = progress
        )

        EllipsisDot(
            dotSize = dotSize,
            color = color,
            offsetXPx = spacingPx * progress,
            scale = 1f
        )

        EllipsisDot(
            dotSize = dotSize,
            color = color,
            offsetXPx = spacingPx + spacingPx * progress,
            scale = 1f
        )

        EllipsisDot(
            dotSize = dotSize,
            color = color,
            offsetXPx = spacingPx * 2f,
            scale = 1f - progress
        )
    }
}

@Composable
private fun EllipsisDot(
    dotSize: Dp,
    color: Color,
    offsetXPx: Float,
    scale: Float
) {
    Box(
        modifier = Modifier
            .offset { IntOffset(offsetXPx.roundToInt(), 0) }
            .scale(scale)
            .size(dotSize)
            .background(color, CircleShape)
    )
}

@Preview(showBackground = true)
@Composable
fun ActivityRowPreview() {
    val sampleActivity = ActivityItem(
        id = 1,
        name = "Running",
        icon = "Exercise",
        color = Color(0xFF6200EE),
        elapsedSeconds = 754, // 12:34
        firstStartDayTime = System.currentTimeMillis() - 3_600_000,
        tagIds = listOf(1, 2)
    )

    val sampleTags = listOf(
        TagItem(
            id = 1,
            name = "Sport",
            color = Color(0xFF4CAF50)
        ),
        TagItem(
            id = 2,
            name = "Health",
            color = Color(0xFFFF9800)
        ),
        TagItem(
            id = 3,
            name = "Something1",
            color = Color(0xFF009800)
        ),
        TagItem(
            id = 4,
            name = "Something2",
            color = Color(0xFF55FF00)
        )
    )

    ActivityRow(
        activity = sampleActivity,
        isActive = true,
        currentTime = System.currentTimeMillis(),
        activeStartTime = System.currentTimeMillis() - 120_000, // +2 мин
        allTags = sampleTags,
        showTimer = true,
        showFirstStart = true,
        onClick = {}
    )
}
