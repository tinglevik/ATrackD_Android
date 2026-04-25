package com.example.actitracker.viewmodel

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.example.actitracker.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.actitracker.data.SettingsDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _backgroundColor = MutableStateFlow(Color(SettingsDataStore.DEFAULT_COLOR_ARGB))
    val backgroundColor: StateFlow<Color> = _backgroundColor

    private val _contentColor = MutableStateFlow(Color.Black)
    val contentColor: StateFlow<Color> = _contentColor

    private val _showWarningDrawer = MutableStateFlow(false)
    val showWarningDrawer: StateFlow<Boolean> = _showWarningDrawer

    private val _previousBg = MutableStateFlow(Color(SettingsDataStore.DEFAULT_COLOR_ARGB))

    private val _previousText = MutableStateFlow(Color.Black)

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage

    init {
        viewModelScope.launch {
            settingsDataStore.backgroundColorFlow.collect { argb ->
                _backgroundColor.value = Color(argb)
            }
        }
        viewModelScope.launch {
            settingsDataStore.contentColorFlow.collect { argb ->
                _contentColor.value = Color(argb)
            }
        }
    }

    fun saveBackgroundColor(color: Color) {
        viewModelScope.launch {
            _backgroundColor.value = color
            settingsDataStore.saveBackgroundColor(color.toArgbInt())
        }
    }

    fun saveContentColor(color: Color) {
        viewModelScope.launch {
            _contentColor.value = color
            settingsDataStore.saveContentColor(color.toArgbInt())
        }
    }

    fun showWarning(previousBg: Color, previousText: Color) {
        _previousBg.value = previousBg
        _previousText.value = previousText
        _showWarningDrawer.value = true
    }

    fun hideWarning() {
        _showWarningDrawer.value = false
    }

    fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun revertChanges(context: Context) {
        saveBackgroundColor(_previousBg.value)
        saveContentColor(_previousText.value)
        hideWarning()
        showSnackbar(context.getString(R.string.settings_reverted))
    }

    fun keepChanges(context: Context) {
        hideWarning()
        showSnackbar(context.getString(R.string.settings_saved))
    }

    private fun Color.toArgbInt(): Int {
        return android.graphics.Color.argb(
            (alpha * 255).toInt(),
            (red * 255).toInt(),
            (green * 255).toInt(),
            (blue * 255).toInt()
        )
    }
}