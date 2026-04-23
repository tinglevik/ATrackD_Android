package com.example.actitracker.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.actitracker.R
import com.example.actitracker.data.model.ActivityItem
import com.example.actitracker.data.model.TagItem
import com.example.actitracker.ui.theme.actitrackerTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LongPressActivityRow(
    activity: ActivityItem,
    isActive: Boolean,
    currentTime: Long,
    activeStartTime: Long?,
    allTags: List<TagItem> = emptyList(),
    contentColor: Color = Color.Black,
    onClick: () -> Unit,
    onLongPress: () -> Unit
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

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongPress
                )
                .padding(
                    vertical = ActivityRowDimens.activityWholeRowVerticalPadding,
                    horizontal = ActivityRowDimens.activityWholeRowHorizontalPadding
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Иконка + карусель
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
                        modifier = Modifier.height(ActivityRowDimens.dotSize),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        if (isActive) {
                            DotsLoader(color = contentColor)
                        }
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(ActivityRowDimens.minRowHeight),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = activity.name,
                        fontSize = ActivityRowDimens.headerFontSize,
                        fontWeight = FontWeight.Medium,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    activity.firstStartDayTime?.let { firstStart ->
                        Text(
                            text = stringResource(
                                R.string.started_at_format,
                                SimpleDateFormat("HH:mm", Locale.getDefault()).format(
                                    Date(
                                        firstStart
                                    )
                                )
                            ),
                            fontSize = ActivityRowDimens.firstStartDayTimeFontSize,
                            color = contentColor.copy(alpha = 0.7f),
                        )
                    }
                }


                if (isActive || liveSeconds > 0) {
                    Text(
                        text = formatSeconds(liveSeconds),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = contentColor
                    )
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

@Preview(showBackground = true)
@Composable
fun LongPressActivityRowPreview() {
    val sampleActivity = ActivityItem(
        id = 1,
        name = "Walking",
        icon = "Walking",
        color = Color(0xFF2196F3),
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
        )
    )

    actitrackerTheme {
        LongPressActivityRow(
            activity = sampleActivity,
            isActive = true,
            currentTime = System.currentTimeMillis(),
            activeStartTime = System.currentTimeMillis() - 120_000, // +2 min
            allTags = sampleTags,
            onClick = {},
            onLongPress = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LongPressActivityRowInactivePreview() {
    val sampleActivity = ActivityItem(
        id = 1,
        name = "Walking",
        icon = "Walking",
        color = Color(0xFF2196F3),
        elapsedSeconds = 3600, // 01:00:00
        firstStartDayTime = System.currentTimeMillis() - 7_200_000,
        tagIds = emptyList()
    )

    actitrackerTheme {
        LongPressActivityRow(
            activity = sampleActivity,
            isActive = false,
            currentTime = System.currentTimeMillis(),
            activeStartTime = null,
            allTags = emptyList(),
            onClick = {},
            onLongPress = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LongPressActivityRowNeverStartedPreview() {
    val sampleActivity = ActivityItem(
        id = 1,
        name = "Walking",
        icon = "Walking",
        color = Color(0xFF2196F3),
        elapsedSeconds = 0,
        firstStartDayTime = null,
        tagIds = emptyList()
    )

    actitrackerTheme {
        LongPressActivityRow(
            activity = sampleActivity,
            isActive = false,
            currentTime = System.currentTimeMillis(),
            activeStartTime = null,
            allTags = emptyList(),
            onClick = {},
            onLongPress = {}
        )
    }
}
