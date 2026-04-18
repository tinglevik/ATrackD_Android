package com.example.atrackd.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.atrackd.data.model.ActivityItem
import com.example.atrackd.data.model.TagItem
import com.example.atrackd.ui.components.IconMapper

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditActivityDialog(
    activity: ActivityItem,
    allTags: List<TagItem>,
    onDismiss: () -> Unit,
    onSave: (ActivityItem) -> Unit,
    onDelete: () -> Unit,
    isCreating: Boolean = false,
    dialogBackgroundColor: Color = MaterialTheme.colorScheme.surface,
    dialogContentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    var name by remember { mutableStateOf(activity.name) }
    var selectedColor by remember { mutableStateOf(activity.color) }
    var selectedIconName by remember { mutableStateOf(activity.icon) }
    var showInQuickPanel by remember { mutableStateOf(activity.showInQuickPanel) }
    var selectedTagIds by remember { mutableStateOf(activity.tagIds) }
    var isError by remember { mutableStateOf(false) }
    var showTagMenu by remember { mutableStateOf(false) }

    // Состояние для отображения окон выбора
    var showColorPicker by remember { mutableStateOf(false) }
    var showIconPicker by remember { mutableStateOf(false) }
    
    val dummyFocusRequester = remember { FocusRequester() }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogBackgroundColor,
        titleContentColor = dialogContentColor,
        textContentColor = dialogContentColor,
        iconContentColor = dialogContentColor,

        title = {
            Text(if (isCreating) "Create Activity" else "Edit Activity")
        },

        text = {
            Column {
                Box(
                    modifier = Modifier
                        .size(0.dp)
                        .focusRequester(dummyFocusRequester)
                        .focusable()
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        isError = false
                    },
                    label = {
                        Text(
                            "Name",
                            color = dialogContentColor.copy(alpha = 0.7f)
                        )
                    },
                    isError = isError,
                    singleLine = true,
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = dialogContentColor,
                        unfocusedTextColor = dialogContentColor,
                        focusedBorderColor = dialogContentColor,
                        unfocusedBorderColor = dialogContentColor.copy(alpha = 0.5f),
                        cursorColor = dialogContentColor,
                        errorBorderColor = Color.Red,
                        errorTextColor = dialogContentColor
                    )
                )

                if (isError) {
                    Text(
                        "Name cannot be empty",
                        color = Color.Red,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Кнопка выбора цвета
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showColorPicker = true }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Color", color = dialogContentColor)
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(selectedColor)
                            .border(1.dp, dialogContentColor.copy(alpha = 0.3f), CircleShape)
                    )
                }

                // Кнопка выбора иконки
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showIconPicker = true }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Icon", color = dialogContentColor)
                    Icon(
                        imageVector = IconMapper.getIcon(selectedIconName),
                        contentDescription = selectedIconName,
                        tint = dialogContentColor,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tags section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Tag",
                        fontSize = 16.sp,
                        color = dialogContentColor,
                        modifier = Modifier.padding(top = 12.dp)
                    )

                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box {
                            TextButton(
                                onClick = { showTagMenu = true },
                                colors = ButtonDefaults.textButtonColors(contentColor = dialogContentColor)
                            ) {
                                Text("+ TAG")
                            }
                            DropdownMenu(
                                expanded = showTagMenu,
                                onDismissRequest = { showTagMenu = false },
                                containerColor = dialogBackgroundColor
                            ) {
                                val unselectedTags = allTags.filter { it.id !in selectedTagIds }
                                if (unselectedTags.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("No more tags", color = dialogContentColor.copy(alpha = 0.5f)) },
                                        onClick = { showTagMenu = false }
                                    )
                                } else {
                                    unselectedTags.forEach { tag ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        Icons.AutoMirrored.Filled.Label,
                                                        contentDescription = null,
                                                        tint = tag.color,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(tag.name, color = dialogContentColor)
                                                }
                                            },
                                            onClick = {
                                                selectedTagIds = selectedTagIds + tag.id
                                                showTagMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            selectedTagIds.forEach { tagId ->
                                val tag = allTags.find { it.id == tagId }
                                if (tag != null) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color.Transparent,
                                        modifier = Modifier
                                            .padding(2.dp)
                                            .border(1.dp, dialogContentColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.Label,
                                                contentDescription = null,
                                                tint = tag.color,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = tag.name,
                                                fontSize = 12.sp,
                                                color = dialogContentColor
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove tag",
                                                tint = dialogContentColor,
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clickable {
                                                        selectedTagIds = selectedTagIds - tagId
                                                    }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Show in notification",
                            fontSize = 14.sp,
                            color = dialogContentColor
                        )
                        Text(
                            text = "Quick access from notification bar\nand lock screen",
                            fontSize = 11.sp,
                            color = dialogContentColor.copy(alpha = 0.6f),
                            lineHeight = 14.sp
                        )
                    }
                    Switch(
                        checked = showInQuickPanel,
                        onCheckedChange = { showInQuickPanel = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = dialogBackgroundColor,
                            checkedTrackColor = dialogContentColor,
                            uncheckedThumbColor = dialogContentColor.copy(alpha = 0.5f),
                            uncheckedTrackColor = dialogContentColor.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        },

        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        isError = true
                    } else {
                        onSave(activity.copy(
                            name = name.trim(),
                            color = selectedColor,
                            icon = selectedIconName,
                            showInQuickPanel = showInQuickPanel,
                            tagIds = selectedTagIds
                        ))
                    }
                },
                enabled = name.isNotBlank(),
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = dialogContentColor,
                    contentColor = dialogBackgroundColor
                )
            ) {
                Text("Save")
            }
        },

        dismissButton = {
            Row {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = dialogContentColor
                    )
                ) {
                    Text("Cancel")
                }

                if (!isCreating) {
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.Red
                        )
                    ) {
                        Text("Delete")
                    }
                }
            }
        }
    )

    // Окно выбора цвета
    if (showColorPicker) {
        Dialog(
            onDismissRequest = { showColorPicker = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                ColorPickerScreen(
                    initialColor = selectedColor,
                    onColorConfirmed = {
                        selectedColor = it
                        showColorPicker = false
                    },
                    onDismiss = { showColorPicker = false }
                )
            }
        }
    }

    // Окно выбора иконки
    if (showIconPicker) {
        Dialog(
            onDismissRequest = { showIconPicker = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.95f)
                    .imePadding()
                    .clip(RoundedCornerShape(16.dp))
            ) {
                IconPickerScreen(
                    initialIconName = selectedIconName,
                    onIconSelected = {
                        selectedIconName = it
                        showIconPicker = false
                    },
                    onDismiss = { showIconPicker = false }
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        dummyFocusRequester.requestFocus()
    }
}
