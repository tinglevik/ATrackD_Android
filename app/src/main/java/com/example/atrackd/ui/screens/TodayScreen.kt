package com.example.atrackd.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.atrackd.R
import com.example.atrackd.data.model.ActivityItem
import com.example.atrackd.data.model.TagItem
import com.example.atrackd.ui.components.ActivityRowDimens
import com.example.atrackd.ui.components.CircleIconButton
import com.example.atrackd.ui.components.IconMapper
import com.example.atrackd.ui.components.LongPressActivityRow
import com.example.atrackd.ui.components.formatSeconds
import com.example.atrackd.ui.components.verticalScrollbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TodayScreen(
    activities: List<ActivityItem>,
    activeActivityId: Long?,
    activeStartTime: Long?,
    ticker: Long,
    onStartActivity: (Long) -> Unit,
    onStopActivity: (Long) -> Unit,
    onManageClick: () -> Unit,
    onCreateStart: () -> Unit,
    onCreateDismiss: () -> Unit,
    onCreateSave: (ActivityItem) -> Unit,
    isCreating: Boolean,
    allTags: List<TagItem> = emptyList(),
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = MaterialTheme.colorScheme.onBackground,
    onQuickPanelToggle: (ActivityItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Search and Filter State
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    // Tag Filter State: null = All, -1L = No Tag, else = Tag ID
    var selectedTagFilterId by remember { mutableStateOf<Long?>(null) }
    var showFilterMenu by remember { mutableStateOf(false) }

    // Logic for filtering activities
    val filteredActivities = remember(
        activities,
        searchQuery,
        selectedTagFilterId
    ) {
        activities.filter { activity ->
            val matchesSearch = searchQuery.isBlank() || 
                    activity.name.contains(searchQuery, ignoreCase = true)
            
            val matchesTag = when (selectedTagFilterId) {
                null -> true // All tags
                -1L -> activity.tagIds.isEmpty() // No tag
                else -> activity.tagIds.contains(selectedTagFilterId)
            }
            
            matchesSearch && matchesTag
        }
    }

    // Active activities list for the "Current task" block
    val activeActivities = remember(activities, activeActivityId) {
        activities.filter { it.id == activeActivityId }
    }

    var longPressedActivity by remember { mutableStateOf<ActivityItem?>(null) }

    Scaffold(
        containerColor = backgroundColor,
        contentColor = contentColor,
        topBar = {
            TodayTopBar(
                ticker = ticker,
                contentColor = contentColor,
                onManageClick = onManageClick
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            CircleIconButton(
                onClick = onCreateStart,
                painter = painterResource(R.drawable.ic_add_outline),
                outerShape = CircleShape,
                contentDescription = "Add",
                size = 50.dp,
                containerColor = contentColor,
                iconTint = backgroundColor,
                modifier = Modifier.shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    clip = false
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 2) Block: Current task
            CurrentTaskBlock(
                activeActivities = activeActivities,
                ticker = ticker,
                activeStartTime = activeStartTime,
                contentColor = contentColor,
                onStopActivity = onStopActivity
            )

            // 1) Block: Task + Search + Filter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Task",
                    fontSize = ActivityRowDimens.headerFontSize * 0.7,
                    fontWeight = FontWeight.Medium,
                    color = contentColor.copy(alpha = 0.7f)
                )

                Spacer(
                    Modifier.width(
                        (ActivityRowDimens.activityRowHorizontalSpacerSize.value * 0.7).dp
                    )
                )

                // Search Block
                SearchBox(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    isActive = isSearchActive,
                    onActiveChange = { 
                        isSearchActive = it
                        if (!it) searchQuery = "" 
                    },
                    focusRequester = focusRequester,
                    contentColor = contentColor,
                    modifier = Modifier.weight(1f)
                )

                Spacer(
                    Modifier.width(
                        (ActivityRowDimens.activityRowHorizontalSpacerSize.value * 0.7).dp
                    )
                )

                // Filter Zone
                Box {
                    Row(
                        modifier = Modifier
                            .clickable { showFilterMenu = true }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val filterText = when (selectedTagFilterId) {
                            null -> "All tags"
                            -1L -> "No tag"
                            else -> allTags.find { it.id == selectedTagFilterId }?.name ?: "Tag"
                        }
                        Text(
                            text = filterText,
                            fontSize = ActivityRowDimens.headerFontSize * 0.7,
                            color = contentColor.copy(alpha = 0.7f),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = contentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false },
                        modifier = Modifier.background(backgroundColor)
                    ) {
                        DropdownMenuItem(
                            text = { Text("All tags", color = contentColor) },
                            onClick = {
                                selectedTagFilterId = null
                                showFilterMenu = false
                            }
                        )
                        allTags.forEach { tag ->
                            DropdownMenuItem(
                                text = { Text(tag.name, color = contentColor) },
                                onClick = {
                                    selectedTagFilterId = tag.id
                                    showFilterMenu = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("No tag", color = contentColor) },
                            onClick = {
                                selectedTagFilterId = -1L
                                showFilterMenu = false
                            }
                        )
                    }
                }
            }

            // Main Activities List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScrollbar(listState)
            ) {
                items(filteredActivities, key = { it.id }) { activity ->
                    LongPressActivityRow(
                        activity = activity,
                        isActive = activity.id == activeActivityId,
                        currentTime = ticker,
                        activeStartTime = activeStartTime,
                        allTags = allTags,
                        contentColor = contentColor,
                        onClick = {
                            if (activity.id == activeActivityId) {
                                onStopActivity(activity.id)
                            } else {
                                onStartActivity(activity.id)
                            }
                        },
                        onLongPress = {
                            longPressedActivity = activity
                        }
                    )
                }
            }
        }

        // Dialogs
        if (isCreating) {
            EditActivityDialog(
                activity = ActivityItem(-1, "", Color.Cyan, "Task"),
                allTags = allTags,
                isCreating = true,
                onDismiss = onCreateDismiss,
                onSave = {
                    if (it.name.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Name cannot be empty")
                        }
                    } else {
                        onCreateSave(it)
                        onCreateDismiss()
                    }
                },
                onDelete = {},
                dialogBackgroundColor = contentColor,
                dialogContentColor = backgroundColor
            )
        }

        longPressedActivity?.let { activity ->
            QuickPanelToggleDialog(
                activity = activity,
                onDismiss = { longPressedActivity = null },
                onToggle = { updated ->
                    onQuickPanelToggle(updated)
                    longPressedActivity = null
                },
                dialogBackgroundColor = contentColor,
                dialogContentColor = backgroundColor
            )
        }
    }
}

