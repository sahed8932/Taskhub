package com.example.ui

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ReferralsScreen(
    viewModel: TaskHubViewModel
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()

    var referralCopied by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats Overview Card
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
                        text = "3-Level Referral System",
                        color = TelegramBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Referral Dashboard",
                        color = TextLight,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Referrals", color = TextMuted, fontSize = 11.sp)
                            Text(
                                text = "${user.referralsL1 + user.referralsL2 + user.referralsL3}",
                                color = TextLight,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Referral Income", color = TextMuted, fontSize = 11.sp)
                            Text(
                                text = "${String.format("%.2f", user.referralEarnings)} Coins",
                                color = AccentGreen,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // Multi levels stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReferralLevelStatCard(level = "Level 1 (5%)", count = user.referralsL1, modifier = Modifier.weight(1f))
                        ReferralLevelStatCard(level = "Level 2 (5%)", count = user.referralsL2, modifier = Modifier.weight(1f))
                        ReferralLevelStatCard(level = "Level 3 (2%)", count = user.referralsL3, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Copy Referral Link Card
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
                        text = "Your Referral Link",
                        color = TextLight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val refLink = "https://t.me/TaskHubBot?start=${user.telegramId}"

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.2f))
                            .border(1.dp, HighDensityBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = refLink,
                            color = TextLight,
                            fontSize = 11.sp,
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (referralCopied) Icons.Filled.Check else Icons.Filled.ContentCopy,
                            contentDescription = "Copy",
                            tint = if (referralCopied) AccentGreen else TelegramBlue,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable {
                                    referralCopied = true
                                    // Simulation delay resetting icon
                                }
                        )
                    }
                    if (referralCopied) {
                        Text(
                            text = "Link copied to clipboard!",
                            color = AccentGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // Referral Tree Structure Card
        item {
            Text(
                text = "Referral Tree Structure",
                color = TextLight,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

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
                    if (user.referralTreeJson.isEmpty()) {
                        Text(
                            text = "Your referral tree is currently empty. Invite members to expand your hierarchy!",
                            color = TextMuted,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(8.dp)
                        )
                    } else {
                        // Display hierarchical interactive node list
                        val chains = user.referralTreeJson.split(" | ")
                        chains.forEach { node ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(TelegramBlue)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = node,
                                    color = TextLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReferralLevelStatCard(
    level: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Black.copy(alpha = 0.2f))
            .border(1.dp, HighDensityBorder, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Text(text = level, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "$count users", color = TextLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
