package com.example.atrackd.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.atrackd.data.model.ActivityItem

@Composable
fun QuickPanelToggleDialog(
    activity: ActivityItem,
    onDismiss: () -> Unit,
    onToggle: (ActivityItem) -> Unit,
    dialogBackgroundColor: Color = MaterialTheme.colorScheme.surface,
    dialogContentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val newState = !activity.showInQuickPanel

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogBackgroundColor,
        titleContentColor = dialogContentColor,
        textContentColor = dialogContentColor,
        title = {
            Text(
                text = "${activity.icon} ${activity.name}",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = if (newState)
                    "Add to notification panel and lock screen for quick access?"
                else
                    "Remove from notification panel and lock screen?"
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onToggle(activity.copy(showInQuickPanel = newState))
                    onDismiss()
                },
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = dialogContentColor,
                    contentColor = dialogBackgroundColor
                )
            ) {
                Text(if (newState) "Add" else "Remove")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = dialogContentColor
                )
            ) {
                Text("Cancel")
            }
        }
    )
}