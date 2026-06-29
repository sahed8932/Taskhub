package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TaskEntity

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TasksListScreen(
    viewModel: TaskHubViewModel,
    onNavigate: (Screen) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val filteredTasks by viewModel.filteredTasks.collectAsState()

    val categories = listOf("All", "YouTube", "Telegram", "Twitter", "Signup")
    val filters = listOf("Newest", "Highest Reward", "Most Popular", "Difficulty")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("Search tasks e.g. YouTube...", color = TextMuted) },
            leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = "Search", tint = TextMuted) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TelegramBlue,
                unfocusedBorderColor = HighDensityBorder,
                focusedTextColor = TextLight,
                unfocusedTextColor = TextLight
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Categories selector
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { cat ->
                val isSelected = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) TelegramBlue else SlateCard)
                        .border(1.dp, HighDensityBorder, RoundedCornerShape(20.dp))
                        .clickable { viewModel.updateSelectedCategory(cat) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) ColorPrimaryOnText else TextLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Sorting/Filters row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filters) { filter ->
                val isSelected = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) TelegramBlue.copy(alpha = 0.15f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (isSelected) TelegramBlue else HighDensityBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { viewModel.updateSelectedFilter(filter) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = filter,
                        color = if (isSelected) TelegramBlue else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Tasks list
        if (filteredTasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.ListAlt,
                        contentDescription = "Empty",
                        tint = TextMuted,
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No tasks match your filter",
                        color = TextLight,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Try clearing search or picking another category.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredTasks) { task ->
                    TaskCard(
                        task = task,
                        onClick = {
                            viewModel.selectTask(task)
                            onNavigate(Screen.TaskDetails(task.id))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TaskCard(
    task: TaskEntity,
    onClick: () -> Unit
) {
    val isFull = task.remainingSlots <= 0

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (task.isPaused) SlateCard.copy(alpha = 0.5f) else SlateCard
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (task.isPaused) Color.DarkGray else HighDensityBorder,
                RoundedCornerShape(16.dp)
            )
            .clickable(enabled = !isFull && !task.isPaused) { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header: Category & Reward Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val icon = when(task.category.uppercase()) {
                        "YOUTUBE" -> Icons.Filled.PlayArrow
                        "TELEGRAM" -> Icons.Filled.Send
                        "TWITTER" -> Icons.Filled.Share
                        else -> Icons.Filled.OpenInNew
                    }
                    val iconColor = when(task.category.uppercase()) {
                        "YOUTUBE" -> Color.Red
                        "TELEGRAM" -> TelegramBlue
                        "TWITTER" -> Color.Cyan
                        else -> AccentGreen
                    }
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(iconColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = task.category,
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Reward badge
                Surface(
                    color = TelegramBlue.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TelegramBlue.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = "+${String.format("%.1f", task.reward)} Coins",
                        color = TelegramBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = task.title,
                color = TextLight,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Details Line: Duration, Difficulty, Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Time
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.Timer, contentDescription = "Time", tint = TextMuted, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = task.estimatedTime, color = TextMuted, fontSize = 11.sp)
                    }
                    // Difficulty
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.TrendingUp, contentDescription = "Difficulty", tint = TextMuted, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = task.difficulty,
                            color = when(task.difficulty) {
                                "Hard" -> Color.Red
                                "Medium" -> AccentGold
                                else -> AccentGreen
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Slots progress
                Text(
                    text = "Slots: ${task.remainingSlots} / ${task.totalSlots}",
                    color = if (isFull) Color.Red else TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            val progress = if (task.totalSlots > 0) {
                task.remainingSlots.toFloat() / task.totalSlots.toFloat()
            } else 0f
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = if (isFull) Color.Red else TelegramBlue,
                trackColor = Color.DarkGray
            )

            if (isFull || task.isPaused) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = if (isFull) Color.Red.copy(alpha = 0.1f) else Color.DarkGray.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isFull) "Task Full (0 slots left)" else "Task Paused by Admin",
                        color = if (isFull) Color.Red else TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}
