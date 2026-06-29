package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.data.SubmissionEntity
import com.example.data.TaskEntity
import com.example.data.TaskStep
import com.example.data.UserEntity
import com.example.data.WithdrawalEntity

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminPanelScreen(
    viewModel: TaskHubViewModel,
    onNavigate: (Screen) -> Unit
) {
    val isAdminLocked by viewModel.isAdminLocked.collectAsState()

    if (isAdminLocked) {
        var passwordInput by remember { mutableStateOf("") }
        var showError by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(SlateCard)
                    .border(1.dp, HighDensityBorder, RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Admin Lock",
                    tint = Color.Red,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "System Locked",
                    color = TextLight,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Please enter your administrative password to access the Control Center.",
                    color = TextMuted,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        showError = false
                    },
                    placeholder = { Text("Password", color = TextMuted) },
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Red,
                        unfocusedBorderColor = HighDensityBorder,
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (showError) {
                    Text(
                        text = "Incorrect password! Try again.",
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        val success = viewModel.unlockAdmin(passwordInput)
                        if (!success) {
                            showError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Unlock Admin Control", fontWeight = FontWeight.Bold, color = TextLight)
                }
            }
        }
        return
    }

    val allUsers by viewModel.allUsers.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val allSubmissions by viewModel.allSubmissions.collectAsState()
    val allWithdrawals by viewModel.allWithdrawals.collectAsState()

    var activeTab by remember { mutableStateOf("Submissions") }
    val tabs = listOf("Submissions", "Tasks", "Users", "Withdrawals", "Broadcast")

    // Stats
    val pendingSubmissionsCount = allSubmissions.count { it.status == "Pending Review" }
    val pendingWithdrawalsCount = allWithdrawals.count { it.status == "Pending" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats Overview header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SlateCard)
                    .border(1.dp, HighDensityBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "System Admin Portal",
                            color = Color.Red,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Control Center Overview",
                            color = TextLight,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = { viewModel.lockAdmin() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Lock, contentDescription = "Lock", tint = Color.Red, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Lock", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AdminSummaryStatCard(title = "Submissions", count = "$pendingSubmissionsCount pending", modifier = Modifier.weight(1f))
                    AdminSummaryStatCard(title = "Withdrawals", count = "$pendingWithdrawalsCount pending", modifier = Modifier.weight(1f))
                    AdminSummaryStatCard(title = "Users Active", count = "${allUsers.size} users", modifier = Modifier.weight(1f))
                }
            }
        }

        // Sub Navigation scrollable tabs
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(tabs) { tab ->
                    val isSelected = activeTab == tab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color.Red else SlateCard)
                            .border(1.dp, if (isSelected) Color.Red else HighDensityBorder, RoundedCornerShape(8.dp))
                            .clickable { activeTab = tab }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = tab,
                            color = TextLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Router depending on tabs
        when (activeTab) {
            "Submissions" -> {
                val pendSub = allSubmissions.filter { it.status == "Pending Review" }
                if (pendSub.isEmpty()) {
                    item {
                        Text(
                            text = "No pending task reviews remaining! Great job.",
                            color = TextMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(32.dp)
                        )
                    }
                } else {
                    items(pendSub) { sub ->
                        AdminSubmissionReviewCard(sub = sub, viewModel = viewModel)
                    }
                }
            }

            "Tasks" -> {
                item {
                    // Create Task trigger box
                    var showCreateDialog by remember { mutableStateOf(false) }
                    Button(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = "Add")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create Custom Earning Task", fontWeight = FontWeight.Bold)
                    }

                    if (showCreateDialog) {
                        AdminCreateTaskDialog(
                            onDismiss = { showCreateDialog = false },
                            onCreate = { title, reward, slots, type, time, diff, cat, steps ->
                                viewModel.createTask(title, reward, slots, type, time, diff, cat, steps)
                                showCreateDialog = false
                            }
                        )
                    }
                }

                items(allTasks) { task ->
                    AdminTaskManagementCard(task = task, viewModel = viewModel)
                }
            }

            "Users" -> {
                items(allUsers) { user ->
                    AdminUserManagementRow(user = user, viewModel = viewModel)
                }
            }

            "Withdrawals" -> {
                val pendingWr = allWithdrawals.filter { it.status == "Pending" }
                if (pendingWr.isEmpty()) {
                    item {
                        Text(
                            text = "No pending withdrawal requests.",
                            color = TextMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(32.dp)
                        )
                    }
                } else {
                    items(pendingWr) { wr ->
                        AdminWithdrawalReviewCard(wr = wr, viewModel = viewModel)
                    }
                }
            }

            "Broadcast" -> {
                item {
                    AdminBroadcastCard(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AdminSummaryStatCard(
    title: String,
    count: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.2f))
            .border(1.dp, HighDensityBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Text(text = title, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = count, color = TextLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AdminSubmissionReviewCard(
    sub: SubmissionEntity,
    viewModel: TaskHubViewModel
) {
    var isRejectExpanded by remember { mutableStateOf(false) }
    var rejectReason by remember { mutableStateOf("") }

    Card(
        colors = CardDefaults.cardColors(containerColor = SlateCard),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, HighDensityBorder, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // User details
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(text = "@${sub.username}", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "Telegram ID: ${sub.telegramId}", color = TextMuted, fontSize = 11.sp)
                }
                Surface(
                    color = TelegramBlue.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "+${sub.taskReward} BDT",
                        color = TelegramBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Task title
            Text(text = "Completed Task: ${sub.taskTitle}", color = TextLight, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            if (sub.userNote.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "User Note: \"${sub.userNote}\"", color = AccentGold, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))

            // Scrollable row of proof screenshots
            val screenshotsList = remember(sub.screenshotsJson) {
                if (sub.screenshotsJson.isEmpty()) emptyList() else sub.screenshotsJson.split("|||")
            }
            Text(text = "Submitted Proofs (${screenshotsList.size}):", color = TextLight, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(screenshotsList.size) { idx ->
                    val url = screenshotsList[idx]
                    var showZoomDialog by remember { mutableStateOf(false) }

                    Box(
                        modifier = Modifier
                            .size(100.dp, 120.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.3f))
                            .border(1.dp, TelegramBlue.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .clickable { showZoomDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(4.dp)) {
                            Icon(imageVector = Icons.Filled.AddAPhoto, contentDescription = "Screenshot", tint = TelegramBlue, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Screenshot #${idx + 1}", color = TextLight, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "Click to Zoom", color = TextMuted, fontSize = 8.sp, textAlign = TextAlign.Center)
                        }
                    }

                    if (showZoomDialog) {
                        AlertDialog(
                            onDismissRequest = { showZoomDialog = false },
                            title = { Text("Screenshot Proof #${idx + 1}", color = TextLight, fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                            text = {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(300.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Black),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Filled.Image, contentDescription = "Proof", tint = TextMuted, modifier = Modifier.size(64.dp))
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = url, color = TextMuted, fontSize = 10.sp, maxLines = 1)
                                }
                            },
                            confirmButton = {
                                Button(onClick = { showZoomDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue)) {
                                    Text("Close")
                                }
                            },
                            containerColor = SlateCard
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (!isRejectExpanded) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.approveSubmission(sub.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Approve", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Button(
                        onClick = { isRejectExpanded = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reject", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            } else {
                Column {
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        placeholder = { Text("Reason for rejection/resubmission...", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Red,
                            unfocusedBorderColor = Color.DarkGray,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (rejectReason.isNotEmpty()) {
                                    viewModel.rejectSubmission(sub.id, rejectReason)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Confirm Reject", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                if (rejectReason.isNotEmpty()) {
                                    viewModel.requestResubmit(sub.id, rejectReason)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Request Resubmit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminTaskManagementCard(
    task: TaskEntity,
    viewModel: TaskHubViewModel
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SlateCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SlateCard.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(text = task.title, color = TextLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = "Remaining Slots: ${task.remainingSlots} / ${task.totalSlots}", color = TextMuted, fontSize = 11.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Status badge
                    Surface(
                        color = if (task.isPaused) Color.Red.copy(alpha = 0.15f) else AccentGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (task.isPaused) "Paused" else "Active",
                            color = if (task.isPaused) Color.Red else AccentGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { viewModel.updateTaskStatus(task, !task.isPaused) },
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.2f), CircleShape)
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = if (task.isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                        contentDescription = "Pause",
                        tint = TextLight,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = { viewModel.duplicateTask(task) },
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.2f), CircleShape)
                        .size(32.dp)
                ) {
                    Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = "Copy", tint = TextLight, modifier = Modifier.size(16.dp))
                }
                IconButton(
                    onClick = { viewModel.deleteTask(task) },
                    modifier = Modifier
                        .background(Color.Red.copy(alpha = 0.2f), CircleShape)
                        .size(32.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun AdminUserManagementRow(
    user: UserEntity,
    viewModel: TaskHubViewModel
) {
    var balanceAdjustment by remember { mutableStateOf("") }
    var adjustmentReason by remember { mutableStateOf("Manual Adjustment") }

    Card(
        colors = CardDefaults.cardColors(containerColor = SlateCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, HighDensityBorder, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: User Info and Ban Control
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(text = "@${user.username}", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "Telegram ID: ${user.telegramId}", color = TextMuted, fontSize = 11.sp)
                }
                
                Button(
                    onClick = { viewModel.updateUserBanned(user, !user.isBanned) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (user.isBanned) AccentGreen else Color.Red
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(28.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text(
                        text = if (user.isBanned) "Unban User" else "Ban User",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Financial Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Total Earned", color = TextMuted, fontSize = 10.sp)
                    Text(text = "৳${String.format("%.2f", user.totalEarned)}", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Current Balance", color = TextMuted, fontSize = 10.sp)
                    Text(text = "৳${String.format("%.2f", user.totalBalance)}", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Divider(color = HighDensityBorder.copy(alpha = 0.5f), thickness = 1.dp)

            // Financial Adjustment Controls
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = "Adjust User Balance", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = balanceAdjustment,
                        onValueChange = { balanceAdjustment = it },
                        placeholder = { Text("Amount", color = TextMuted, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TelegramBlue,
                            unfocusedBorderColor = HighDensityBorder,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight
                        ),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    )

                    Button(
                        onClick = {
                            val amt = balanceAdjustment.toDoubleOrNull()
                            if (amt != null && amt > 0) {
                                viewModel.adjustUserBalance(user, amt, adjustmentReason)
                                balanceAdjustment = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("+ BDT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val amt = balanceAdjustment.toDoubleOrNull()
                            if (amt != null && amt > 0) {
                                viewModel.adjustUserBalance(user, -amt, adjustmentReason)
                                balanceAdjustment = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("- BDT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedTextField(
                    value = adjustmentReason,
                    onValueChange = { adjustmentReason = it },
                    placeholder = { Text("Adjustment Reason (optional)", color = TextMuted, fontSize = 11.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TelegramBlue,
                        unfocusedBorderColor = HighDensityBorder,
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp)
                )
            }
        }
    }
}

@Composable
fun AdminWithdrawalReviewCard(
    wr: WithdrawalEntity,
    viewModel: TaskHubViewModel
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SlateCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SlateCard.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "Method: ${wr.method}", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(text = "Account: ${wr.accountAddress}", color = TextMuted, fontSize = 11.sp)
            Text(text = "Amount: ${wr.amount} BDT (Fee: ${wr.fee})", color = AccentGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.approveWithdrawal(wr.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Approve Payment", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { viewModel.rejectWithdrawal(wr.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reject & Refund", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AdminBroadcastCard(
    viewModel: TaskHubViewModel
) {
    var title by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SlateCard)
            .padding(12.dp)
    ) {
        Text(text = "Broadcast Global Announcement", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { Text("Announcement Title...", color = TextMuted) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Red,
                unfocusedBorderColor = Color.DarkGray,
                focusedTextColor = TextLight,
                unfocusedTextColor = TextLight
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("Details of announcement announcement text...", color = TextMuted) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Red,
                unfocusedBorderColor = Color.DarkGray,
                focusedTextColor = TextLight,
                unfocusedTextColor = TextLight
            ),
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            shape = RoundedCornerShape(8.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (title.isNotEmpty() && text.isNotEmpty()) {
                    viewModel.broadcastAnnouncement(title, text)
                    title = ""
                    text = ""
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Broadcast News", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AdminCreateTaskDialog(
    onDismiss: () -> Unit,
    onCreate: (String, Double, Int, String, String, String, String, List<TaskStep>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var reward by remember { mutableStateOf("") }
    var slots by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("YouTube Like") }
    var time by remember { mutableStateOf("2 min") }
    var difficulty by remember { mutableStateOf("Easy") }
    var category by remember { mutableStateOf("Social") }

    // Custom steps list state
    val customSteps = remember { mutableStateListOf<TaskStep>() }
    var currentStepText by remember { mutableStateOf("") }
    var currentStepType by remember { mutableStateOf("text") }
    var currentStepImgUrl by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Task", color = TextLight, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(androidx.compose.foundation.rememberScrollState())
                    .padding(vertical = 4.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight, focusedBorderColor = TelegramBlue),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = reward,
                        onValueChange = { reward = it },
                        label = { Text("Reward (BDT)", color = TextMuted, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight, focusedBorderColor = TelegramBlue),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = slots,
                        onValueChange = { slots = it },
                        label = { Text("Total Slots", color = TextMuted, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight, focusedBorderColor = TelegramBlue),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category", color = TextMuted, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight, focusedBorderColor = TelegramBlue),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = type,
                        onValueChange = { type = it },
                        label = { Text("Task Type / Interaction", color = TextMuted, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight, focusedBorderColor = TelegramBlue),
                        modifier = Modifier.weight(1f)
                    )
                }

                Divider(color = HighDensityBorder.copy(alpha = 0.5f), thickness = 1.dp)

                // Instruction steps builder section
                Text(
                    text = "Build Instructions Steps",
                    color = AccentGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                // List of existing steps
                if (customSteps.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(6.dp)
                    ) {
                        customSteps.forEachIndexed { idx, step ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${idx + 1}. [${step.type.uppercase()}] ${step.text}",
                                    color = TextLight,
                                    fontSize = 11.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { customSteps.removeAt(idx) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }

                // Add step inputs
                OutlinedTextField(
                    value = currentStepText,
                    onValueChange = { currentStepText = it },
                    label = { Text("Step Details & Actions...", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight, focusedBorderColor = TelegramBlue),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Style:", color = TextMuted, fontSize = 11.sp)
                    listOf("text", "bold", "warning", "note").forEach { style ->
                        val isSel = currentStepType == style
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSel) TelegramBlue else Color.Black.copy(alpha = 0.3f))
                                .clickable { currentStepType = style }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(text = style, color = TextLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = currentStepImgUrl,
                    onValueChange = { currentStepImgUrl = it },
                    label = { Text("Hint/Screenshot URL (Optional)", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight, focusedBorderColor = TelegramBlue),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (currentStepText.isNotEmpty()) {
                            val newStepNum = customSteps.size + 1
                            customSteps.add(
                                TaskStep(
                                    stepNumber = newStepNum,
                                    text = currentStepText,
                                    type = currentStepType,
                                    imageResOrUrl = if (currentStepImgUrl.isNotEmpty()) currentStepImgUrl else null
                                )
                            )
                            currentStepText = ""
                            currentStepImgUrl = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("+ Add Step", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val r = reward.toDoubleOrNull() ?: 5.0
                    val s = slots.toIntOrNull() ?: 100
                    val finalSteps = if (customSteps.isEmpty()) {
                        listOf(
                            TaskStep(1, "Open the customized target link.", "text"),
                            TaskStep(2, "Complete target interaction (Like/Join/Subscribe).", "bold"),
                            TaskStep(3, "Take proof screenshots showing success.", "warning"),
                            TaskStep(4, "Submit screenshot on proof section.", "note")
                        )
                    } else {
                        customSteps.toList()
                    }
                    onCreate(title, r, s, type, time, difficulty, category, finalSteps)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Create Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        },
        containerColor = SlateCard
    )
}
