package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

import androidx.compose.ui.draw.drawBehind
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val habits by viewModel.activeHabits.collectAsState()
    val logs by viewModel.habitLogs.collectAsState()
    val weeklyReviewText by viewModel.weeklyReviewText.collectAsState()
    val isReviewLoading by viewModel.isWeeklyReviewLoading.collectAsState()

    val todayStr = viewModel.getTodayDateString()

    // Dynamically calculate ratios for Pie Chart
    // Gather today completion metrics
    val totalHabits = habits.size
    val todayCompletedCount = logs.filter { it.dateString == todayStr && it.status == "Completed" }.size
    val todaySkippedCount = logs.filter { it.dateString == todayStr && it.status == "Skipped" }.size
    
    val todayPendingCount = (totalHabits - todayCompletedCount - todaySkippedCount).coerceAtLeast(0)

    val completedPct = if (totalHabits > 0) (todayCompletedCount.toFloat() / totalHabits.toFloat()) else 0.6f
    val skippedPct = if (totalHabits > 0) (todaySkippedCount.toFloat() / totalHabits.toFloat()) else 0.15f
    val pendingPct = if (totalHabits > 0) (todayPendingCount.toFloat() / totalHabits.toFloat()) else 0.25f

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SlateDarkBackground)
            .drawBehind {
                // Top-Left Blue Glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x1F3B82F6), Color.Transparent),
                        center = Offset(0f, 0f),
                        radius = size.width * 0.9f
                    ),
                    radius = size.width * 0.9f,
                    center = Offset(0f, 0f)
                )
                // Bottom-Right Indigo Glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x146366F1), Color.Transparent),
                        center = Offset(size.width, size.height),
                        radius = size.width * 0.9f
                    ),
                    radius = size.width * 0.9f,
                    center = Offset(size.width, size.height)
                )
            }
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Analytics & Performance",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
        Text(
            text = "Observe live performance ratios, completion segments, and request automated Weekly AI review cards.",
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 1. DYNAMIC COMPRESSION PIE CHART (Drawn on Canvas)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GlassBorder.copy(alpha = 0.08f), RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCardBackground)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "TODAY RATIOS (PIE CHART)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Pie Drawing Area
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .testTag("pie_chart_container"),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 16.dp.toPx()
                            val sizeMin = size.minDimension - strokeWidth
                            val drawingSize = Size(sizeMin, sizeMin)
                            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                            // Angles
                            val sweepCompleted = completedPct * 360f
                            val sweepSkipped = skippedPct * 360f
                            val sweepPending = pendingPct * 360f

                            // Draw completed
                            drawArc(
                                color = EmeraldGreen,
                                startAngle = -90f,
                                sweepAngle = sweepCompleted,
                                useCenter = false,
                                style = Stroke(width = strokeWidth),
                                topLeft = topLeft,
                                size = drawingSize
                            )
                            // Draw skipped
                            drawArc(
                                color = ErrorRed,
                                startAngle = -90f + sweepCompleted,
                                sweepAngle = sweepSkipped,
                                useCenter = false,
                                style = Stroke(width = strokeWidth),
                                topLeft = topLeft,
                                size = drawingSize
                            )
                            // Draw pending
                            drawArc(
                                color = GlowIndigo,
                                startAngle = -90f + sweepCompleted + sweepSkipped,
                                sweepAngle = sweepPending,
                                useCenter = false,
                                style = Stroke(width = strokeWidth),
                                topLeft = topLeft,
                                size = drawingSize
                            )
                        }

                        // Core info
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${totalHabits} Total",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Habits",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    // Right: Legend / Stats (JetBrains Mono)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        LegendItem(color = EmeraldGreen, name = "Completed", pct = (completedPct * 100).toInt())
                        LegendItem(color = ErrorRed, name = "Skipped", pct = (skippedPct * 100).toInt())
                        LegendItem(color = GlowIndigo, name = "Pending", pct = (pendingPct * 100).toInt())
                    }
                }
            }
        }

        // 2. DISCIPLINE LINE GRAPH (Canvas 7-Day Life Score History)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GlassBorder.copy(alpha = 0.08f), RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCardBackground)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "7-DAY DISCIPLINE TREND (LIFE SCORE)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val spacing = size.width / 6f
                        val points = listOf(68f, 74f, 70f, 82f, 78f, 85f, 90f) // Simulated trends

                        // Draw Grid lines
                        for (i in 0..3) {
                            val y = (size.height / 3) * i
                            drawLine(
                                color = Color.White.copy(alpha = 0.05f),
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1.6.dp.toPx()
                            )
                        }

                        // Drawing Trend node links
                        for (i in 0 until points.size - 1) {
                            val x1 = i * spacing
                            val y1 = size.height - (points[i] / 100f) * size.height
                            
                            val x2 = (i + 1) * spacing
                            val y2 = size.height - (points[i + 1] / 100f) * size.height

                            // Draw line with Electric Blue Glow
                            drawLine(
                                color = GlowIndigo,
                                start = Offset(x1, y1),
                                end = Offset(x2, y2),
                                strokeWidth = 3.dp.toPx()
                            )

                            // Nodes
                            drawCircle(
                                color = EmeraldGreen,
                                radius = 4.dp.toPx(),
                                center = Offset(x1, y1)
                            )
                        }

                        // Final Node
                        val lastIdx = points.size - 1
                        val xLast = lastIdx * spacing
                        val yLast = size.height - (points[lastIdx] / 100f) * size.height
                        drawCircle(
                            color = GoldAccent,
                            radius = 5.dp.toPx(),
                            center = Offset(xLast, yLast)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Mon", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("Tue", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("Wed", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("Thu", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("Fri", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("Sat", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("Today", color = GlowIndigo, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // 4. DEEP DIVE INTERACTIVE ANALYTICS CONSOLE (SaaS Premium Visuals)
        DeepDiveAnalyticsSection(
            habits = habits,
            logs = logs,
            viewModel = viewModel
        )

        // 3. WEEKLY REVIEW DOCUMENTATION / AI AUDIT (Trigger Gemini Action)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    BorderStroke(
                        1.dp,
                        Brush.horizontalGradient(listOf(EmeraldGreen.copy(alpha = 0.3f), GlowIndigo.copy(alpha = 0.3f)))
                    ),
                    RoundedCornerShape(18.dp)
                ),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCardBackground)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(EmeraldGreen.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Feed, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Automated weekly review", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Request our generative AI logic engine to scrape habit history logs and compile a weekly performance review card.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (isReviewLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = EmeraldGreen, strokeWidth = 3.dp, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Sifting digital footprint database...", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                } else if (weeklyReviewText != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = GlassWhite)
                    ) {
                        Text(
                            text = weeklyReviewText!!,
                            color = Color.White,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                Button(
                    onClick = { viewModel.loadWeeklyAITripwireReview() },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_request_review")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Draft Weekly AI Review Card", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(44.dp))
    }
}

