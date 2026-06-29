package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest

// High Density Theme Colors
val TelegramBlue = Color(0xFFD0BCFF) // Map TelegramBlue to the new primary accent (D0BCFF)
val SlateBackground = Color(0xFF0F1115) // Deep slate/black background
val SlateCard = Color(0xFF1C1B1F) // Container background
val AccentGold = Color(0xFFF59E0B)
val AccentGreen = Color(0xFF10B981)
val TextLight = Color(0xFFE3E2E6)
val TextMuted = Color(0xFF938F99)

val ColorPrimaryOnText = Color(0xFF381E72)
val AccentRed = Color(0xFFF2B8B5)
val HighDensityBorder = Color.White.copy(alpha = 0.05f)

sealed class Screen {
    object UserDashboard : Screen()
    object TasksList : Screen()
    data class TaskDetails(val taskId: Int) : Screen()
    object Wallet : Screen()
    object Referrals : Screen()
    object DailyBonus : Screen()
    object Achievements : Screen()
    object Leaderboard : Screen()
    object AdminHome : Screen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskHubApp(
    viewModel: TaskHubViewModel = viewModel()
) {
    val currentScreenState = remember { mutableStateOf<Screen>(Screen.UserDashboard) }
    var currentScreen by currentScreenState
    val isAdminMode by viewModel.isAdminMode.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val activeNotification by viewModel.activeNotification.collectAsState()
    val toastState = remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = true) {
        viewModel.toastMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Modern glassmorphic gradient background
    val bgGradient = Brush.verticalGradient(
        colors = listOf(SlateBackground, Color(0xFF07080A))
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = TextLight
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(end = 16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Hub,
                                contentDescription = "Logo",
                                tint = TelegramBlue,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TaskHub",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextLight
                            )
                            if (isAdminMode) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = Color.Red.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.border(1.dp, Color.Red, RoundedCornerShape(4.dp))
                                ) {
                                    Text(
                                        text = "ADMIN",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Red,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Profile / Role Switcher
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(SlateCard)
                                .border(1.dp, HighDensityBorder, RoundedCornerShape(20.dp))
                                .clickable {
                                    viewModel.toggleRoleMode()
                                    // Reset to standard dashboards when changing role
                                    if (isAdminMode) {
                                        currentScreen = Screen.UserDashboard
                                    } else {
                                        currentScreen = Screen.AdminHome
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (isAdminMode) Icons.Filled.Shield else Icons.Filled.Person,
                                contentDescription = "Role",
                                tint = if (isAdminMode) Color.Red else TelegramBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isAdminMode) "To User" else "To Admin",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextLight
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (!isAdminMode) {
                NavigationBar(
                    containerColor = SlateCard.copy(alpha = 0.95f),
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .border(1.dp, HighDensityBorder, RoundedCornerShape(0.dp))
                ) {
                    val navItems = listOf(
                        Triple(Screen.UserDashboard, Icons.Filled.Dashboard, "Home"),
                        Triple(Screen.TasksList, Icons.Filled.ListAlt, "Tasks"),
                        Triple(Screen.Wallet, Icons.Filled.AccountBalanceWallet, "Wallet"),
                        Triple(Screen.Referrals, Icons.Filled.People, "Ref"),
                        Triple(Screen.DailyBonus, Icons.Filled.CardGiftcard, "Bonus")
                    )

                    navItems.forEach { (screen, icon, label) ->
                        val isSelected = when (screen) {
                            Screen.UserDashboard -> currentScreen is Screen.UserDashboard
                            Screen.TasksList -> currentScreen is Screen.TasksList || currentScreen is Screen.TaskDetails
                            Screen.Wallet -> currentScreen is Screen.Wallet
                            Screen.Referrals -> currentScreen is Screen.Referrals
                            Screen.DailyBonus -> currentScreen is Screen.DailyBonus
                            else -> false
                        }

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentScreen = screen },
                            icon = { Icon(imageVector = icon, contentDescription = label) },
                            label = { Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TelegramBlue,
                                unselectedIconColor = TextMuted,
                                selectedTextColor = TelegramBlue,
                                unselectedTextColor = TextMuted,
                                indicatorColor = TelegramBlue.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            } else {
                // Admin navigation bottom bar
                NavigationBar(
                    containerColor = SlateCard.copy(alpha = 0.95f),
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .border(1.dp, HighDensityBorder, RoundedCornerShape(0.dp))
                ) {
                    val adminItems = listOf(
                        Triple(Screen.AdminHome, Icons.Filled.Dashboard, "Admin Panel"),
                        Triple(Screen.TasksList, Icons.Filled.ListAlt, "User Tasks"),
                        Triple(Screen.Leaderboard, Icons.Filled.Leaderboard, "Leaderboard"),
                        Triple(Screen.Achievements, Icons.Filled.EmojiEvents, "Badges")
                    )

                    adminItems.forEach { (screen, icon, label) ->
                        val isSelected = currentScreen == screen

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentScreen = screen },
                            icon = { Icon(imageVector = icon, contentDescription = label) },
                            label = { Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Red,
                                unselectedIconColor = TextMuted,
                                selectedTextColor = Color.Red,
                                unselectedTextColor = TextMuted,
                                indicatorColor = Color.Red.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient)
                .padding(innerPadding)
        ) {
            // Screen router with smooth transitions
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "ScreenTransition"
            ) { targetScreen ->
                when (targetScreen) {
                    is Screen.UserDashboard -> UserDashboardScreen(
                        viewModel = viewModel,
                        onNavigate = { currentScreen = it }
                    )
                    is Screen.TasksList -> TasksListScreen(
                        viewModel = viewModel,
                        onNavigate = { currentScreen = it }
                    )
                    is Screen.TaskDetails -> TaskDetailScreen(
                        taskId = targetScreen.taskId,
                        viewModel = viewModel,
                        onNavigate = { currentScreen = it }
                    )
                    is Screen.Wallet -> WalletScreen(
                        viewModel = viewModel,
                        onNavigate = { currentScreen = it }
                    )
                    is Screen.Referrals -> ReferralsScreen(
                        viewModel = viewModel
                    )
                    is Screen.DailyBonus -> DailyBonusScreen(
                        viewModel = viewModel
                    )
                    is Screen.Achievements -> AchievementsScreen(
                        viewModel = viewModel
                    )
                    is Screen.Leaderboard -> LeaderboardScreen(
                        viewModel = viewModel
                    )
                    is Screen.AdminHome -> AdminPanelScreen(
                        viewModel = viewModel,
                        onNavigate = { currentScreen = it }
                    )
                }
            }
        }
    }

    // Floating global notifications
    AnimatedVisibility(
        visible = activeNotification != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = Modifier
            .align(Alignment.TopCenter)
            .zIndex(99f)
    ) {
        activeNotification?.let { notif ->
            GlobalNotificationOverlay(
                notification = notif,
                onDismiss = { viewModel.dismissGlobalNotification() }
            )
        }
    }
}
}

@Composable
fun GlobalNotificationOverlay(
    notification: GlobalNotification,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = SlateCard.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .border(
                1.dp,
                Brush.linearGradient(
                    colors = listOf(
                        TelegramBlue.copy(alpha = 0.8f),
                        Color(0xFF8B5CF6).copy(alpha = 0.8f)
                    )
                ),
                RoundedCornerShape(16.dp)
            )
            .clickable { onDismiss() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when (notification.type) {
                            NotificationType.SUCCESS -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                            NotificationType.WARNING -> Color.Red.copy(alpha = 0.15f)
                            else -> TelegramBlue.copy(alpha = 0.15f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (notification.type) {
                        NotificationType.SUCCESS -> Icons.Filled.CheckCircle
                        NotificationType.WARNING -> Icons.Filled.Warning
                        else -> Icons.Filled.Info
                    },
                    contentDescription = null,
                    tint = when (notification.type) {
                        NotificationType.SUCCESS -> Color(0xFF4CAF50)
                        NotificationType.WARNING -> Color.Red
                        else -> TelegramBlue
                    },
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    color = TextLight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = notification.message,
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
