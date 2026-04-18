package com.example.atrackd.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import com.example.atrackd.data.model.TagItem

@Composable
fun EditTagDialog(
    tag: TagItem,
    onDismiss: () -> Unit,
    onSave: (TagItem) -> Unit,
    onDelete: () -> Unit,
    isCreating: Boolean = false,
    dialogBackgroundColor: Color = MaterialTheme.colorScheme.surface,
    dialogContentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    var name by remember { mutableStateOf(tag.name) }
    var selectedColor by remember { mutableStateOf(tag.color) }
    var isError by remember { mutableStateOf(false) }
    
    // Состояние для отображения окна выбора цвета
    var showColorPicker by remember { mutableStateOf(false) }

    val dummyFocusRequester = remember { FocusRequester() }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogBackgroundColor,
        titleContentColor = dialogContentColor,
        textContentColor = dialogContentColor,
        title = { Text(if (isCreating) "Create Tag" else "Edit Tag") },
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
                    label = { Text("Name", color = dialogContentColor.copy(alpha = 0.7f)) },
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
                        cursorColor = dialogContentColor
                    )
                )
                if (isError) {
                    Text("Name cannot be empty", color = Color.Red, modifier = Modifier.padding(top = 4.dp))
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        isError = true
                    } else {
                        onSave(tag.copy(name = name.trim(), color = selectedColor))
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

    // Окно выбора цвета (ColorPickerScreen) поверх текущего диалога
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
                    // Убрано жесткое ограничение высоты
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

    LaunchedEffect(Unit) {
        dummyFocusRequester.requestFocus()
    }
}
