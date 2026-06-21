package com.example.ui.screens

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val user by viewModel.currentUser.collectAsState()
    val achievements by viewModel.achievements.collectAsState()

    var showLegalDialog by remember { mutableStateOf(false) }
    var selectedLegalTopic by remember { mutableStateOf("Privacy Policy") }

    val allPotentialBadges = listOf(
        BadgeDef("first_habit", "Step One", "Created your very first habit in HabitOS. Journey begins!", "emoji_events", GoldAccent),
        BadgeDef("streak_7", "Aweekened Power", "Sustained a disciplined 7-day habit completion streak.", "local_fire_department", Color(0xFFFF5722)),
        BadgeDef("streak_30", "Monthly Conqueror", "Sustained an unbeatable 30-day streak! Pure perfection.", "whatshot", Color(0xFFFF9800)),
        BadgeDef("consistency_master", "Consistency Aspirant", "Successfully checked off 10 habit completions.", "star", EmeraldGreen),
        BadgeDef("study_champ", "Study Champion", "Logged learning study stats 3+ times.", "school", CustomPurple),
        BadgeDef("fitness_warrior", "Fitness Warrior", "Logged workout activities 3+ times.", "fitness_center", CyanAccent),
        BadgeDef("productivity_legend", "Productivity Legend", "Completed 25 habits across categories! Brilliant.", "bolt", GlowIndigo)
    )

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
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Center Profile Avatar initials circle
        Box(
            modifier = Modifier
                .size(90.dp)
                .background(
                    Brush.radialGradient(listOf(GlowIndigo, DeepIndigo)),
                    CircleShape
                )
                .border(2.dp, GlassBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            val initials = if (!user?.fullName.isNullOrBlank()) {
                user!!.fullName.split(" ").map { it.take(1) }.joinToString("").take(2).uppercase()
            } else {
                "OS"
            }
            Text(
                text = initials,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black
            )
        }

        // Profile meta
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = user?.fullName ?: "Disciplined Achiever",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "@${user?.username ?: "achiever"}",
                color = GlowIndigo,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "HabitOS AI Specialist • USA",
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Dynamic XP Indicator Progress Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GlassBorder.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCardBackground)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // XP values
                val currentXp = user?.xp ?: 0
                val level = user?.level ?: 1
                val base = (level - 1) * 150
                val levelProgressionXp = currentXp - base
                val target = 150
                val ratio = (levelProgressionXp.toFloat() / target.toFloat()).coerceIn(0f, 1f)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("LEVEL $level PROGRESS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Text("$levelProgressionXp / $target XP to level up", fontSize = 11.sp, color = GlowIndigo)
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = ratio,
                    color = GlowIndigo,
                    trackColor = GlassWhite,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
        }

        // Statistics node summary block
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatSummaryBlock(label = "Life Score", value = String.format(java.util.Locale.US, "%.1f", user?.lifeScore ?: 80.0), color = EmeraldGreen, modifier = Modifier.weight(1f))
            StatSummaryBlock(label = "Streaks", value = "${user?.currentStreak ?: 0} dys", color = Color(0xFFFF5722), modifier = Modifier.weight(1f))
            StatSummaryBlock(label = "Achievements", value = "${achievements.size} Badges", color = GoldAccent, modifier = Modifier.weight(1f))
        }

        Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 8.dp))

        // BADGED ACHIEVEMENTS GRID
        Text(
            text = "ACHIEVEMENTS & TROPHIES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = TextSecondary,
            letterSpacing = 1.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        allPotentialBadges.forEach { badge ->
            val unlocked = achievements.any { it.badgeId == badge.id }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (unlocked) badge.accentColor.copy(alpha = 0.3f) else GlassBorder.copy(alpha = 0.03f),
                        RoundedCornerShape(14.dp)
                    ),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (unlocked) badge.accentColor.copy(alpha = 0.05f) else SlateCardBackground.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (unlocked) badge.accentColor.copy(alpha = 0.15f) else GlassWhite,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (badge.iconName) {
                                "emoji_events" -> Icons.Default.EmojiEvents
                                "local_fire_department" -> Icons.Default.LocalFireDepartment
                                "whatshot" -> Icons.Default.Whatshot
                                "star" -> Icons.Default.Star
                                "school" -> Icons.Default.School
                                "fitness_center" -> Icons.Default.FitnessCenter
                                else -> Icons.Default.Bolt
                            },
                            contentDescription = null,
                            tint = if (unlocked) badge.accentColor else TextSecondary.copy(alpha = 0.5f)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = badge.name,
                            color = if (unlocked) Color.White else TextSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = badge.description,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )
                    }

                    if (!unlocked) {
                        Icon(Icons.Default.Lock, contentDescription = "Locked", tint = TextSecondary.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                    } else {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Unlocked", tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 8.dp))

        // LEGAL DECLARATIONS BUTTON
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showLegalDialog = true }
                .border(1.dp, GlassBorder.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCardBackground)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Gavel, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Legal, GDPR & Compliance Info", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // LOGOUT BUTTON
        Button(
            onClick = { viewModel.logout() },
            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.15f)),
            border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_logout")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Logout, contentDescription = "Log Out", tint = ErrorRed, modifier = Modifier.size(16.dp))
                Text("Log Out Safe Session", color = ErrorRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(64.dp))
    }

    // COMPLIANT DISCLOSURES OVERLAY MODAL
    if (showLegalDialog) {
        AlertDialog(
            onDismissRequest = { showLegalDialog = false },
            title = {
                Text("Legislation & Compliance Panel", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Sliding segment
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Privacy Policy", "GDPR Compliance", "Terms of Use", "Cookie Policy").forEach { topic ->
                            val isSelected = selectedLegalTopic == topic
                            Card(
                                modifier = Modifier
                                    .clickable { selectedLegalTopic = topic }
                                    .border(1.dp, if (isSelected) GlowIndigo else GlassBorder, RoundedCornerShape(8.dp)),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = if (isSelected) GlowIndigo.copy(alpha = 0.2f) else GlassWhite)
                            ) {
                                Text(topic, color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp))
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .verticalScroll(rememberScrollState())
                            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                    ) {
                        val policyText = when (selectedLegalTopic) {
                            "Privacy Policy" -> """
                                HabitOS AI is fully committed to protecting your personal data privacy indices.
                                
                                💡 KEY PRINCIPLES:
                                1. Full end-to-end SQLite local containment. Your logs remain on this terminal hardware unless safely backed up.
                                2. Transparency. We document every transaction and Gemini Rest request payload explicitly.
                                3. Minimal storage indices. No personal details are ever harvested by telemetry metrics.
                            """.trimIndent()
                            "GDPR Compliance" -> """
                                We comply 100% with the European Union's General Data Protection Regulation (Regulation EU 2016/679).
                                
                                🔧 YOUR REGISTERED RIGHTS:
                                - **Right to be Forgotten**: Clicking 'Delete Account' wipes all database tables.
                                - **Data Portability**: Option to export local SQLite as binary files/raw JSON format.
                                - **Restriction of Processing**: No profiling of spiritual habits is performed.
                            """.trimIndent()
                            "Cookie Policy" -> """
                                Since HabitOS AI Android runs wholly inside local container sandboxing, we store zero standard browser cookies.
                                
                                We persist only secure local shared preference flags detailing notification state to manage background notifications safely.
                            """.trimIndent()
                            else -> """
                                TERMS OF USE & LICENSE AGREEMENT:
                                
                                1. Permission is granted to run HabitOS AI for individual habit optimization, personal study logging, and mindfulness tracking.
                                2. You may copy code layouts for developer education. No commercial unauthorized cloning.
                                3. NO LIABILITY for routine failures or missed glass of water water drops! Stay disciplined.
                            """.trimIndent()
                        }
                        Text(
                            text = policyText,
                            color = TextPrimary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showLegalDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = GlowIndigo),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Close Panel", color = Color.White)
                }
            },
            containerColor = SlateCardBackground,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
        )
    }
}

@Composable
fun StatSummaryBlock(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(1.dp, GlassBorder.copy(alpha = 0.03f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SlateCardBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = color, fontSize = 16.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

data class BadgeDef(
    val id: String,
    val name: String,
    val description: String,
    val iconName: String,
    val accentColor: Color
)