@Composable
private fun TodayTopBar(
    ticker: Long,
    contentColor: Color,
    onManageClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = ActivityRowDimens.activityWholeRowVerticalPadding,
                horizontal = ActivityRowDimens.activityWholeRowHorizontalPadding
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .clickable(onClick = onManageClick)
                .padding(ActivityRowDimens.activityWholeRowVerticalPadding)
            ,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_manage_activities),
                contentDescription = "Manage activities",
                tint = contentColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Manage",
                fontSize = 12.sp,
                color = contentColor
            )
        }

        val date = SimpleDateFormat(
            "MMM dd (EEE)",
            Locale.getDefault()
        ).format(Date(ticker))

        Text(
            text = date,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}

@Composable
private fun SearchBox(
    query: String,
    onQueryChange: (String) -> Unit,
    isActive: Boolean,
    onActiveChange: (Boolean) -> Unit,
    focusRequester: FocusRequester,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .height(40.dp)
            .then(
                if (isActive) {
                    Modifier.border(
                        BorderStroke(1.dp, contentColor),
                        RoundedCornerShape(8.dp)
                    )
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                if (!isActive) {
                    onActiveChange(true)
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        if (!isActive) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = contentColor,
                modifier = Modifier.padding(start = 4.dp)
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text(
                            text = "Search",
                            color = contentColor.copy(alpha = 0.5f),
                            fontSize = 16.sp
                        )
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        textStyle = TextStyle(color = contentColor, fontSize = 16.sp),
                        cursorBrush = SolidColor(contentColor),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                    )
                    
                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }
                }

                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear",
                    tint = contentColor,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable {
                            onActiveChange(false)
                        }
                )
            }
        }
    }
}

