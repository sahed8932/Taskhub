package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DailyBonusScreen(
    viewModel: TaskHubViewModel
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val scope = rememberCoroutineScope()

    var isSpinning by remember { mutableStateOf(false) }
    var wheelAngle by remember { mutableStateOf(0f) }
    var wonCoins by remember { mutableStateOf<Double?>(null) }

    var isBoxOpening by remember { mutableStateOf(false) }
    var boxCoins by remember { mutableStateOf<Int?>(null) }

    val rotation = remember { Animatable(0f) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Daily Checkin Banner
        item {
            currentUser?.let { user ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SlateCard)
                        .border(1.dp, HighDensityBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Claim Rewards",
                        color = TelegramBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Daily Check-in & Streak",
                        color = TextLight,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Current Streak: ${user.streakDays} Days",
                                color = TextLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Claim daily to grow your streak reward bonus multiplier!",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                        Button(
                            onClick = { viewModel.claimDailyCheckIn() },
                            colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Claim Now", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Spin Wheel Card
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SlateCard)
                    .border(1.dp, HighDensityBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Lucky Spin Wheel",
                    color = TextLight,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Text(
                    text = "Try your luck on the spin wheel once per day!",
                    color = TextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Interactive Spin Wheel Graphics Representation
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .border(4.dp, TelegramBlue, CircleShape)
                        .background(Color.Black.copy(alpha = 0.3f))
                        .rotate(rotation.value),
                    contentAlignment = Alignment.Center
                ) {
                    // Segment dividers drawing or layout
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 2.dp.toPx()
                        // draw slice lines
                        for (i in 0 until 6) {
                            val angleRad = Math.toRadians((i * 60).toDouble())
                            val endX = (center.x + size.width * Math.cos(angleRad)).toFloat()
                            val endY = (center.y + size.height * Math.sin(angleRad)).toFloat()
                            drawLine(
                                color = TelegramBlue.copy(alpha = 0.4f),
                                start = center,
                                end = androidx.compose.ui.geometry.Offset(endX, endY),
                                strokeWidth = strokeWidth
                            )
                        }
                    }
                    // Visual rewards texts in wheel
                    Text("50", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.offset(y = (-50).dp))
                    Text("10", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.offset(x = (50).dp))
                    Text("5", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.offset(y = (50).dp))
                    Text("2.5", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.offset(x = (-50).dp))
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (!isSpinning) {
                            scope.launch {
                                isSpinning = true
                                rotation.animateTo(
                                    targetValue = rotation.value + 1440f + (0..360).random().toFloat(),
                                    animationSpec = tween(durationMillis = 2500, easing = FastOutSlowInEasing)
                                )
                                viewModel.spinWheel()
                                isSpinning = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isSpinning,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isSpinning) "Spinning Wheel..." else "SPIN WHEEL", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Lucky Box Chest Card
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SlateCard)
                    .border(1.dp, HighDensityBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Mystery Lucky Box",
                    color = TextLight,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Text(
                    text = "Open a box and receive up to 50 BDT instantly!",
                    color = TextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.2f))
                        .border(1.dp, AccentGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable(enabled = !isBoxOpening) {
                            scope.launch {
                                isBoxOpening = true
                                delay(1200) // simulated timing
                                viewModel.openLuckyBox()
                                isBoxOpening = false
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (isBoxOpening) Icons.Filled.CardGiftcard else Icons.Filled.Inventory,
                            contentDescription = "Box",
                            tint = AccentGold,
                            modifier = Modifier.size(45.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isBoxOpening) "Opening..." else "TAP TO OPEN",
                            color = AccentGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
