package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackersScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val user by viewModel.currentUser.collectAsState()

    // Observe today's state variables
    val waterLog by viewModel.waterLogToday.collectAsState()
    val sleepLog by viewModel.sleepLogToday.collectAsState()
    val mood by viewModel.moodToday.collectAsState()
    val expensesLog by viewModel.expensesToday.collectAsState()
    val readingLog by viewModel.readingToday.collectAsState()
    val screenTimeLog by viewModel.screenTimeToday.collectAsState()

    // Temporary inputs
    var inputExpenseStr by remember { mutableStateOf("") }
    var inputReadingStr by remember { mutableStateOf("") }
    var inputScreenTimeStr by remember { mutableStateOf("") }

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
            text = "Advanced Log System",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
        Text(
            text = "Fine-tune and store quantitative parameters detailing health, spirituality, finance, and wellness.",
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 1. WATER INTAKE TRACKER (Tactile Plus/Minus buttons)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GlassBorder.copy(alpha = 0.08f), RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCardBackground)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WaterDrop, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Hydration Index", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("${waterLog.toInt()} / 8 Cups", color = ElectricBlue, fontSize = 14.sp, fontWeight = FontWeight.Black)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            val nextVal = (waterLog - 1).coerceAtLeast(0.0)
                            viewModel.logMetric("Water", nextVal, "Cups", "Subtracted cup")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GlassWhite),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("-1 Cup", color = Color.White, fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            val nextVal = waterLog + 1
                            viewModel.logMetric("Water", nextVal, "Cups", "Added cup")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_log_water")
                    ) {
                        Text("+1 Cup", color = Color.White, fontSize = 13.sp)
                    }
                }
            }
        }

        // 2. SLEEP tracker slider
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GlassBorder.copy(alpha = 0.08f), RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCardBackground)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bedtime, contentDescription = null, tint = CustomPurple, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sleep Log", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(String.format(java.util.Locale.US, "%.1f Hrs", sleepLog), color = CustomPurple, fontSize = 14.sp, fontWeight = FontWeight.Black)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Slider(
                    value = sleepLog.toFloat(),
                    onValueChange = {
                        viewModel.logMetric("Sleep", it.toDouble(), "Hours", "Logged Sleep")
                    },
                    valueRange = 0f..12f,
                    steps = 24,
                    colors = SliderDefaults.colors(
                        thumbColor = CustomPurple,
                        activeTrackColor = CustomPurple,
                        inactiveTrackColor = GlassWhite
                    )
                )

                Text(
                    text = "Optimal standard target: 7 to 9 hours",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // 3. MOOD TRACKER (Expressive Slider & Emojis)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GlassBorder.copy(alpha = 0.08f), RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCardBackground)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Mood, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mood Tracker", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    val moodText = when (mood.toInt()) {
                        1 -> "😢 Awful"
                        2 -> "😔 Bad"
                        3 -> "😐 Okay"
                        4 -> "😊 Awesome"
                        else -> "🔥 Epic"
                    }
                    Text(moodText, color = GoldAccent, fontSize = 14.sp, fontWeight = FontWeight.Black)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Slider(
                    value = mood.toFloat(),
                    onValueChange = {
                        viewModel.logMetric("Mood", it.toInt().toDouble(), "Scale", "Logged mood")
                    },
                    valueRange = 1f..5f,
                    steps = 3,
                    colors = SliderDefaults.colors(
                        thumbColor = GoldAccent,
                        activeTrackColor = GoldAccent,
                        inactiveTrackColor = GlassWhite
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("😢", fontSize = 14.sp)
                    Text("😐", fontSize = 14.sp)
                    Text("🔥", fontSize = 14.sp)
                }
            }
        }

        // 4. EXPENSES tracker form
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GlassBorder.copy(alpha = 0.08f), RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCardBackground)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Payments, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Financial Outlays", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(String.format(java.util.Locale.US, "$%.2f", expensesLog), color = EmeraldGreen, fontSize = 14.sp, fontWeight = FontWeight.Black)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputExpenseStr,
                        onValueChange = { inputExpenseStr = it },
                        placeholder = { Text("Log dollar outlay", color = TextSecondary, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = GlassBorder
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp)
                            .testTag("expense_input")
                    )

                    Button(
                        onClick = {
                            val expenseValue = inputExpenseStr.toDoubleOrNull() ?: 0.0
                            viewModel.logMetric("Expenses", expenseValue, "USD", "Logged expense")
                            inputExpenseStr = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_log_expense")
                    ) {
                        Text("Log $", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 5. READING PAGES LOG
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GlassBorder.copy(alpha = 0.08f), RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCardBackground)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reading Log", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("${readingLog.toInt()} Pages", color = CyanAccent, fontSize = 14.sp, fontWeight = FontWeight.Black)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputReadingStr,
                        onValueChange = { inputReadingStr = it },
                        placeholder = { Text("Enter pages read", color = TextSecondary, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = GlassBorder
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp)
                    )

                    Button(
                        onClick = {
                            val pagesValue = inputReadingStr.toDoubleOrNull() ?: 0.0
                            viewModel.logMetric("Reading", pagesValue, "Pages", "Logged reading progress")
                            inputReadingStr = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Log", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 6. SCREEN TIME
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GlassBorder.copy(alpha = 0.08f), RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCardBackground)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Screen Time Tracker", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(String.format(java.util.Locale.US, "%.1f Hrs", screenTimeLog), color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Black)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputScreenTimeStr,
                        onValueChange = { inputScreenTimeStr = it },
                        placeholder = { Text("Log phone hours", color = TextSecondary, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = TextSecondary,
                            unfocusedBorderColor = GlassBorder
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp)
                    )

                    Button(
                        onClick = {
                            val hrsValue = inputScreenTimeStr.toDoubleOrNull() ?: 0.0
                            viewModel.logMetric("ScreenTime", hrsValue, "Hours", "Logged phone usage")
                            inputScreenTimeStr = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GlassWhite),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Log Time", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(44.dp))
    }
}
