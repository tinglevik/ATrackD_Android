package com.example.atrackd.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.atrackd.data.model.GoalItem

@Composable
fun EditGoalDialog(
    goal: GoalItem,
    onDismiss: () -> Unit,
    onSave: (GoalItem) -> Unit,
    onDelete: () -> Unit,
    isCreating: Boolean = false,
    dialogBackgroundColor: Color = MaterialTheme.colorScheme.surface,
    dialogContentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    var name by remember { mutableStateOf(goal.name) }
    var targetHours by remember { mutableStateOf((goal.targetSeconds / 3600).toString()) }
    var period by remember { mutableStateOf(goal.period) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogBackgroundColor,
        titleContentColor = dialogContentColor,
        textContentColor = dialogContentColor,
        title = { Text(if (isCreating) "Create Goal" else "Edit Goal") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        isError = false
                    },
                    label = { Text("Name", color = dialogContentColor.copy(alpha = 0.7f)) },
                    isError = isError,
                    singleLine = true,
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = dialogContentColor,
                        unfocusedTextColor = dialogContentColor,
                        focusedBorderColor = dialogContentColor,
                        unfocusedBorderColor = dialogContentColor.copy(alpha = 0.5f),
                        cursorColor = dialogContentColor
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = targetHours,
                    onValueChange = { targetHours = it.filter { char -> char.isDigit() } },
                    label = { Text("Target Hours", color = dialogContentColor.copy(alpha = 0.7f)) },
                    singleLine = true,
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = dialogContentColor,
                        unfocusedTextColor = dialogContentColor,
                        focusedBorderColor = dialogContentColor,
                        unfocusedBorderColor = dialogContentColor.copy(alpha = 0.5f),
                        cursorColor = dialogContentColor
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Period", color = dialogContentColor)
                Row {
                    RadioButton(selected = period == "DAILY", onClick = { period = "DAILY" })
                    Text("Daily", modifier = Modifier.padding(start = 8.dp), color = dialogContentColor)
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = period == "WEEKLY", onClick = { period = "WEEKLY" })
                    Text("Weekly", modifier = Modifier.padding(start = 8.dp), color = dialogContentColor)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        isError = true
                    } else {
                        val seconds = (targetHours.toLongOrNull() ?: 0L) * 3600
                        onSave(goal.copy(name = name.trim(), targetSeconds = seconds, period = period))
                    }
                },
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = dialogContentColor,
                    contentColor = dialogBackgroundColor
                )
            ) { Text("Save") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = dialogContentColor)) {
                    Text("Cancel")
                }
                if (!isCreating) {
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) {
                        Text("Delete")
                    }
                }
            }
        }
    )
}
