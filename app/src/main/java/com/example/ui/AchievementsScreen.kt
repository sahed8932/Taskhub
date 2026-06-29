package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AchievementsScreen(
    viewModel: TaskHubViewModel
) {
    val submissions by viewModel.userSubmissions.collectAsState()
    val userAchievements by viewModel.userAchievements.collectAsState()

    val approvedCount = submissions.count { it.status == "Approved" }

    val badges = listOf(
        Triple("TASKS_10", "Bronze Earner", 10),
        Triple("TASKS_50", "Silver Earner", 50),
        Triple("TASKS_100", "Gold Earner", 100),
        Triple("TASKS_500", "Platinum Specialist", 500),
        Triple("TASKS_1000", "Diamond Legend", 1000)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Headers
        Text(
            text = "Achievement Badges",
            color = TextLight,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Complete online tasks to unlock persistent badges and claim massive coin rewards!",
            color = TextMuted,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Progress view
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SlateCard)
                .border(1.dp, HighDensityBorder, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Text(
                text = "Overall Tasks Completed: $approvedCount Tasks",
                color = TextLight,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(badges) { (code, title, threshold) ->
                val isUnlocked = userAchievements.any { it.code == code }
                val progress = minOf(1f, approvedCount.toFloat() / threshold.toFloat())

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUnlocked) SlateCard else Color.Black.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.border(
                        1.dp,
                        if (isUnlocked) AccentGold.copy(alpha = 0.4f) else HighDensityBorder,
                        RoundedCornerShape(16.dp)
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(if (isUnlocked) AccentGold.copy(alpha = 0.2f) else Color.DarkGray.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isUnlocked) Icons.Filled.EmojiEvents else Icons.Filled.Star,
                                contentDescription = null,
                                tint = if (isUnlocked) AccentGold else TextMuted,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = title,
                            color = if (isUnlocked) TextLight else TextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Requires $threshold completions",
                            color = TextMuted,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Linear progress indicator
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = if (isUnlocked) AccentGold else TelegramBlue,
                            trackColor = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isUnlocked) "UNLOCKED!" else "${(progress * 100).toInt()}%",
                            color = if (isUnlocked) AccentGold else TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
