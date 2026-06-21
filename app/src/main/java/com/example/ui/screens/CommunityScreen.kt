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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val user by viewModel.currentUser.collectAsState()
    var selectedTab by remember { mutableStateOf("Leaderboard") }

    val leaderboardUsers = listOf(
        LeaderboardEntry(1, "Alex Morgan", "Productivity Hacker", 32, 1450, "🔥 32 Day Streak"),
        LeaderboardEntry(2, "Mei-Ling Zhou", "Mindfulness Zen", 28, 1280, "🔥 28 Day Streak"),
        LeaderboardEntry(3, "Sofia Alvarez", "Fitness Beast", 21, 1050, "🔥 21 Day Streak"),
        LeaderboardEntry(4, user?.fullName ?: "Disciplined Achiever", "Self Developer", user?.currentStreak ?: 0, (user?.level ?: 1) * 150 + (user?.xp ?: 0), "🔥 ${user?.currentStreak ?: 0} Day Streak")
    ).sortedByDescending { it.xp }

    val activeBattles = listOf(
        HabitBattle("Midnight Read-Off", "Read 10 pages before 23:59 each night.", "5 Days Left", 124, EmeraldGreen),
        HabitBattle("Diaphragmatic Deep Meditation", "Mediate 15 mins daily.", "2 Days Left", 87, CustomPurple),
        HabitBattle("Hydration Marathon", "Log 3000ml of hydration water.", "Completed", 342, ElectricBlue)
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Community & Arena",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
        Text(
            text = "Engage in live multiplayer Habit Battles, track leaderboard streaks, and find accountability partners.",
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Custom segment switch tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(GlassWhite, RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            listOf("Leaderboard", "Habit Battles", "Partners").forEach { tab ->
                val active = selectedTab == tab
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedTab = tab },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (active) GlowIndigo else Color.Transparent
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // LEADERBOARD TAB
        if (selectedTab == "Leaderboard") {
            Text("GLOBAL LEADERBOARD STREAKS", fontSize = 11.sp, fontWeight = FontWeight.Black, color = TextSecondary)
            
            leaderboardUsers.forEachIndexed { idx, entry ->
                val isMe = entry.name == (user?.fullName ?: "Disciplined Achiever")
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isMe) GlowIndigo.copy(alpha = 0.5f) else GlassBorder.copy(alpha = 0.05f),
                            RoundedCornerShape(16.dp)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isMe) GlowIndigo.copy(alpha = 0.1f) else SlateCardBackground
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Rank Number badge
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(
                                        when (idx) {
                                            0 -> GoldAccent.copy(alpha = 0.2f)
                                            1 -> Color(0xFFC0C0C0).copy(alpha = 0.2f)
                                            2 -> Color(0xFFCD7F32).copy(alpha = 0.2f)
                                            else -> GlassWhite
                                        },
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${idx + 1}",
                                    color = when (idx) {
                                        0 -> GoldAccent
                                        1 -> Color(0xFFE2E8F0)
                                        2 -> Color(0xFFE28A3E)
                                        else -> Color.White
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(entry.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    if (isMe) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(GlowIndigo, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text("ME", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White)
                                        }
                                    }
                                }
                                Text(entry.title, color = TextSecondary, fontSize = 11.sp)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(entry.streakText, color = Color(0xFFFF5722), fontSize = 13.sp, fontWeight = FontWeight.Black)
                            Text("${entry.xp} XP", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // HABIT BATTLES TAB
        else if (selectedTab == "Habit Battles") {
            Text("LIVE BATTLEGROUND ARENAS", fontSize = 11.sp, fontWeight = FontWeight.Black, color = TextSecondary)
            
            activeBattles.forEach { battle ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GlassBorder.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateCardBackground)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(battle.themeColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(battle.timeRemaining, color = battle.themeColor, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                            }

                            Text("${battle.participants} Enlisted", color = TextSecondary, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(battle.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(battle.description, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp, bottom = 14.dp))

                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(containerColor = battle.themeColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(42.dp)
                        ) {
                            Text("Enlist in Battle", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // PARTNERS TAB
        else {
            Text("ACCOUNTABILITY TEAM PARTNER LIST", fontSize = 11.sp, fontWeight = FontWeight.Black, color = TextSecondary)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(1.dp, Brush.horizontalGradient(listOf(GlowIndigo, CustomPurple))),
                        RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SlateCardBackground)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        "Enlist Accountability Partner",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Pairing your HabitOS routine with a dedicated advisor increases completion rate of target habits by up to 2.5x times.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                    )

                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = GlowIndigo),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("Search Registered Partners", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(44.dp))
    }
}

data class LeaderboardEntry(
    val rank: Int,
    val name: String,
    val title: String,
    val streak: Int,
    val xp: Int,
    val streakText: String
)

data class HabitBattle(
    val name: String,
    val description: String,
    val timeRemaining: String,
    val participants: Int,
    val themeColor: Color
)
