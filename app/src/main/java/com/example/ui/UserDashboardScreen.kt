package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NotificationEntity

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserDashboardScreen(
    viewModel: TaskHubViewModel,
    onNavigate: (Screen) -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val activeTasks by viewModel.activeTasks.collectAsState()
    val submissions by viewModel.userSubmissions.collectAsState()
    val notifications by viewModel.userNotifications.collectAsState()

    var promoCodeText by remember { mutableStateOf("") }

    val completedTasksCount = submissions.count { it.status == "Approved" }
    val pendingTasksCount = submissions.count { it.status == "Pending Review" }
    val availableTasksCount = activeTasks.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header
        item {
            currentUser?.let { user ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(TelegramBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.username.take(2).uppercase(),
                                color = ColorPrimaryOnText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Column {
                            Text(
                                text = "Welcome back",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "@${user.username}",
                                color = TextLight,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Notification bell representation
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SlateCard)
                            .border(1.dp, HighDensityBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "Notifications",
                            tint = TextLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Stats Dashboard Grid
        item {
            currentUser?.let { user ->
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    MajesticBalanceCard(
                        totalBalance = user.totalBalance,
                        pendingBalance = user.pendingBalance,
                        totalEarned = user.totalEarned,
                        modifier = Modifier.fillMaxWidth()
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        maxItemsInEachRow = 2
                    ) {
                        val itemWidth = Modifier.weight(1f).fillMaxWidth()

                        DashboardStatCard(
                            title = "Available Tasks",
                            value = "$availableTasksCount",
                            icon = Icons.Filled.ListAlt,
                            tint = TelegramBlue,
                            modifier = itemWidth
                        )
                        DashboardStatCard(
                            title = "Referrals",
                            value = "${user.referralsL1 + user.referralsL2 + user.referralsL3}",
                            icon = Icons.Filled.People,
                            tint = TelegramBlue,
                            modifier = itemWidth
                        )
                        DashboardStatCard(
                            title = "Completed Tasks",
                            value = "$completedTasksCount",
                            icon = Icons.Filled.CheckCircle,
                            tint = AccentGreen,
                            modifier = itemWidth
                        )
                        DashboardStatCard(
                            title = "Referral Income",
                            value = "${String.format("%.1f", user.referralEarnings)} Coins",
                            icon = Icons.Filled.Group,
                            tint = AccentGold,
                            modifier = itemWidth
                        )
                    }
                }
            }
        }

        // Quick Navigation Grid
        item {
            Text(
                text = "Earning Activities",
                color = TextLight,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickNavButton(
                    title = "Spin Wheel",
                    icon = Icons.Filled.Refresh,
                    color = TelegramBlue,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.DailyBonus) }
                )
                QuickNavButton(
                    title = "Badges",
                    icon = Icons.Filled.EmojiEvents,
                    color = AccentGold,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.Achievements) }
                )
                QuickNavButton(
                    title = "Leaders",
                    icon = Icons.Filled.Leaderboard,
                    color = AccentGreen,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.Leaderboard) }
                )
            }
        }

        // Daily Check-in Promo Block
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(TelegramBlue.copy(alpha = 0.3f), Color(0xFF1E1E38))
                        )
                    )
                    .border(1.dp, TelegramBlue.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .clickable { onNavigate(Screen.DailyBonus) }
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Daily Bonus & Streak!",
                            color = TextLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Claim daily check-in & spin the wheel.",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                    Button(
                        onClick = { onNavigate(Screen.DailyBonus) },
                        colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Claim Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Promo Code input Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SlateCard)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Apply Promo Code",
                    color = TextLight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = promoCodeText,
                        onValueChange = { promoCodeText = it },
                        placeholder = { Text("e.g. WELCOME10", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TelegramBlue,
                            unfocusedBorderColor = TextMuted,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.applyPromoCode(promoCodeText)
                            promoCodeText = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Apply")
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tip: Try WELCOME10 or TASKHUB50",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }

        // Global Announcements List
        item {
            Text(
                text = "Recent Notifications & News",
                color = TextLight,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        if (notifications.isEmpty()) {
            item {
                Text(
                    text = "No recent announcements.",
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                )
            }
        } else {
            items(notifications.take(5)) { alert ->
                NotificationRow(alert)
            }
        }
    }
}

@Composable
fun DashboardStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SlateCard),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.border(1.dp, HighDensityBorder, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                color = TextLight,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun QuickNavButton(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SlateCard)
            .border(1.dp, HighDensityBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            color = TextLight,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun NotificationRow(notification: NotificationEntity) {
    val icon = when (notification.type) {
        "Task Approved" -> Icons.Filled.CheckCircle
        "Task Rejected" -> Icons.Filled.Error
        "New Task" -> Icons.Filled.Star
        "Bonus" -> Icons.Filled.CardGiftcard
        "Referral Reward" -> Icons.Filled.People
        "Withdrawal Update" -> Icons.Filled.Payment
        else -> Icons.Filled.Campaign
    }

    val iconColor = when (notification.type) {
        "Task Approved" -> AccentGreen
        "Task Rejected" -> Color.Red
        "New Task" -> AccentGold
        "Bonus" -> AccentGold
        "Referral Reward" -> TelegramBlue
        "Withdrawal Update" -> AccentGreen
        else -> TelegramBlue
    }

    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SlateCard)
            .border(1.dp, HighDensityBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = notification.title,
                color = TextLight,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = notification.message,
                color = TextMuted,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun MajesticBalanceCard(
    totalBalance: Double,
    pendingBalance: Double,
    totalEarned: Double,
    modifier: Modifier = Modifier
) {
    // Dynamic tier based on total earned
    val tier = when {
        totalEarned >= 500 -> "Diamond Tier"
        totalEarned >= 100 -> "Gold Tier"
        totalEarned >= 50 -> "Silver Tier"
        else -> "Bronze Tier"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFD0BCFF), // Light violet
                        Color(0xFF381E72)  // Dark deep violet
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "TOTAL BALANCE",
                        color = Color(0xFF381E72).copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = String.format("%.2f", totalBalance),
                            color = Color(0xFF381E72),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "COINS",
                            color = Color(0xFF381E72),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }

                // Tier badge
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = tier,
                        color = Color(0xFF381E72),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column {
                    Text(
                        text = "PENDING BALANCE",
                        color = Color(0xFF381E72).copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "+${String.format("%.2f", pendingBalance)}",
                        color = Color(0xFF381E72),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        text = "TOTAL EARNED",
                        color = Color(0xFF381E72).copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = String.format("%.2f", totalEarned),
                        color = Color(0xFF381E72),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
