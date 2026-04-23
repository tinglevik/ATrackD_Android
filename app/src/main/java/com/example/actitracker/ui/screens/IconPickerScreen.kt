package com.example.actitracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.actitracker.R
import com.example.actitracker.ui.components.IconMapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconPickerScreen(
    initialIconName: String,
    onIconSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val dummyFocusRequester = remember { FocusRequester() }
    
    val allIcons = remember { IconMapper.getAllIcons() }
    
    val filteredIcons = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            allIcons
        } else {
            allIcons.filter { iconInfo ->
                iconInfo.name.contains(searchQuery, ignoreCase = true) ||
                iconInfo.category.contains(searchQuery, ignoreCase = true) ||
                iconInfo.tags.any { it.contains(searchQuery, ignoreCase = true) }
            }
        }
    }

    val groupedIcons = remember(filteredIcons) {
        filteredIcons.groupBy { it.category }
    }

    // Force focus onto a dummy element immediately to prevent keyboard from popping up
    LaunchedEffect(Unit) {
        dummyFocusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .clickable(
                interactionSource = null,
                indication = null
            ) { focusManager.clearFocus() }
    ) {
        // Dummy element to hold initial focus
        Box(
            modifier = Modifier
                .size(0.dp)
                .focusRequester(dummyFocusRequester)
                .focusable()
        )

        TopAppBar(
            title = { Text(stringResource(R.string.select_icon_title), color = contentColor) },
            navigationIcon = {
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button),
                        tint = contentColor
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
        )

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text(stringResource(R.string.search_icons_hint), color = contentColor.copy(alpha = 0.5f)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = contentColor) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.clear_search), tint = contentColor)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = contentColor,
                unfocusedTextColor = contentColor,
                focusedBorderColor = contentColor,
                unfocusedBorderColor = contentColor.copy(alpha = 0.3f),
                cursorColor = contentColor
            )
        )

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 44.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            groupedIcons.forEach { (category, iconsInGroup) ->
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = stringResource(IconMapper.getCategoryRes(category)),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp, start = 4.dp)
                    )
                }

                items(iconsInGroup) { iconInfo ->
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(if (iconInfo.name == initialIconName) contentColor.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable { onIconSelected(iconInfo.name) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconInfo.icon,
                            contentDescription = iconInfo.name,
                            tint = contentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
