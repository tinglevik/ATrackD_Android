package com.example.atrackd.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ContrastSuggestionDialog(
    backgroundColor: Color,
    textColor: Color,
    isBackgroundChange: Boolean,
    suggestions: List<Pair<String, Color>>,
    onSuggestionSelected: (Color) -> Unit,
    onOpenColorPicker: () -> Unit,
    onKeepAnyway: () -> Unit, // ✅ Новый callback
    onDismiss: () -> Unit
) {
    var selectedColor by remember { mutableStateOf(suggestions.firstOrNull()?.second) }

    AlertDialog(
        onDismissRequest = { /* Нельзя закрыть без выбора */ },
        title = {
            Text(
                "Low contrast!",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    if (isBackgroundChange)
                        "The current text color may not be readable on the new background."
                    else
                        "The chosen text color is not readable on the current background.",
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Превью
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(backgroundColor)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sample Text Preview",
                        color = selectedColor ?: textColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Choose a readable color:",
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Варианты цветов
                suggestions.forEach { (name, color) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedColor = color }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedColor == color,
                            onClick = { selectedColor = color }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(1.dp, Color.Gray, CircleShape)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(name, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Кнопка "выбрать вручную"
                OutlinedButton(
                    onClick = onOpenColorPicker,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (isBackgroundChange) "Pick text color manually"
                        else "Pick background color manually"
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ✅ Кнопка "оставить как есть"
                TextButton(
                    onClick = onKeepAnyway,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Keep anyway (not recommended)",
                        color = Color(0xFFB71C1C) // Красный для предупреждения
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { selectedColor?.let { onSuggestionSelected(it) } },
                enabled = selectedColor != null
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}