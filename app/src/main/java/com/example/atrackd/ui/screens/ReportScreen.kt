package com.example.atrackd.ui.screens

import android.content.Context
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.atrackd.ui.theme.ATrackDTheme
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.toColor
import com.example.atrackd.R
import com.example.atrackd.ui.components.ActivityRowDimens
import com.example.atrackd.ui.components.ReportScreenDimens
import com.example.atrackd.ui.components.verticalScrollbar
import com.example.atrackd.viewmodel.ReportMode
import com.example.atrackd.viewmodel.ReportStats
import com.example.atrackd.viewmodel.ReportViewModel
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun ReportScreen(
    viewModel: ReportViewModel,
    contentColor: Color = Color.Black,
    backgroundColor: Color = Color.White
) {
    val period by viewModel.selectedPeriod.collectAsState()
    val reportStats by viewModel.statsData.collectAsState()
    val reportMode by viewModel.reportMode.collectAsState()
    val dateOffset by viewModel.dateOffset.collectAsState()

    ReportScreenContent(
        period = period,
        reportStats = reportStats,
        reportMode = reportMode,
        dateOffset = dateOffset,
        contentColor = contentColor,
        backgroundColor = backgroundColor,
        onPeriodSelected = { viewModel.selectPeriod(it) },
        onPreviousDay = { viewModel.previousDay() },
        onNextDay = { viewModel.nextDay() },
        onToggleReportMode = { viewModel.toggleReportMode() }
    )
}

