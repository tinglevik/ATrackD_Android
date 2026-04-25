package com.example.actitracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.actitracker.R
import com.example.actitracker.ui.components.ContrastUtils
import com.example.actitracker.viewmodel.SettingsViewModel

private enum class ColorPickerTarget { BACKGROUND, TEXT }

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onNavigateToLicenses: () -> Unit,
    contentColor: Color = Color.Black
) {
    val backgroundColorState by settingsViewModel.backgroundColor.collectAsState()
    val savedContentColor by settingsViewModel.contentColor.collectAsState()

    var colorPickerTarget by remember { mutableStateOf<ColorPickerTarget?>(null) }
    var showContrastDialog by remember { mutableStateOf(false) }
    var pendingColor by remember { mutableStateOf<Color?>(null) }
    var contrastDialogSource by remember { mutableStateOf<ColorPickerTarget?>(null) }
    var openedFromContrastDialog by remember { mutableStateOf(false) }
    var colorBeforeContrastFlow by remember { mutableStateOf<Color?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    Scaffold(
        containerColor = backgroundColorState,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->

        Box(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when {
                    colorPickerTarget != null -> {
                        val isBackground = colorPickerTarget == ColorPickerTarget.BACKGROUND
                        ColorPickerScreen(
                            initialColor = if (isBackground) backgroundColorState else savedContentColor,
                            contrastWarning = if (isBackground)
                                stringResource(R.string.contrast_warning_background)
                            else
                                stringResource(R.string.contrast_warning_text),
                            onColorConfirmed = { color ->
                                val target = colorPickerTarget
                                colorPickerTarget = null
                                openedFromContrastDialog = false
                                colorBeforeContrastFlow = null
                                when (target) {
                                    ColorPickerTarget.BACKGROUND -> {
                                        if (!ContrastUtils.isReadable(savedContentColor, color)) {
                                            pendingColor = color
                                            contrastDialogSource = ColorPickerTarget.BACKGROUND
                                            showContrastDialog = true
                                        } else {
                                            settingsViewModel.saveBackgroundColor(color)
                                        }
                                    }
                                    ColorPickerTarget.TEXT -> {
                                        if (!ContrastUtils.isReadable(color, backgroundColorState)) {
                                            pendingColor = color
                                            contrastDialogSource = ColorPickerTarget.TEXT
                                            showContrastDialog = true
                                        } else {
                                            settingsViewModel.saveContentColor(color)
                                        }
                                    }
                                    null -> {}
                                }
                            },
                            onDismiss = {
                                colorPickerTarget = null
                                if (openedFromContrastDialog && colorBeforeContrastFlow != null) {
                                    when (contrastDialogSource) {
                                        ColorPickerTarget.BACKGROUND ->
                                            settingsViewModel.saveBackgroundColor(colorBeforeContrastFlow!!)
                                        ColorPickerTarget.TEXT ->
                                            settingsViewModel.saveContentColor(colorBeforeContrastFlow!!)
                                        null -> {}
                                    }
                                }
                                openedFromContrastDialog = false
                                colorBeforeContrastFlow = null
                                contrastDialogSource = null
                            }
                        )
                    }
                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.settings_title),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = contentColor,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )
                            
                            Text(
                                text = stringResource(R.string.settings_appearance),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = contentColor.copy(alpha = 0.6f),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            SettingsColorCard(
                                title = stringResource(R.string.settings_background_color),
                                subtitle = stringResource(R.string.settings_background_color_desc),
                                color = backgroundColorState,
                                onClick = {
                                    colorPickerTarget = ColorPickerTarget.BACKGROUND
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            SettingsColorCard(
                                title = stringResource(R.string.settings_content_color),
                                subtitle = stringResource(R.string.settings_content_color_desc),
                                color = savedContentColor,
                                onClick = {
                                    colorPickerTarget = ColorPickerTarget.TEXT
                                }
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Text(
                                text = stringResource(R.string.settings_about),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = contentColor.copy(alpha = 0.6f),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            SettingsActionCard(
                                title = stringResource(R.string.settings_licenses),
                                subtitle = stringResource(R.string.settings_licenses_desc),
                                icon = Icons.Default.Description,
                                onClick = onNavigateToLicenses
                            )
                        }
                    }
                }

                if (showContrastDialog && pendingColor != null && contrastDialogSource != null) {
                    val isFromBg = contrastDialogSource == ColorPickerTarget.BACKGROUND
                    ContrastSuggestionDialog(
                        backgroundColor = if (isFromBg) pendingColor!! else backgroundColorState,
                        textColor = if (isFromBg) savedContentColor else pendingColor!!,
                        isBackgroundChange = isFromBg,
                        suggestions = if (isFromBg)
                            ContrastUtils.suggestTextColors(pendingColor!!)
                        else
                            ContrastUtils.suggestBackgroundColors(pendingColor!!),
                        onSuggestionSelected = { suggested ->
                            if (isFromBg) {
                                settingsViewModel.saveBackgroundColor(pendingColor!!)
                                settingsViewModel.saveContentColor(suggested)
                            } else {
                                settingsViewModel.saveContentColor(pendingColor!!)
                                settingsViewModel.saveBackgroundColor(suggested)
                            }
                            showContrastDialog = false
                            pendingColor = null
                            contrastDialogSource = null
                        },
                        onOpenColorPicker = {
                            colorBeforeContrastFlow = if (isFromBg) backgroundColorState else savedContentColor
                            openedFromContrastDialog = true
                            if (isFromBg) {
                                settingsViewModel.saveBackgroundColor(pendingColor!!)
                                colorPickerTarget = ColorPickerTarget.TEXT
                            } else {
                                settingsViewModel.saveContentColor(pendingColor!!)
                                colorPickerTarget = ColorPickerTarget.BACKGROUND
                            }
                            showContrastDialog = false
                            pendingColor = null
                        },
                        onKeepAnyway = {
                            if (isFromBg) {
                                settingsViewModel.saveBackgroundColor(pendingColor!!)
                            } else {
                                settingsViewModel.saveContentColor(pendingColor!!)
                            }
                            settingsViewModel.showWarning(backgroundColorState, savedContentColor)
                            showContrastDialog = false
                            pendingColor = null
                            contrastDialogSource = null
                        },
                        onDismiss = {
                            showContrastDialog = false
                            pendingColor = null
                            contrastDialogSource = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsColorCard(
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    val fixedCardBg = Color(0xFF1E1E1E)
    val fixedTitleColor = Color(0xFFF5F5F5)
    val fixedSubtitleColor = Color(0xFFB0B0B0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = fixedCardBg
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = fixedTitleColor
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = fixedSubtitleColor
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape)
            )
        }
    }
}

@Composable
private fun SettingsActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val fixedCardBg = Color(0xFF1E1E1E)
    val fixedTitleColor = Color(0xFFF5F5F5)
    val fixedSubtitleColor = Color(0xFFB0B0B0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = fixedCardBg
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = fixedTitleColor
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = fixedSubtitleColor
                )
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = fixedSubtitleColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
