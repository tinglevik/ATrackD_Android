package com.example.atrackd.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.atrackd.R
import com.example.atrackd.data.model.ActivityItem
import com.example.atrackd.data.model.GoalItem
import com.example.atrackd.data.model.TagItem
import com.example.atrackd.ui.components.ActivityRow
import com.example.atrackd.ui.components.verticalScrollbar
import com.example.atrackd.ui.theme.ATrackDTheme

enum class ManageTab(
    val title: String,
    val contentDescription: String,
    val iconSelected: Int,
    val iconUnselected: Int
) {
    ACTIVITIES(
        "Manage Activities",
        "Activities",
        R.drawable.ic_manage_activities_filled,
        R.drawable.ic_manage_activities_outline
    ),
    TAGS(
        "Manage Tags",
        "Tags",
        R.drawable.ic_label_filled,
        R.drawable.ic_label_outline
    ),
    GOALS(
        "Manage Goals",
        "Goals",
        R.drawable.ic_goal_filled,
        R.drawable.ic_goal_outline
    )
}

@Composable
fun ManageActivitiesScreen(
    activities: List<ActivityItem>,
    onActivityUpdate: (ActivityItem) -> Unit,
    onActivityCreate: (ActivityItem) -> Unit,
    onActivityDelete: (Long) -> Unit,
    tags: List<TagItem> = emptyList(),
    onTagUpdate: (TagItem) -> Unit = {},
    onTagCreate: (TagItem) -> Unit = {},
    onTagDelete: (Long) -> Unit = {},
    goals: List<GoalItem> = emptyList(),
    onGoalUpdate: (GoalItem) -> Unit = {},
    onGoalCreate: (GoalItem) -> Unit = {},
    onGoalDelete: (Long) -> Unit = {},
    navController: NavHostController,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = MaterialTheme.colorScheme.onBackground,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(ManageTab.ACTIVITIES) }

    var showCreateDialog by remember { mutableStateOf(false) }
    var editingActivity by remember { mutableStateOf<ActivityItem?>(null) }
    var editingTag by remember { mutableStateOf<TagItem?>(null) }
    var editingGoal by remember { mutableStateOf<GoalItem?>(null) }

    var itemToDeleteId by remember { mutableStateOf<Long?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    Scaffold(
        containerColor = backgroundColor,
        contentColor = contentColor,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .width(48.dp)
                            .fillMaxHeight()
                            .clickable { navController.popBackStack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back_arrow_outline),
                            contentDescription = "Back",
                            tint = contentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        ManageTab.entries.forEach { tab ->
                            val isSelected = tab == selectedTab
                            Column(
                                modifier = Modifier
                                    .width(48.dp)
                                    .fillMaxHeight()
                                    .clickable { selectedTab = tab },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(
                                        if (isSelected)
                                            tab.iconSelected
                                        else tab.iconUnselected
                                    ),
                                    contentDescription = tab.contentDescription,
                                    tint = if (isSelected) {
                                        contentColor
                                    } else contentColor.copy(alpha = 0.5f),
                                    modifier = Modifier.size(24.dp)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(3.dp)
                                            .background(contentColor)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(3.dp))
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .width(48.dp)
                            .fillMaxHeight()
                            .clickable { showCreateDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_add_outline),
                            contentDescription = "Add",
                            tint = contentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Text(
                    text = selectedTab.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = contentColor,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 4.dp)
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                ManageTab.ACTIVITIES -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScrollbar(listState)
                    ) {
                        items(activities) { activity ->
                            ActivityRow(
                                activity = activity,
                                isActive = false,
                                currentTime = 0L,
                                activeStartTime = null,
                                allTags = tags,
                                contentColor = contentColor,
                                showTimer = false,
                                showFirstStart = false,
                                onClick = { editingActivity = activity }
                            )
                        }
                    }
                }

                ManageTab.TAGS -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(tags) { tag ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { editingTag = tag }
                                        .padding(vertical = 8.dp, horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Label,
                                        contentDescription = null,
                                        tint = tag.color,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = tag.name,
                                        fontSize = 16.sp,
                                        color = contentColor
                                    )
                                }
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    thickness = 1.dp,
                                    color = contentColor.copy(alpha = 0.1f)
                                )
                            }
                        }
                    }
                }

                ManageTab.GOALS -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(goals) { goal ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { editingGoal = goal }
                                        .padding(vertical = 8.dp, horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Flag,
                                        contentDescription = null,
                                        tint = contentColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = goal.name,
                                            fontSize = 16.sp,
                                            color = contentColor
                                        )
                                        Text(
                                            text = "${goal.targetSeconds / 3600}h per ${goal.period.lowercase()}",
                                            fontSize = 12.sp,
                                            color = contentColor.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    thickness = 1.dp,
                                    color = contentColor.copy(alpha = 0.1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dialogs logic based on tab
        when (selectedTab) {
            ManageTab.ACTIVITIES -> {
                if (showCreateDialog) {
                    EditActivityDialog(
                        // Смена иконки по умолчанию на закрашенный круг
                        activity = ActivityItem(-1, "", Color.Cyan, "●"),
                        allTags = tags,
                        isCreating = true,
                        onDismiss = { showCreateDialog = false },
                        onSave = { onActivityCreate(it); showCreateDialog = false },
                        onDelete = {},
                        dialogBackgroundColor = contentColor,
                        dialogContentColor = backgroundColor
                    )
                }
                editingActivity?.let { activity ->
                    EditActivityDialog(
                        activity = activity,
                        allTags = tags,
                        onDismiss = { editingActivity = null },
                        onSave = { onActivityUpdate(it); editingActivity = null },
                        onDelete = { itemToDeleteId = activity.id; showDeleteConfirm = true },
                        dialogBackgroundColor = contentColor,
                        dialogContentColor = backgroundColor
                    )
                }
            }

            ManageTab.TAGS -> {
                if (showCreateDialog) {
                    EditTagDialog(
                        tag = TagItem(-1, "", Color.Cyan),
                        isCreating = true,
                        onDismiss = { showCreateDialog = false },
                        onSave = { onTagCreate(it); showCreateDialog = false },
                        onDelete = {},
                        dialogBackgroundColor = contentColor,
                        dialogContentColor = backgroundColor
                    )
                }
                editingTag?.let { tag ->
                    EditTagDialog(
                        tag = tag,
                        onDismiss = { editingTag = null },
                        onSave = { onTagUpdate(it); editingTag = null },
                        onDelete = { itemToDeleteId = tag.id; showDeleteConfirm = true },
                        dialogBackgroundColor = contentColor,
                        dialogContentColor = backgroundColor
                    )
                }
            }

            ManageTab.GOALS -> {
                if (showCreateDialog) {
                    EditGoalDialog(
                        goal = GoalItem(-1, "", 0, "DAILY"),
                        isCreating = true,
                        onDismiss = { showCreateDialog = false },
                        onSave = { onGoalCreate(it); showCreateDialog = false },
                        onDelete = {},
                        dialogBackgroundColor = contentColor,
                        dialogContentColor = backgroundColor
                    )
                }
                editingGoal?.let { goal ->
                    EditGoalDialog(
                        goal = goal,
                        onDismiss = { editingGoal = null },
                        onSave = { onGoalUpdate(it); editingGoal = null },
                        onDelete = { itemToDeleteId = goal.id; showDeleteConfirm = true },
                        dialogBackgroundColor = contentColor,
                        dialogContentColor = backgroundColor
                    )
                }
            }
        }

        if (showDeleteConfirm && itemToDeleteId != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Delete item?") },
                text = { Text("Are you sure?") },
                confirmButton = {
                    Button(
                        onClick = {
                            when (selectedTab) {
                                ManageTab.ACTIVITIES -> onActivityDelete(itemToDeleteId!!)
                                ManageTab.TAGS -> onTagDelete(itemToDeleteId!!)
                                ManageTab.GOALS -> onGoalDelete(itemToDeleteId!!)
                            }
                            showDeleteConfirm = false
                            itemToDeleteId = null
                            editingActivity = null
                            editingTag = null
                            editingGoal = null
                        },
                        shape = RectangleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red,
                            contentColor = Color.White
                        )
                    ) { Text("Yes") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ManageActivitiesScreenPreview() {
    val sampleTags = listOf(
        TagItem(1, "Work", Color(0xFF2196F3)),
        TagItem(2, "Personal", Color(0xFF4CAF50)),
        TagItem(3, "Health", Color(0xFFFF9800))
    )

    val sampleActivities = listOf(
        ActivityItem(
            id = 1,
            name = "Coding",
            color = Color(0xFF2196F3),
            icon = "Code",
            elapsedSeconds = 3600,
            tagIds = listOf(1)
        ),
        ActivityItem(
            id = 2,
            name = "Reading",
            color = Color(0xFF4CAF50),
            icon = "Book",
            elapsedSeconds = 1800,
            tagIds = listOf(2)
        ),
        ActivityItem(
            id = 3,
            name = "Gym",
            color = Color(0xFFFF9800),
            icon = "Exercise",
            elapsedSeconds = 0,
            tagIds = listOf(3)
        )
    )

    val sampleGoals = listOf(
        GoalItem(1, "Daily Coding", 3600 * 4, "DAILY"),
        GoalItem(2, "Weekly Exercise", 3600 * 10, "WEEKLY")
    )

    ATrackDTheme {
        ManageActivitiesScreen(
            activities = sampleActivities,
            onActivityUpdate = {},
            onActivityCreate = {},
            onActivityDelete = {},
            tags = sampleTags,
            onTagUpdate = {},
            onTagCreate = {},
            onTagDelete = {},
            goals = sampleGoals,
            onGoalUpdate = {},
            onGoalCreate = {},
            onGoalDelete = {},
            navController = rememberNavController()
        )
    }
}