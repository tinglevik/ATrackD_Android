package com.example.actitracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.toColorInt
import com.example.actitracker.R
import com.example.actitracker.ui.components.HueBar
import com.example.actitracker.ui.components.SaturationValuePanel

// Helper function: Compose Color → ARGB Int
private fun Color.toArgbInt(): Int = toArgb()

// Helper function: Color → HEX string
private fun colorToHex(color: Color): String {
    val argb = color.toArgbInt()
    return "#%06X".format(argb and 0xFFFFFF)
}

// Helper function: HEX string → Color?
private fun parseHexColor(hex: String): Color? {
    return try {
        val cleaned = if (hex.startsWith("#")) hex else "#$hex"
        if (cleaned.length != 7) return null
        Color(cleaned.toColorInt())
    } catch (_: Exception) {
        null
    }
}

@Composable
fun ColorPickerScreen(
    initialColor: Color,
    contrastWarning: String? = null,
    onColorConfirmed: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    val initialHsv = remember(initialColor) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(initialColor.toArgbInt(), hsv)
        hsv
    }

    var hue by remember { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember { mutableFloatStateOf(initialHsv[1]) }
    var value by remember { mutableFloatStateOf(initialHsv[2]) }

    val selectedColor by remember(hue, saturation, value) {
        mutableStateOf(Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value))))
    }

    var hexInput by remember(selectedColor) {
        mutableStateOf(colorToHex(selectedColor))
    }
    var hexError by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val dummyFocusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(0.dp)
                .focusRequester(dummyFocusRequester)
                .focusable()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDismiss) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.cancel_button),
                    tint = Color.Red,
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                text = stringResource(R.string.color_picker_title),
                fontSize = 20.sp,
                style = MaterialTheme.typography.titleMedium
            )

            IconButton(onClick = { onColorConfirmed(selectedColor) }) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = stringResource(R.string.color_picker_confirm),
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        if (contrastWarning != null) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_info_outline),
                        contentDescription = stringResource(R.string.info_desc),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = contrastWarning,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(stringResource(R.string.color_picker_current), modifier = Modifier.padding(end = 8.dp))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(initialColor)
                    .border(2.dp, Color.Gray, CircleShape)
            )

            Spacer(modifier = Modifier.width(24.dp))

            Text(stringResource(R.string.color_picker_new), modifier = Modifier.padding(end = 8.dp))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(selectedColor)
                    .border(2.dp, Color.Gray, CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SaturationValuePanel(
            hue = hue,
            saturation = saturation,
            value = value,
            onSaturationValueChanged = { s, v ->
                saturation = s
                value = v
                hexInput = colorToHex(
                    Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, s, v)))
                )
                hexError = false
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.height(16.dp))

        HueBar(
            hue = hue,
            onHueChanged = { h ->
                hue = h
                hexInput = colorToHex(
                    Color(android.graphics.Color.HSVToColor(floatArrayOf(h, saturation, value)))
                )
                hexError = false
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = hexInput,
            onValueChange = { input: String ->
                hexInput = input
                hexError = false

                val parsed = parseHexColor(input)
                if (parsed != null) {
                    val hsv = FloatArray(3)
                    android.graphics.Color.colorToHSV(parsed.toArgbInt(), hsv)
                    hue = hsv[0]
                    saturation = hsv[1]
                    value = hsv[2]
                }
            },
            label = { Text(stringResource(R.string.color_picker_hex_label)) },
            placeholder = { Text("#FF5722") },
            isError = hexError,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    // Shift focus to an invisible element
                    dummyFocusRequester.requestFocus()
                    
                    val parsed = parseHexColor(hexInput)
                    if (parsed == null) {
                        hexError = true
                    }
                }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (hexError) {
            Text(
                stringResource(R.string.color_picker_hex_error),
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }

    LaunchedEffect(Unit) {
        dummyFocusRequester.requestFocus()
    }
}