@Composable
private fun CurrentTaskBlock(
    activeActivities: List<ActivityItem>,
    ticker: Long,
    activeStartTime: Long?,
    contentColor: Color,
    onStopActivity: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Text(
            text = "Current task",
            fontSize = ActivityRowDimens.headerFontSize * 0.7,
            color = contentColor.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        AnimatedContent(
            targetState = activeActivities,
            label = "CurrentTaskAnimation",
            transitionSpec = {
                fadeIn(
                    animationSpec = tween(300)
                ) togetherWith fadeOut(
                    animationSpec = tween(300)
                )
            }
        ) { currentActiveList ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(
                        min =
                            ActivityRowDimens.currentTaskPadding * 2 +
                                    ActivityRowDimens.touchTargetSize +
                                    ActivityRowDimens.currentTaskBorder
                    )
                    .then(
                        if (currentActiveList.isNotEmpty()) {
                            Modifier.border(
                                BorderStroke(
                                    ActivityRowDimens.currentTaskBorder,
                                    contentColor
                                ),
                                RoundedCornerShape(8.dp)
                            )
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (currentActiveList.isEmpty()) {
                    Text(
                        text = "No activity running",
                        color = contentColor.copy(alpha = 0.5f),
                        fontSize = 16.sp
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(ActivityRowDimens.currentTaskPadding)
                    ) {
                        currentActiveList.forEach { activity ->
                            val liveSeconds = if (activeStartTime != null) {
                                activity.elapsedSeconds + (ticker - activeStartTime) / 1000
                            } else {
                                activity.elapsedSeconds
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = IconMapper.getIcon(activity.icon),
                                    contentDescription = null,
                                    tint = activity.color,
                                    modifier = Modifier.size(ActivityRowDimens.ACTIVITY_ROW_ICON_SIZE.dp)
                                )

                                Spacer(
                                    modifier = Modifier.width(
                                        (ActivityRowDimens.activityRowHorizontalSpacerSize.value * 0.5)
                                            .dp
                                    )
                                )

                                Text(
                                    text = activity.name,
                                    color = contentColor,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = ActivityRowDimens.headerFontSize,
                                    modifier = Modifier.weight(1f)
                                )

                                Spacer(
                                    modifier = Modifier.width(
                                        (ActivityRowDimens.activityRowHorizontalSpacerSize.value * 0.5)
                                            .dp
                                    )
                                )

                                Text(
                                    text = formatSeconds(liveSeconds),
                                    color = contentColor,
                                    fontSize = ActivityRowDimens.headerFontSize
                                )

                                Box(
                                    modifier = Modifier
                                        .size(ActivityRowDimens.touchTargetSize)
                                        .clickable { onStopActivity(activity.id) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_stop_filled),
                                        contentDescription = "Stop",
                                        tint = contentColor,
                                        modifier = Modifier.size(
                                            ActivityRowDimens.ACTIVITY_ROW_ICON_SIZE.dp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
fun TodayScreenPreview() {
    val now = System.currentTimeMillis()

    val activities = listOf(
        ActivityItem(
            id = 1,
            name = "Guitar practice",
            color = Color(0xFF81C784),
            icon = "Music",
            elapsedSeconds = 120
        ),
        ActivityItem(
            id = 2,
            name = "Reading",
            color = Color(0xFF64B5F6),
            icon = "Reading",
            elapsedSeconds = 540
        ),
        ActivityItem(
            id = 3,
            name = "Workout",
            color = Color(0xFFE57373),
            icon = "Exercise",
            elapsedSeconds = 0
        ),
        ActivityItem(
            id = 4,
            name = "Meditation",
            color = Color(0xFFBA68C8),
            icon = "Meditation",
            elapsedSeconds = 0
        )
    )

    TodayScreen(
        activities = activities,
        activeActivityId = 1,
        activeStartTime = now - 60_000,
        ticker = now,
        onStartActivity = {},
        onStopActivity = {},
        onManageClick = {},
        onCreateStart = {},
        onCreateDismiss = {},
        onCreateSave = {},
        isCreating = false,
        onQuickPanelToggle = {}
    )
}
