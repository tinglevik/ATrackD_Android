package com.example.atrackd.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(
    onBack: () -> Unit,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Open Source Licenses", color = contentColor) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = contentColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        },
        containerColor = backgroundColor
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            LicenseItem(
                name = "Material Icons (Google)",
                license = "Apache License 2.0",
                contentColor = contentColor
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Apache License\nVersion 2.0, January 2004\nhttp://www.apache.org/licenses/",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = contentColor.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = """
                TERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION

                1. Definitions.
                "License" shall mean the terms and conditions for use, reproduction, and distribution as defined by Sections 1 through 9 of this document.
                "Licensor" shall mean the copyright owner or entity authorized by the copyright owner that is granting the License.
                ...
                
                (Full text of Apache 2.0 License shortened for brevity in this UI, 
                but available at http://www.apache.org/licenses/LICENSE-2.0)
                
                Copyright 2024 Google LLC
                
                Licensed under the Apache License, Version 2.0 (the "License");
                you may not use this file except in compliance with the License.
                You may obtain a copy of the License at
                
                    http://www.apache.org/licenses/LICENSE-2.0
                
                Unless required by applicable law or agreed to in writing, software
                distributed under the License is distributed on an "AS IS" BASIS,
                WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
                See the License for the specific language governing permissions and
                limitations under the License.
                """.trimIndent(),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = contentColor.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun LicenseItem(name: String, license: String, contentColor: Color) {
    Column {
        Text(
            text = name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
        Text(
            text = license,
            fontSize = 14.sp,
            color = contentColor.copy(alpha = 0.7f)
        )
    }
}
