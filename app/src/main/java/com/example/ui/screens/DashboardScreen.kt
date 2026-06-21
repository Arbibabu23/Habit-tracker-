package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Habit
import com.example.ui.theme.*
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val user by viewModel.currentUser.collectAsState()
    val habits by viewModel.activeHabits.collectAsState()
    val logs by viewModel.habitLogs.collectAsState()
    val insights by viewModel.smartInsights.collectAsState()
    val isInsightsLoading by viewModel.isInsightsLoading.collectAsState()

    val todayStr = viewModel.getTodayDateString()
    val todayCompletions = logs.filter { it.dateString == todayStr && it.status == "Completed" }

    val dueTodayHabits = remember(habits, todayStr) {
        habits.filter { habit ->
            if (habit.frequency == "Specific Days") {
                if (habit.customDaysOfWeek.isBlank()) true
                else {
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                    val date = try { sdf.parse(todayStr) } catch(e: Exception) { java.util.Date() }
                    val cal = java.util.Calendar.getInstance().apply { time = date }
                    val dayOfWeekInt = cal.get(java.util.Calendar.DAY_OF_WEEK)
                    val dayStr = when(dayOfWeekInt) {
                        java.util.Calendar.MONDAY -> "Mon"
                        java.util.Calendar.TUESDAY -> "Tue"
                        java.util.Calendar.WEDNESDAY -> "Wed"
                        java.util.Calendar.THURSDAY -> "Thu"
                        java.util.Calendar.FRIDAY -> "Fri"
                        java.util.Calendar.SATURDAY -> "Sat"
                        java.util.Calendar.SUNDAY -> "Sun"
                        else -> ""
                    }
                    habit.customDaysOfWeek.contains(dayStr, ignoreCase = true)
                }
            } else if (habit.frequency == "Custom Interval") {
                if (habit.customIntervalDays <= 0) true
                else {
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                    val todayDate = try { sdf.parse(todayStr) } catch(e: Exception) { java.util.Date() }
                    val createdDate = java.util.Date(habit.createdAt)
                    val diffMs = todayDate.time - createdDate.time
                    val diffDays = (diffMs / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
                    diffDays % habit.customIntervalDays == 0
                }
            } else {
                true
            }
        }
    }

    val completionRate = if (dueTodayHabits.isNotEmpty()) {
        todayCompletions.size.toFloat() / dueTodayHabits.size.toFloat()
    } else {
        0f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = completionRate,
        animationSpec = tween(durationMillis = 800)
    )

    // Greeting string based on Hour of the Day
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 0..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }

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
    ) {
        // Upper Header Block (Floating Glass Card)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$greeting,",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = user?.fullName ?: "Disciplined Achiever",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                // Level badge details (XP indicator)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(GlowIndigo.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                            .border(1.dp, GlowIndigo.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "LVL ${user?.level ?: 1}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Flame Streak Node
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFF5722).copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xFFFF5722).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color(0xFFFB8C00), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "${user?.currentStreak ?: 0} DYS",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // PROGRESS CARD PANEL (Tactile / Glassmorphic)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorder, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GlassWhite)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TODAY ROUTINE PROGRESS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = GlowIndigo,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${todayCompletions.size} of ${dueTodayHabits.size} Completed",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (dueTodayHabits.isEmpty()) "No scheduled habits due today" else "${((completionRate) * 100).toInt()}% completed for today cycle.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Life Score Indicator
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(EmeraldGreen, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Life Score Level: ",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = String.format(Locale.US, "%.1f", user?.lifeScore ?: 80.0),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen
                            )
                        }
                    }

                    // Progress Ring drawn on Canvas
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Track
                            drawCircle(
                                color = Color.White.copy(alpha = 0.08f),
                                radius = size.minDimension / 2,
                                style = Stroke(width = 8.dp.toPx())
                            )
                            // Filled Sweep
                            drawArc(
                                color = GlowIndigo,
                                startAngle = -90f,
                                sweepAngle = animatedProgress * 360f,
                                useCenter = false,
                                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        Text(
                            text = "${(animatedProgress * 100).toInt()}%",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // GEMINI AI COACH RECOMMENDATION WIDGET
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(
                            1.dp,
                            Brush.horizontalGradient(listOf(GlowIndigo.copy(alpha = 0.4f), CustomPurple.copy(alpha = 0.4f)))
                        ),
                        RoundedCornerShape(20.dp)
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SlateCardBackground)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(GlowIndigo.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = GlowIndigo, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Gemini AI Coach Insights",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        IconButton(
                            onClick = { viewModel.triggerRefreshSmartInsights() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            if (isInsightsLoading) {
                                CircularProgressIndicator(color = GlowIndigo, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TextSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    insights.forEachIndexed { idx, insight ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("•", color = GlowIndigo, fontSize = 16.sp, modifier = Modifier.padding(end = 8.dp))
                            Text(
                                text = insight,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // TODAY ROUTINE HABITS SECTOR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TODAY'S HABIT NODES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "View All",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlowIndigo,
                    modifier = Modifier.clickable { viewModel.navigateTo(MainViewModel.Screen.Habits) }
                )
            }

            if (dueTodayHabits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.LibraryAdd, contentDescription = null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No active schedules today.",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Congratulations! All schedules clear, or configure more nodes.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                dueTodayHabits.forEach { habit ->
                    HabitListItem(
                        habit = habit,
                        viewModel = viewModel,
                        isCompleted = todayCompletions.any { it.habitId == habit.id },
                        allHabits = habits,
                        todayCompletions = todayCompletions
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun HabitListItem(
    habit: Habit,
    viewModel: MainViewModel,
    isCompleted: Boolean,
    allHabits: List<Habit> = emptyList(),
    todayCompletions: List<com.example.data.models.HabitLog> = emptyList(),
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var entryNote by remember { mutableStateOf("") }

    val isLocked = remember(habit, todayCompletions) {
        habit.dependsOnHabitId > 0 && todayCompletions.none { it.habitId == habit.dependsOnHabitId }
    }

    val parentName = remember(habit, allHabits) {
        if (habit.dependsOnHabitId > 0) {
            allHabits.find { it.id == habit.dependsOnHabitId }?.name
        } else null
    }
    
    val categoryIcon = when (habit.iconName) {
        "water_drop" -> Icons.Default.WaterDrop
        "fitness_center" -> Icons.Default.FitnessCenter
        "school" -> Icons.Default.School
        "menu_book" -> Icons.Default.MenuBook
        "favorite" -> Icons.Default.Favorite
        "spa" -> Icons.Default.Spa
        "payments" -> Icons.Default.Payments
        else -> Icons.Default.Bolt
    }

    val themeColor = remember {
        try {
            Color(android.graphics.Color.parseColor(habit.colorHex))
        } catch (e: Exception) {
            GlowIndigo
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, GlassBorder.copy(alpha = if (isLocked) 0.04f else 0.1f), RoundedCornerShape(16.dp))
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) themeColor.copy(alpha = 0.05f) else SlateCardBackground
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .then(if (isLocked) Modifier.alpha(0.5f) else Modifier)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Category Icon Node with dynamic border
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(themeColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .border(1.dp, themeColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(categoryIcon, contentDescription = null, tint = themeColor, modifier = Modifier.size(20.dp))
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = habit.name,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            style = if (isCompleted) LocalTextStyle.current.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough) else LocalTextStyle.current
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = habit.category,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Box(
                                modifier = Modifier
                                    .size(3.dp)
                                    .background(TextSecondary, CircleShape)
                            )
                            Text(
                                text = habit.priority,
                                color = when (habit.priority) {
                                    "High" -> ErrorRed
                                    "Medium" -> GoldAccent
                                    else -> TextSecondary
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )

                            if (isLocked && parentName != null) {
                                Box(
                                    modifier = Modifier
                                        .size(3.dp)
                                        .background(TextSecondary, CircleShape)
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "locked",
                                        tint = ErrorRed,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "Requires: $parentName",
                                        color = ErrorRed,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Checkbox toggle node (Skeuomorphic tactile switch)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            if (isLocked) Color.White.copy(alpha = 0.02f)
                            else if (isCompleted) themeColor 
                            else Color.White.copy(alpha = 0.05f),
                            CircleShape
                        )
                        .border(
                            1.dp,
                            if (isLocked) Color.White.copy(alpha = 0.1f)
                            else if (isCompleted) themeColor 
                            else Color.White.copy(alpha = 0.2f),
                            CircleShape
                        )
                        .clickable(enabled = !isLocked) { viewModel.toggleHabit(habit.id) }
                        .testTag("habit_checkbox_${habit.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLocked) {
                        Icon(Icons.Default.Lock, contentDescription = "Locked", tint = TextSecondary, modifier = Modifier.size(12.dp))
                    } else if (isCompleted) {
                        Icon(Icons.Default.Check, contentDescription = "Done", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Expandable Drawer for detailed scheduling & notes
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Divider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = habit.description,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Note Logger Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = entryNote,
                            onValueChange = { entryNote = it },
                            placeholder = { Text("Add daily reflection log", color = TextSecondary, fontSize = 12.sp) },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.03f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedIndicatorColor = themeColor,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        )

                        Button(
                            onClick = {
                                viewModel.completeHabitWithStatus(habit.id, "Completed", entryNote)
                                entryNote = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text("Save", fontSize = 12.sp, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Secondary actions (Skip, Reschedule)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Skip routine",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    viewModel.completeHabitWithStatus(habit.id, "Skipped", "Skipped by choice.")
                                    expanded = false
                                }
                                .padding(8.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = "Reschedule",
                            color = themeColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    viewModel.completeHabitWithStatus(habit.id, "Rescheduled", "Rescheduled.")
                                    expanded = false
                                }
                                .padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}