@Composable
fun ReportScreenContent(
    period: ReportPeriod,
    reportStats: ReportStats,
    reportMode: ReportMode,
    dateOffset: Int,
    contentColor: Color = Color.Black,
    backgroundColor: Color = Color.White,
    onPeriodSelected: (ReportPeriod) -> Unit = {},
    onPreviousDay: () -> Unit = {},
    onNextDay: () -> Unit = {},
    onToggleReportMode: () -> Unit = {}
) {
    val sortedReport = remember(reportStats) {
        reportStats.data.filter { it.value > 0 }.toList().sortedByDescending { it.second }
    }

    val scrollState = rememberLazyListState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Period selection tabs
        Row(modifier = Modifier.fillMaxWidth()) {
            ReportPeriod.entries.forEach { p ->
                val isSelected = p == period
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPeriodSelected(p) }
                    ) {
                        Text(
                            text = p.displayName,
                            color = contentColor,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(vertical = 8.dp)
                        )
                    }
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .background(contentColor)
                        )
                    }
                }
            }
        }

        // Navigation Arrows and Date
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = ReportScreenDimens.paddingGeneral,
                    vertical = 0.dp
                )
        ) {
            if (period == ReportPeriod.TODAY) {
                val dateText = remember(dateOffset) {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, dateOffset)
                    SimpleDateFormat(
                        "d MMMM yyyy",
                        Locale.getDefault()).format(cal.time)
                }
                Text(
                    text = dateText,
                    color = contentColor,
                    modifier = Modifier.align(Alignment.CenterStart),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // Day navigation
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .padding(ReportScreenDimens.paddingGeneral)
                        .size(36.dp) // или размер иконки
                 .clickable { onPreviousDay() }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron_left_circle),
                        contentDescription = "Prev",
                        tint = contentColor
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp) // или размер иконки
                        .clickable { onNextDay() }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron_right_circle),
                        contentDescription = "Next",
                        tint = contentColor
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            ReportPieChart(reportStats, reportMode, contentColor, backgroundColor)

            // Mode toggle (Activities / Tags)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(ReportScreenDimens.paddingGeneral / 2)
                    .clickable { onToggleReportMode() },
                contentAlignment = Alignment.Center
            ) {
                if (reportMode == ReportMode.ACTIVITIES) {
                    Icon(
                        painter = painterResource(R.drawable.ic_today_outline),
                        contentDescription = "Switch to Tags",
                        tint = contentColor,
                        modifier = Modifier
                            .padding(ReportScreenDimens.paddingGeneral / 2)
                            .size(24.dp)
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_label_outline),
                        contentDescription = "Switch to Activities",
                        tint = contentColor,
                        modifier = Modifier
                            .padding(ReportScreenDimens.paddingGeneral / 2)
                            .size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(ReportScreenDimens.paddingGeneral))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .verticalScrollbar(scrollState),
            state = scrollState
        ) {
            if (sortedReport.isEmpty()) {
                item {
                    Text(
                        "No data for this period",
                        color = contentColor,
                        modifier = Modifier.padding(
                            ReportScreenDimens.paddingGeneral
                        )
                    )
                }
            } else {
                items(sortedReport) { (name, seconds) ->
                    val color = reportStats.colors[name] ?: Color.Gray

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = ReportScreenDimens.paddingGeneral,
                                vertical = 6.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Вертикальная неширокая полоска цвета
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(24.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(color)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (reportMode == ReportMode.TAGS && name != "No Tag") {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Label,
                                        contentDescription = null,
                                        tint = color,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(name, color = contentColor)
                            }
                            Text(formatTime(seconds), color = contentColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportPieChart(
    reportStats: ReportStats,
    mode: ReportMode,
    contentColor: Color = Color.Black,
    backgroundColor: Color = Color.White
) {
    val contentColorArgb = remember(contentColor) { contentColor.toArgb() }
    val contentColorArgbLighted = remember(contentColor) {
        val hsl = FloatArray(3)
        androidx.core.graphics.ColorUtils.colorToHSL(contentColor.toArgb(), hsl)
        hsl[2] = (hsl[2] + 0.7f).coerceAtMost(1f) // увеличиваем lightness
        androidx.core.graphics.ColorUtils.HSLToColor(hsl)
    }
    val backgroundColorArgb = remember(backgroundColor) { backgroundColor.toArgb() }
    val totalStr = remember(reportStats.totalSeconds) { formatTime(reportStats.totalSeconds) }

    // Thickness in DP
    val strokeWidthDp = 2f

    AndroidView(
        factory = { ctx: Context ->
            PieChart(ctx).apply {
                description.isEnabled = false
                isRotationEnabled = false
                setTouchEnabled(false)
                setUsePercentValues(true)
                legend.isEnabled = false

                isDrawHoleEnabled = true
                setHoleColor(backgroundColorArgb)

                // Border for the hole using transparent circle
                setTransparentCircleColor(contentColorArgb)
                setTransparentCircleAlpha(255)
                holeRadius = 48f
                transparentCircleRadius = 51f

                setDrawCenterText(true)
                setCenterTextColor(contentColorArgbLighted)

                // Background of the chart view will be visible through sliceSpace and extraOffsets
                setBackgroundColor(contentColorArgb)
                setExtraOffsets(strokeWidthDp, strokeWidthDp, strokeWidthDp, strokeWidthDp)
            }
        },
        update = { pieChart ->
            val filteredData = if (mode == ReportMode.TAGS) {
                reportStats.data.filter { it.key != "No Tag" && it.value > 0 }
            } else {
                reportStats.data.filter { it.value > 0 }
            }

            val entries = filteredData.map { PieEntry(it.value.toFloat(), it.key) }.toMutableList()
            val sliceColors = filteredData.map { (name, _) ->
                reportStats.colors[name]?.toArgb() ?: AndroidColor.GRAY
            }

            // Update theme colors
            pieChart.setHoleColor(backgroundColorArgb)
            pieChart.setTransparentCircleColor(contentColorArgbLighted)
            pieChart.setBackgroundColor(contentColorArgbLighted)
            pieChart.setCenterTextColor(contentColorArgb)
            pieChart.setEntryLabelTextSize(11f)
            pieChart.setEntryLabelColor(contentColorArgb)

            if (entries.isEmpty()) {
                pieChart.centerText = ""
                pieChart.data = null
            } else {
                pieChart.centerText = "Total\n$totalStr"
                pieChart.setCenterTextSize(14f)

                val dataSet = PieDataSet(entries, "").apply {
                    colors = sliceColors
                    sliceSpace = strokeWidthDp
                    valueTextColor = contentColorArgb
                    valueTextSize = 10f
                    setDrawValues(false) // Cleaner look with border
                }
                pieChart.data = PieData(dataSet)
            }

            pieChart.invalidate()
        },
        modifier = Modifier
            .padding(vertical = 16.dp)
            .size(280.dp)
            .clip(CircleShape)
    )
}

private fun formatTime(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%02d:%02d:%02d".format(h, m, s)
    else "%02d:%02d".format(m, s)
}

@Preview(showBackground = true)
@Composable
fun ReportScreenPreview() {
    ATrackDTheme {
        ReportScreenContent(
            period = ReportPeriod.TODAY,
            reportStats = SampleReportStats,
            reportMode = ReportMode.ACTIVITIES,
            dateOffset = 0
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ReportPieChartPreview() {
    ATrackDTheme {
        ReportPieChart(
            reportStats = SampleReportStats,
            mode = ReportMode.ACTIVITIES
        )
    }
}

private val SampleReportStats = ReportStats(
    data = mapOf(
        "Coding" to 3600L * 4,
        "Reading" to 3600L * 2,
        "Exercise" to 1800L,
        "Sleep" to 3600L * 8
    ),
    colors = mapOf(
        "Coding" to Color(0xFF2196F3),
        "Reading" to Color(0xFFFF9800),
        "Exercise" to Color(0xFF4CAF50),
        "Sleep" to Color(0xFF9C27B0)
    ),
    totalSeconds = (3600L * 4) + (3600L * 2) + 1800L + (3600L * 8)
)