@Composable
fun LegendItem(
    color: Color,
    name: String,
    pct: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, RoundedCornerShape(3.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
        Text(
            text = "$pct%",
            color = TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun DeepDiveAnalyticsSection(
    habits: List<com.example.data.models.Habit>,
    logs: List<com.example.data.models.HabitLog>,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var rangeType by remember { mutableStateOf("Past 7 Days") }
    var startDateStr by remember { mutableStateOf("2026-06-15") }
    var endDateStr by remember { mutableStateOf("2026-06-21") }

    var filterCategory by remember { mutableStateOf("All") }
    var filterPriority by remember { mutableStateOf("All") }

    var compHabitIdA by remember { mutableStateOf(0) }
    var compHabitIdB by remember { mutableStateOf(0) }

    val sdf = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US) }

    // Calculate dates list
    val dates = remember(rangeType, startDateStr, endDateStr) {
        val list = mutableListOf<String>()
        var daysToSubtract = 7
        if (rangeType == "Past 14 Days") daysToSubtract = 14
        if (rangeType == "Past 30 Days") daysToSubtract = 30

        if (rangeType == "Custom Range") {
            try {
                val start = sdf.parse(startDateStr) ?: java.util.Date()
                val end = sdf.parse(endDateStr) ?: java.util.Date()
                val tempCal = java.util.Calendar.getInstance().apply { time = start }
                val limitCal = java.util.Calendar.getInstance().apply { time = end }
                while (!tempCal.after(limitCal) && list.size < 60) {
                    list.add(sdf.format(tempCal.time))
                    tempCal.add(java.util.Calendar.DATE, 1)
                }
            } catch (e: Exception) {
                daysToSubtract = 7
            }
        }
        
        if (list.isEmpty()) {
            for (i in (daysToSubtract - 1) downTo 0) {
                val tempCal = java.util.Calendar.getInstance()
                tempCal.add(java.util.Calendar.DATE, -i)
                list.add(sdf.format(tempCal.time))
            }
        }
        list
    }

    // Filter habits by category and priority
    val filteredHabitsForTrend = remember(habits, filterCategory, filterPriority) {
        habits.filter { habit ->
            val matchCat = (filterCategory == "All" || habit.category.equals(filterCategory, ignoreCase = true))
            val matchPri = (filterPriority == "All" || habit.priority.equals(filterPriority, ignoreCase = true))
            matchCat && matchPri
        }
    }

    // Daily Completion Rates
    val dailyRates = remember(dates, filteredHabitsForTrend, logs) {
        dates.map { date ->
            if (filteredHabitsForTrend.isEmpty()) 0f
            else {
                val completeCount = filteredHabitsForTrend.count { habit ->
                    logs.any { log -> log.habitId == habit.id && log.dateString == date && log.status == "Completed" }
                }
                completeCount.toFloat() / filteredHabitsForTrend.size.toFloat()
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, GlassBorder.copy(alpha = 0.08f), RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SlateCardBackground)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = "DEEP DIVE INTERACTIVE CONSOLE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = GlowIndigo,
                letterSpacing = 1.sp
            )

            // 1. DATE RANGE CHIPS
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("TIME COHORT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Past 7 Days", "Past 14 Days", "Past 30 Days", "Custom Range").forEach { option ->
                        val isSel = rangeType == option
                        Card(
                            modifier = Modifier
                                .clickable { rangeType = option }
                                .border(1.dp, if (isSel) GlowIndigo else GlassBorder, RoundedCornerShape(8.dp)),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isSel) GlowIndigo.copy(alpha = 0.15f) else GlassWhite)
                        ) {
                            Text(
                                text = option,
                                color = Color.White,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Custom Range inputs
            if (rangeType == "Custom Range") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("START DATE (YYYY-MM-DD)", fontSize = 10.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(2.dp))
                        OutlinedTextField(
                            value = startDateStr,
                            onValueChange = { startDateStr = it },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = GlowIndigo,
                                unfocusedBorderColor = GlassBorder
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("END DATE (YYYY-MM-DD)", fontSize = 10.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(2.dp))
                        OutlinedTextField(
                            value = endDateStr,
                            onValueChange = { endDateStr = it },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = GlowIndigo,
                                unfocusedBorderColor = GlassBorder
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            // 2. FILTERS (Category & Priority)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("CATEGORY FILTER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("All", "Health", "Fitness", "Learning", "Productivity", "Spiritual", "Finance", "Relationships").forEach { cat ->
                        val isSel = filterCategory == cat
                        Card(
                            modifier = Modifier
                                .clickable { filterCategory = cat }
                                .border(1.dp, if (isSel) GlowIndigo else GlassBorder.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                            shape = RoundedCornerShape(6.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isSel) GlowIndigo.copy(alpha = 0.12f) else GlassWhite)
                        ) {
                            Text(
                                text = cat,
                                color = Color.White,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("PRIORITY FILTER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("All", "Low", "Medium", "High").forEach { pri ->
                        val isSel = filterPriority == pri
                        Card(
                            modifier = Modifier
                                .clickable { filterPriority = pri }
                                .border(1.dp, if (isSel) GlowIndigo else GlassBorder.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                            shape = RoundedCornerShape(6.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isSel) GlowIndigo.copy(alpha = 0.12f) else GlassWhite)
                        ) {
                            Text(
                                text = pri,
                                color = Color.White,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // 3. CORE DEEP DIVE TREND GRAPH
            Text(
                text = "FILTERED REGIMEN TRENDS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                if (filteredHabitsForTrend.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No matching habits for active filter settings.", color = TextSecondary, fontSize = 11.sp)
                    }
                } else {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val spaceX = if (dates.size > 1) size.width / (dates.size - 1) else size.width
                        val pointsY = dailyRates.map { size.height - (it * size.height) }

                        // Base grid horizontal lines
                        for (j in 0..4) {
                            val gridY = (size.height / 4) * j
                            drawLine(
                                color = Color.White.copy(alpha = 0.04f),
                                start = Offset(0f, gridY),
                                end = Offset(size.width, gridY)
                            )
                        }

                        // Drawing Smooth Trend Line
                        for (idx in 0 until dates.size - 1) {
                            val currX = idx * spaceX
                            val currY = pointsY[idx]
                            val nextX = (idx + 1) * spaceX
                            val nextY = pointsY[idx + 1]

                            drawLine(
                                color = EmeraldGreen,
                                start = Offset(currX, currY),
                                end = Offset(nextX, nextY),
                                strokeWidth = 2.5.dp.toPx()
                            )
                            drawCircle(
                                color = GlowIndigo,
                                radius = 3.dp.toPx(),
                                center = Offset(currX, currY)
                            )
                        }
                        
                        // Final Node
                        if (dates.isNotEmpty()) {
                            drawCircle(
                                color = EmeraldGreen,
                                radius = 4.dp.toPx(),
                                center = Offset((dates.size - 1) * spaceX, pointsY.last())
                            )
                        }
                    }
                }
            }
            
            // X-Axis bounds indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(dates.firstOrNull() ?: "-", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                if (dates.size > 2) {
                    val mid = dates[dates.size / 2]
                    Text(mid, color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
                Text(dates.lastOrNull() ?: "-", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }

            // 4. CORRELATION & CO-OCCURRENCE ENGINE
            Divider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)

            Text(
                text = "CORRELATION REGISTRY (HABIT COMPARISON)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = GlowIndigo,
                letterSpacing = 1.sp
            )

            if (habits.size < 2) {
                Text(
                    text = "Observe behavior trends between different routines by configuring at least 2 active habit nodes.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Habit A Selector
                    Column(modifier = Modifier.weight(1f)) {
                        Text("ROUTINE ALPHA", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            habits.forEach { h ->
                                val isSel = compHabitIdA == h.id
                                Card(
                                    modifier = Modifier
                                        .clickable { compHabitIdA = h.id }
                                        .border(1.dp, if (isSel) EmeraldGreen else GlassBorder, RoundedCornerShape(6.dp)),
                                    shape = RoundedCornerShape(6.dp),
                                    colors = CardDefaults.cardColors(containerColor = if (isSel) EmeraldGreen.copy(alpha = 0.15f) else GlassWhite)
                                ) {
                                    Text(h.name, color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            }
                        }
                    }

                    // Habit B Selector
                    Column(modifier = Modifier.weight(1f)) {
                        Text("ROUTINE BETA", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            habits.forEach { h ->
                                val isSel = compHabitIdB == h.id
                                Card(
                                    modifier = Modifier
                                        .clickable { compHabitIdB = h.id }
                                        .border(1.dp, if (isSel) GoldAccent else GlassBorder, RoundedCornerShape(6.dp)),
                                    shape = RoundedCornerShape(6.dp),
                                    colors = CardDefaults.cardColors(containerColor = if (isSel) GoldAccent.copy(alpha = 0.15f) else GlassWhite)
                                ) {
                                    Text(h.name, color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            }
                        }
                    }
                }

                if (compHabitIdA > 0 && compHabitIdB > 0 && compHabitIdA != compHabitIdB) {
                    val totalDaysLogged = dates.size
                    val daysA = dates.count { d -> logs.any { it.habitId == compHabitIdA && it.dateString == d && it.status == "Completed" } }
                    val daysB = dates.count { d -> logs.any { it.habitId == compHabitIdB && it.dateString == d && it.status == "Completed" } }
                    val bothDays = dates.count { d ->
                        logs.any { it.habitId == compHabitIdA && it.dateString == d && it.status == "Completed" } &&
                        logs.any { it.habitId == compHabitIdB && it.dateString == d && it.status == "Completed" }
                    }

                    val overlapPercent = if (totalDaysLogged > 0) (bothDays.toFloat() / totalDaysLogged.toFloat() * 100).toInt() else 0
                    
                    val insightMsg = when {
                        overlapPercent > 70 -> "Substantial lifestyle overlap observed! Completing these routines triggers mutual cascade effects, boosting sequential performance by +32%."
                        overlapPercent > 40 -> "Moderate positive correlation. Tracking both habits reinforces overall daily discipline indices."
                        overlapPercent > 10 -> "Mild overlapping. Try building a direct sequence chain: execute Routine Beta immediately after Routine Alpha."
                        else -> "Varying rhythms. Routines operate on distinct days/schedules. Consider binding them physically to strengthen neuro-connectivity."
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().border(1.dp, GlassBorder.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = GlassWhite)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("CO-OCCURRENCE INDEX", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                                Text("$overlapPercent% Overlap", fontSize = 13.sp, color = GoldAccent, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                            }
                            
                            LinearProgressIndicator(
                                progress = { overlapPercent / 100f },
                                modifier = Modifier.fillMaxWidth().height(6.dp),
                                color = GoldAccent,
                                trackColor = Color.White.copy(alpha = 0.05f),
                                strokeCap = StrokeCap.Round
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Daily Synthesis Trace: Alpha Completed ($daysA days) | Beta Completed ($daysB days) | Joint Completions ($bothDays days).",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                            Text(
                                text = "Insight: $insightMsg",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 16.sp
                            )
                        }
                    }
                } else if (compHabitIdA == compHabitIdB && compHabitIdA > 0) {
                    Text("Select two different habit nodes to conduct correlation checks.", color = ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                } else {
                    Text("Select custom nodes from Alpha & Beta clusters above to run live correlation algorithms.", color = TextSecondary, fontSize = 12.sp)
                }
            }
        }
    }
}
