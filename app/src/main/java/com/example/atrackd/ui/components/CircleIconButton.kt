package com.example.atrackd.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CircleIconButton(
    onClick: () -> Unit,
    painter: Painter? = null,
    outerShape: Shape,
    contentDescription: String,
    size: Dp = 40.dp,
    containerColor: Color = Color.Transparent, // цвет заполнения
    iconTint: Color = MaterialTheme.colorScheme.primary, // цвет иконки
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = outerShape,
        color = containerColor,
        modifier = modifier.size(size)
    ) {
        if (painter != null) {
            Icon(
                painter = painter,
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}