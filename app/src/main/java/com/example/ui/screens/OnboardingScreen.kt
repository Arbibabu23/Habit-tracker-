package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableStateOf(1) }
    val selectedGoals by viewModel.selectedGoals.collectAsState()
    val selectedDifficulty by viewModel.selectedDifficulty.collectAsState()
    val selectedTargetPeriod by viewModel.selectedTargetPeriod.collectAsState()
    val isProcessing by viewModel.isOnboardingProcessing.collectAsState()
    val aiPlan by viewModel.onboardingAiPlan.collectAsState()

    val goalsList = listOf(
        GoalOption("Health", "Hydration & Weight", Icons.Default.WaterDrop, "#2563EB"),
        GoalOption("Fitness", "Workout & Gym", Icons.Default.FitnessCenter, "#10B981"),
        GoalOption("Study", "Focus Hours", Icons.Default.School, "#8B5CF6"),
        GoalOption("Reading", "Daily Pages", Icons.Default.MenuBook, "#A78BFA"),
        GoalOption("Prayer", "Spiritual prayers", Icons.Default.Favorite, "#F59E0B"),
        GoalOption("Mindfulness", "Meditation", Icons.Default.Spa, "#06B6D4"),
        GoalOption("Finance", "Tracking expenses", Icons.Default.Payments, "#10B981"),
        GoalOption("Productivity", "Deep work logic", Icons.Default.Bolt, "#4F46E5")
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SlateDarkBackground,
                        Color(0xFF0F1524),
                        Color(0xFF050B14)
                    )
                )
            )
    ) {
        // Glowing space nodes
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(y = (-50).dp, x = 100.dp)
                .blur(90.dp)
                .background(DeepIndigo.copy(alpha = 0.25f), shape = RoundedCornerShape(150.dp))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header / Stepper Progress
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HabitOS Setup",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )

                // Sleek Stepper pills
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(3) { index ->
                        val active = step >= (index + 1)
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(6.dp)
                                .background(
                                    if (active) GlowIndigo else GlassWhite,
                                    shape = RoundedCornerShape(3.dp)
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // STEP 1: CHOOSE GOALS
            if (step == 1) {
                Text(
                    text = "What is your vision?",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Select one or more core pillars to organize your HabitOS routine nodes.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(goalsList) { goalOption ->
                        val isSelected = selectedGoals.contains(goalOption.id)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .clickable { viewModel.toggleGoal(goalOption.id) }
                                .border(
                                    1.dp,
                                    if (isSelected) Color(android.graphics.Color.parseColor(goalOption.colorHex)) else GlassBorder,
                                    RoundedCornerShape(16.dp)
                                ),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(android.graphics.Color.parseColor(goalOption.colorHex)).copy(alpha = 0.15f) else GlassWhite
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            Color(android.graphics.Color.parseColor(goalOption.colorHex)).copy(alpha = 0.2f),
                                            RoundedCornerShape(10.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = goalOption.icon,
                                        contentDescription = null,
                                        tint = Color(android.graphics.Color.parseColor(goalOption.colorHex))
                                    )
                                }

                                Column {
                                    Text(
                                        text = goalOption.id,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = goalOption.desc,
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { if (selectedGoals.isNotEmpty()) step = 2 },
                    enabled = selectedGoals.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = GlowIndigo),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("onboarding_step1_next"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Deconstruct Pillars", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // STEP 2: SET DIFFICULTY & TARGET PERIODS
            else if (step == 2) {
                Text(
                    text = "Set Routine Difficulty",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Calibrate the tracking intensity to match your current discipline level.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Difficulty Choices
                    Text("DIFFICULTY THRESHOLD", fontSize = 11.sp, fontWeight = FontWeight.Black, color = TextSecondary, letterSpacing = 1.sp)

                    listOf(
                        DifficultyOption("Beginner", "Light, low friction targets. Perfect to build momentum.", "90% start rate"),
                        DifficultyOption("Intermediate", "Healthy daily balance. Requires steady consistent discipline.", "80% completion rate"),
                        DifficultyOption("Advanced", "SaaS professional standard. Extra rigid, epic challenges.", "100% daily focus")
                    ).forEach { diff ->
                        val isSelected = selectedDifficulty == diff.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectedDifficulty.value = diff.id }
                                .border(
                                    1.dp,
                                    if (isSelected) GlowIndigo else GlassBorder,
                                    RoundedCornerShape(16.dp)
                                ),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) GlowIndigo.copy(alpha = 0.1f) else GlassWhite
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.selectedDifficulty.value = diff.id },
                                    colors = RadioButtonDefaults.colors(selectedColor = GlowIndigo, unselectedColor = TextSecondary)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(diff.id, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    Text(diff.desc, color = TextSecondary, fontSize = 12.sp)
                                }

                                Badge(
                                    containerColor = GlassWhite,
                                    contentColor = GlowIndigo,
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Text(diff.badgeValue, fontSize = 10.sp, modifier = Modifier.padding(4.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Cycle Duration Setup (Skeuomorphic Selects)
                    Text("CYCLE DURATION TARGET", fontSize = 11.sp, fontWeight = FontWeight.Black, color = TextSecondary, letterSpacing = 1.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf(14, 30, 90).forEach { days ->
                            val isSelected = selectedTargetPeriod == days
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.selectedTargetPeriod.value = days }
                                    .border(
                                        1.dp,
                                        if (isSelected) GlowIndigo else GlassBorder,
                                        RoundedCornerShape(12.dp)
                                    ),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) GlowIndigo.copy(alpha = 0.15f) else GlassWhite
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("$days Days", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                                        Text(
                                            text = if (days == 30) "Standard" else "Sprint",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { step = 1 },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GlassBorder)
                    ) {
                        Text("Back", color = Color.White)
                    }

                    Button(
                        onClick = {
                            step = 3
                            viewModel.generateAIPersonalizedHabitPlan()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GlowIndigo),
                        modifier = Modifier
                            .weight(2f)
                            .height(52.dp)
                            .testTag("onboarding_step2_next"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Construct AI Nodes", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // STEP 3: PLAY GENERATION BY GEMINI COACH
            else if (step == 3) {
                Text(
                    text = "Analyzing Intent with Gemini",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "HabitOS AI is synthesizing behavioral models for your personalized dashboard.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                if (isProcessing) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = GlowIndigo,
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "Calibrating micro-hydration ratios...",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Forging XP achievements & streaking milestones.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                } else {
                    // Show generated review/plan text beautifully in Glass Card with scroll
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = GlassWhite)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = "PERSONAL COACH FORMULATION",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = EmeraldGreen,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = aiPlan ?: "Custom Routine is active. Ready to initiate dashboard tracking.",
                                fontSize = 14.sp,
                                color = Color.White,
                                lineHeight = 20.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            viewModel.navigateTo(MainViewModel.Screen.Dashboard)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("onboarding_launch_app"),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                    ) {
                        Text("Launch HabitOS AI", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

data class GoalOption(
    val id: String,
    val desc: String,
    val icon: ImageVector,
    val colorHex: String
)

data class DifficultyOption(
    val id: String,
    val desc: String,
    val badgeValue: String
)
