package com.example.actitracker.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest

@Composable
fun AppIcon(
    iconName: String,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    contentDescription: String? = null
) {
    val iconInfo = IconMapper.getIconInfo(iconName)

    val finalModifier = Modifier.size(24.dp).then(modifier)

    if (iconInfo?.assetPath != null) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(iconInfo.assetPath)
                .decoderFactory(SvgDecoder.Factory())
                .build(),
            contentDescription = contentDescription ?: iconName,
            colorFilter = ColorFilter.tint(tint),
            modifier = finalModifier
        )
    } else {
        Icon(
            imageVector = iconInfo?.icon ?: Icons.Default.QuestionMark,
            contentDescription = contentDescription ?: iconName,
            tint = tint,
            modifier = finalModifier
        )
    }
}

