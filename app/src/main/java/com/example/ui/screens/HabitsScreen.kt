package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Habit
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val habits by viewModel.activeHabits.collectAsState()
    
    var showCreateForm by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    // Form states
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Productivity") }
    var priority by remember { mutableStateOf("Medium") }
    var frequency by remember { mutableStateOf("Daily") }
    var reminderTime by remember { mutableStateOf("08:00") }
    var colorHex by remember { mutableStateOf("#4F46E5") } // Default Indigo
    var iconName by remember { mutableStateOf("bolt") }

    var customIntervalVal by remember { mutableStateOf("3") }
    var selectedDaysOfWeek by remember { mutableStateOf(setOf<String>()) }
    var selectedDependencyHabitId by remember { mutableStateOf(0) }

    val categories = listOf("All", "Health", "Fitness", "Learning", "Productivity", "Spiritual", "Finance", "Relationships")
    val formCategories = listOf("Health", "Fitness", "Learning", "Productivity", "Spiritual", "Finance", "Relationships")
    val priorities = listOf("Low", "Medium", "High")
    val frequencies = listOf("Daily", "Specific Days", "Custom Interval", "Dependency Trigger")
    
    // Sample colors
    val colorPalettes = listOf(
        Pair("#2563EB", "water_drop"), // Blue - Health
        Pair("#10B981", "fitness_center"), // Green - fitness
        Pair("#8B5CF6", "school"), // Purple - Learning
        Pair("#A78BFA", "menu_book"), // Book - reading
        Pair("#F59E0B", "favorite"), // Amber - Spiritual
        Pair("#06B6D4", "spa"), // Cyan - mindfulness
        Pair("#EC4899", "payments") // Pink - Finance
    )

    val filteredHabits = if (selectedCategoryFilter == "All") {
        habits
    } else {
        habits.filter { it.category == selectedCategoryFilter }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateForm = true },
                containerColor = GlowIndigo,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 60.dp).testTag("fab_add_habit")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
            }
        },
        containerColor = SlateDarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Ecosystem Node Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Text(
                "Configure, delete, and synthesize your automatic daily micro-routines.",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            // CATEGORY HORIZONTAL FILTER SLIDER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategoryFilter == cat
                    Card(
                        modifier = Modifier
                            .clickable { selectedCategoryFilter = cat }
                            .border(
                                1.dp,
                                if (isSelected) GlowIndigo else GlassBorder,
                                RoundedCornerShape(12.dp)
                            ),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) GlowIndigo else GlassWhite
                        )
                    ) {
                        Text(
                            text = cat,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // LIST OF CURRENT ACTIVE HABITS
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredHabits.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 44.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.SettingsSuggest, contentDescription = null, tint = TextSecondary.copy(alpha = 0.4f), modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "No nodes found for category: $selectedCategoryFilter",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Click the '+' button to synthesize custom cards.",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                } else {
                    items(filteredHabits) { habit ->
                        HabitSettingsCard(habit = habit, allHabits = habits, onDelete = { viewModel.deleteHabit(habit) })
                    }
                }
            }
        }
    }

    // CREATE HABIT CUSTOM MODAL FORM DIALOG
    if (showCreateForm) {
        AlertDialog(
            onDismissRequest = { showCreateForm = false },
            title = {
                Text(
                    "Synthesize Intelligent Habit",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Habit Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Habit Name", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GlowIndigo,
                            unfocusedBorderColor = GlassBorder
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("add_habit_name_input")
                    )

                    // Habit Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description / Target", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GlowIndigo,
                            unfocusedBorderColor = GlassBorder
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Category Selection Chips
                    Text("CATEGORY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        formCategories.forEach { cat ->
                            val isSelected = category == cat
                            Card(
                                modifier = Modifier
                                    .clickable { category = cat }
                                    .border(1.dp, if (isSelected) GlowIndigo else GlassBorder, RoundedCornerShape(8.dp)),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = if (isSelected) GlowIndigo.copy(alpha = 0.2f) else GlassWhite)
                            ) {
                                Text(cat, color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                            }
                        }
                    }

                    // Priority selection
                    Text("PRIORITY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        priorities.forEach { prio ->
                            val isSelected = priority == prio
                            Button(
                                onClick = { priority = prio },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) GlowIndigo else GlassWhite,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(prio, fontSize = 12.sp)
                            }
                        }
                    }

                    // Frequency & Reminder hour
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("FREQUENCY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            frequencies.forEach { freq ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { frequency = freq }
                                ) {
                                    RadioButton(
                                        selected = frequency == freq,
                                        onClick = { frequency = freq },
                                        colors = RadioButtonDefaults.colors(selectedColor = GlowIndigo, unselectedColor = TextSecondary)
                                    )
                                    Text(freq, color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("REMINDER TIME", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = reminderTime,
                                onValueChange = { reminderTime = it },
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

                    // CONDITIONAL ADVANCED SCHEDULING SECTIONS
                    if (frequency == "Custom Interval") {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("REPEAT EACH (EVERY X DAYS)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            OutlinedTextField(
                                value = customIntervalVal,
                                onValueChange = { customIntervalVal = it.filter { char -> char.isDigit() } },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = GlowIndigo,
                                    unfocusedBorderColor = GlassBorder
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("custom_interval_input")
                            )
                        }
                    }

                    if (frequency == "Specific Days") {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("SELECT RECURRING DAYS OF WEEK", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                                    val isSelected = selectedDaysOfWeek.contains(day)
                                    Card(
                                        modifier = Modifier
                                            .clickable {
                                                selectedDaysOfWeek = if (isSelected) {
                                                    selectedDaysOfWeek - day
                                                } else {
                                                    selectedDaysOfWeek + day
                                                }
                                            }
                                            .border(1.dp, if (isSelected) GlowIndigo else GlassBorder, RoundedCornerShape(8.dp)),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = if (isSelected) GlowIndigo.copy(alpha = 0.2f) else GlassWhite)
                                    ) {
                                        Text(
                                            text = day,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (frequency == "Dependency Trigger") {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("TRIGGER PREREQUISITE HABIT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            if (habits.isEmpty()) {
                                Text("No other habits active to bind dependency.", color = ErrorRed, fontSize = 12.sp)
                            } else {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 120.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    habits.forEach { otherHabit ->
                                        val isSelected = selectedDependencyHabitId == otherHabit.id
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { selectedDependencyHabitId = otherHabit.id }
                                                .background(if (isSelected) GlowIndigo.copy(alpha = 0.15f) else Color.Transparent, RoundedCornerShape(8.dp))
                                                .padding(vertical = 4.dp, horizontal = 8.dp)
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { selectedDependencyHabitId = otherHabit.id },
                                                colors = RadioButtonDefaults.colors(selectedColor = GlowIndigo)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(otherHabit.name, color = Color.White, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Theme Palette Selection
                    Text("PALETTE THEME & ICON", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        colorPalettes.forEach { palette ->
                            val isSelected = colorHex == palette.first
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(android.graphics.Color.parseColor(palette.first)), CircleShape)
                                    .border(
                                        2.dp,
                                        if (isSelected) Color.White else Color.Transparent,
                                        CircleShape
                                    )
                                    .clickable {
                                        colorHex = palette.first
                                        iconName = palette.second
                                    }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val interval = if (frequency == "Custom Interval") customIntervalVal.toIntOrNull() ?: 3 else 0
                            val daysOfWeek = if (frequency == "Specific Days") selectedDaysOfWeek.sorted().joinToString(",") else ""
                            val depId = if (frequency == "Dependency Trigger") selectedDependencyHabitId else 0

                            viewModel.addCustomHabit(
                                name = name,
                                description = description,
                                category = category,
                                priority = priority,
                                frequency = frequency,
                                reminderTime = reminderTime,
                                colorHex = colorHex,
                                iconName = iconName,
                                customIntervalDays = interval,
                                customDaysOfWeek = daysOfWeek,
                                dependsOnHabitId = depId
                            )
                            // reset
                            name = ""
                            description = ""
                            frequency = "Daily"
                            customIntervalVal = "3"
                            selectedDaysOfWeek = emptySet()
                            selectedDependencyHabitId = 0
                            showCreateForm = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_habit_save")
                ) {
                    Text("Synthesize Node", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateForm = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SlateCardBackground,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
        )
    }
}

@Composable
fun HabitSettingsCard(
    habit: Habit,
    allHabits: List<Habit> = emptyList(),
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeColor = remember {
        try {
            Color(android.graphics.Color.parseColor(habit.colorHex))
        } catch (e: Exception) {
            GlowIndigo
        }
    }

    val parentName = remember(habit, allHabits) {
        if (habit.dependsOnHabitId > 0) {
            allHabits.find { it.id == habit.dependsOnHabitId }?.name
        } else null
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, GlassBorder.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SlateCardBackground)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(themeColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = themeColor, modifier = Modifier.size(18.dp))
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(habit.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    val scheduleDesc = when (habit.frequency) {
                        "Custom Interval" -> "Every ${habit.customIntervalDays} days"
                        "Specific Days" -> "On: ${habit.customDaysOfWeek}"
                        "Dependency Trigger" -> if (parentName != null) "Triggered by: $parentName" else "Dependency Trigger"
                        else -> habit.frequency
                    }
                    Text("Reminder: ${habit.reminderTime} • $scheduleDesc", color = TextSecondary, fontSize = 12.sp)
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Habit", tint = ErrorRed.copy(alpha = 0.8f))
            }
        }
    }
}
