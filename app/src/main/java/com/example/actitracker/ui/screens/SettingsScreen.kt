package com.example.actitracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.actitracker.R
import com.example.actitracker.ui.components.ContrastUtils
import androidx.compose.ui.tooling.preview.Preview
import com.example.actitracker.ui.theme.ActitrackerTheme
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

    SettingsScreenContent(
        backgroundColorState = backgroundColorState,
        savedContentColor = savedContentColor,
        onBackgroundColorChange = { settingsViewModel.saveBackgroundColor(it) },
        onContentColorChange = { settingsViewModel.saveContentColor(it) },
        onShowWarning = { bg, txt -> settingsViewModel.showWarning(bg, txt) },
        onNavigateToLicenses = onNavigateToLicenses,
        contentColor = contentColor
    )
}

@Composable
fun SettingsScreenContent(
    backgroundColorState: Color,
    savedContentColor: Color,
    onBackgroundColorChange: (Color) -> Unit,
    onContentColorChange: (Color) -> Unit,
    onShowWarning: (Color, Color) -> Unit,
    onNavigateToLicenses: () -> Unit,
    contentColor: Color = Color.Black
) {
    var colorPickerTarget by remember { mutableStateOf<ColorPickerTarget?>(null) }
    var showContrastDialog by remember { mutableStateOf(false) }
    var pendingColor by remember { mutableStateOf<Color?>(null) }
    var contrastDialogSource by remember { mutableStateOf<ColorPickerTarget?>(null) }
    var openedFromContrastDialog by remember { mutableStateOf(false) }
    var colorBeforeContrastFlow by remember { mutableStateOf<Color?>(null) }

    val onColorConfirmedInternal: (Color) -> Unit = { color ->
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
                    onBackgroundColorChange(color)
                }
            }
            ColorPickerTarget.TEXT -> {
                if (!ContrastUtils.isReadable(color, backgroundColorState)) {
                    pendingColor = color
                    contrastDialogSource = ColorPickerTarget.TEXT
                    showContrastDialog = true
                } else {
                    onContentColorChange(color)
                }
            }
            null -> {}
        }
    }

    val onDismissInternal: () -> Unit = {
        if (openedFromContrastDialog && colorBeforeContrastFlow != null) {
            when (contrastDialogSource) {
                ColorPickerTarget.BACKGROUND ->
                    onBackgroundColorChange(colorBeforeContrastFlow!!)
                ColorPickerTarget.TEXT ->
                    onContentColorChange(colorBeforeContrastFlow!!)
                null -> {}
            }
        }
        colorPickerTarget = null
        openedFromContrastDialog = false
        colorBeforeContrastFlow = null
        contrastDialogSource = null
    }

    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = backgroundColorState,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->

        LaunchedEffect(snackbarMessage) {
            snackbarMessage?.let {
                snackbarHostState.showSnackbar(it)
                snackbarMessage = null
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                colorPickerTarget != null && !openedFromContrastDialog -> {
                    val isBackground = colorPickerTarget == ColorPickerTarget.BACKGROUND
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        ColorPickerScreen(
                            modifier = Modifier
                                .fillMaxSize()
//                                .statusBarsPadding()
                                .navigationBarsPadding()
                                .imePadding(),
                            initialColor = if (isBackground) backgroundColorState else savedContentColor,
                            contrastWarning = if (isBackground)
                                stringResource(R.string.contrast_warning_background)
                            else
                                stringResource(R.string.contrast_warning_text),
                            onColorConfirmed = onColorConfirmedInternal,
                            onDismiss = onDismissInternal,
                            backgroundColor = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
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

            if (colorPickerTarget != null && openedFromContrastDialog) {
                val isBackground = colorPickerTarget == ColorPickerTarget.BACKGROUND
                Dialog(
                    onDismissRequest = onDismissInternal,
                    properties = DialogProperties(
                        usePlatformDefaultWidth = false,
                        decorFitsSystemWindows = false
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        ColorPickerScreen(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .clip(RoundedCornerShape(16.dp))
                                .wrapContentHeight(),
                            initialColor = if (isBackground) backgroundColorState else savedContentColor,
                            contrastWarning = if (isBackground)
                                stringResource(R.string.contrast_warning_background)
                            else
                                stringResource(R.string.contrast_warning_text),
                            onColorConfirmed = onColorConfirmedInternal,
                            onDismiss = onDismissInternal,
                            backgroundColor = backgroundColorState,
                            contentColor = savedContentColor
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
                            onBackgroundColorChange(pendingColor!!)
                            onContentColorChange(suggested)
                        } else {
                            onContentColorChange(pendingColor!!)
                            onBackgroundColorChange(suggested)
                        }
                        showContrastDialog = false
                        pendingColor = null
                        contrastDialogSource = null
                    },
                    onOpenColorPicker = {
                        colorBeforeContrastFlow = if (isFromBg) backgroundColorState else savedContentColor
                        openedFromContrastDialog = true
                        if (isFromBg) {
                            onBackgroundColorChange(pendingColor!!)
                            colorPickerTarget = ColorPickerTarget.TEXT
                        } else {
                            onContentColorChange(pendingColor!!)
                            colorPickerTarget = ColorPickerTarget.BACKGROUND
                        }
                        showContrastDialog = false
                        pendingColor = null
                    },
                    onKeepAnyway = {
                        if (isFromBg) {
                            onBackgroundColorChange(pendingColor!!)
                        } else {
                            onContentColorChange(pendingColor!!)
                        }
                        onShowWarning(backgroundColorState, savedContentColor)
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
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = fixedTitleColor
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = fixedSubtitleColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

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
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = fixedTitleColor
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = fixedSubtitleColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = fixedSubtitleColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    ActitrackerTheme {
        SettingsScreenContent(
            backgroundColorState = Color.White,
            savedContentColor = Color.Black,
            onBackgroundColorChange = {},
            onContentColorChange = {},
            onShowWarning = { _, _ -> },
            onNavigateToLicenses = {},
            contentColor = Color.Black
        )
    }
}
