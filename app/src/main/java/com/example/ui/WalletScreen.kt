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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TransactionEntity
import com.example.data.WithdrawalEntity

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WalletScreen(
    viewModel: TaskHubViewModel,
    onNavigate: (Screen) -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val withdrawals by viewModel.userWithdrawals.collectAsState()
    val transactions by viewModel.userTransactions.collectAsState()

    var withdrawAmount by remember { mutableStateOf("") }
    var accountAddress by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("Bkash") }

    val methods = listOf("Bkash", "Nagad", "Rocket", "Binance Pay", "USDT")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Balances Header
        item {
            currentUser?.let { user ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DashboardStatCard(
                        title = "Available Balance",
                        value = "${String.format("%.2f", user.totalBalance)} BDT",
                        icon = Icons.Filled.AccountBalanceWallet,
                        tint = AccentGold,
                        modifier = Modifier.weight(1f)
                    )
                    DashboardStatCard(
                        title = "Pending Balance",
                        value = "${String.format("%.2f", user.pendingBalance)} BDT",
                        icon = Icons.Filled.HourglassEmpty,
                        tint = TextMuted,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Cashout Form
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SlateCard)
                    .border(1.dp, HighDensityBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Request Manual Withdrawal",
                    color = TextLight,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Method selection flow
                Text("Select Method", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    methods.forEach { method ->
                        val isSelected = selectedMethod == method
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) TelegramBlue else Color.Transparent)
                                .border(1.dp, if (isSelected) TelegramBlue else HighDensityBorder, RoundedCornerShape(6.dp))
                                .clickable { selectedMethod = method }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = method,
                                color = if (isSelected) ColorPrimaryOnText else TextLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Account / Wallet details input
                Text("Account Number / Wallet ID / Binance UID", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = accountAddress,
                    onValueChange = { accountAddress = it },
                    placeholder = { Text("e.g. 017XXXXXXXX / Binance ID", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TelegramBlue,
                        unfocusedBorderColor = HighDensityBorder,
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Withdrawal Amount
                Text("Withdrawal Amount (BDT)", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = withdrawAmount,
                    onValueChange = { withdrawAmount = it },
                    placeholder = { Text("Minimum 20.0 BDT", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TelegramBlue,
                        unfocusedBorderColor = HighDensityBorder,
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Submit Cashout trigger
                Button(
                    onClick = {
                        val amt = withdrawAmount.toDoubleOrNull()
                        if (amt != null && accountAddress.isNotEmpty()) {
                            viewModel.requestWithdraw(amt, selectedMethod, accountAddress)
                            withdrawAmount = ""
                            accountAddress = ""
                        } else {
                            // trigger VM warning
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Request Cashout (2% Fee)", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Withdraw History Header
        item {
            Text(
                text = "Withdrawal Requests",
                color = TextLight,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (withdrawals.isEmpty()) {
            item {
                Text(
                    text = "No withdrawal history.",
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                )
            }
        } else {
            items(withdrawals) { wr ->
                WithdrawalRow(wr = wr)
            }
        }

        // Ledger Statement list header
        item {
            Text(
                text = "Transaction History Ledger",
                color = TextLight,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (transactions.isEmpty()) {
            item {
                Text(
                    text = "No transactions recorded yet.",
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                )
            }
        } else {
            items(transactions) { txn ->
                TransactionRow(txn = txn)
            }
        }
    }
}

@Composable
fun WithdrawalRow(wr: WithdrawalEntity) {
    val statusColor = when (wr.status) {
        "Approved" -> AccentGreen
        "Rejected" -> Color.Red
        else -> AccentGold
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SlateCard)
            .border(1.dp, HighDensityBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = "${wr.method} Cashout",
                color = TextLight,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = "To: ${wr.accountAddress}",
                color = TextMuted,
                fontSize = 11.sp
            )
            Text(
                text = "Fee deducted: ${String.format("%.2f", wr.fee)} BDT",
                color = TextMuted,
                fontSize = 10.sp
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${String.format("%.2f", wr.amount)} BDT",
                color = TextLight,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = statusColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.2f))
            ) {
                Text(
                    text = wr.status,
                    color = statusColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun TransactionRow(txn: TransactionEntity) {
    val isPositive = txn.amount > 0
    val amtColor = if (isPositive) AccentGreen else Color.Red
    val amtSign = if (isPositive) "+" else ""

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SlateCard)
            .border(1.dp, HighDensityBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = txn.description,
                color = TextLight,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
            Text(
                text = txn.type,
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "$amtSign${String.format("%.2f", txn.amount)} BDT",
            color = amtColor,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}
