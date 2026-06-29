package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class TaskHubViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application.applicationContext,
        TaskHubDatabase::class.java,
        "taskhub_db"
    ).fallbackToDestructiveMigration().build()

    private val dao = db.taskHubDao()
    private val repository = TaskHubRepository(dao)

    // Current session: "user" (jrshakibyt) or "admin" (admin_shakib)
    private val _isAdminMode = MutableStateFlow(false)
    val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

    // Admin locked state
    private val _isAdminLocked = MutableStateFlow(true)
    val isAdminLocked: StateFlow<Boolean> = _isAdminLocked.asStateFlow()

    fun unlockAdmin(password: String): Boolean {
        return if (password == AdminConfig.ADMIN_PASSWORD) {
            _isAdminLocked.value = false
            true
        } else {
            false
        }
    }

    fun lockAdmin() {
        _isAdminLocked.value = true
    }

    // Logged in User state
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Selected task for detail/submission
    private val _selectedTask = MutableStateFlow<TaskEntity?>(null)
    val selectedTask: StateFlow<TaskEntity?> = _selectedTask.asStateFlow()

    // Flows from Repository
    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeTasks: StateFlow<List<TaskEntity>> = repository.activeTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSubmissions: StateFlow<List<SubmissionEntity>> = repository.allSubmissions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWithdrawals: StateFlow<List<WithdrawalEntity>> = repository.allWithdrawals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active User Specific Flows
    private val _userSubmissions = MutableStateFlow<List<SubmissionEntity>>(emptyList())
    val userSubmissions: StateFlow<List<SubmissionEntity>> = _userSubmissions.asStateFlow()

    private val _userWithdrawals = MutableStateFlow<List<WithdrawalEntity>>(emptyList())
    val userWithdrawals: StateFlow<List<WithdrawalEntity>> = _userWithdrawals.asStateFlow()

    private val _userNotifications = MutableStateFlow<List<NotificationEntity>>(emptyList())
    val userNotifications: StateFlow<List<NotificationEntity>> = _userNotifications.asStateFlow()

    private val _userTransactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val userTransactions: StateFlow<List<TransactionEntity>> = _userTransactions.asStateFlow()

    private val _userAchievements = MutableStateFlow<List<AchievementEntity>>(emptyList())
    val userAchievements: StateFlow<List<AchievementEntity>> = _userAchievements.asStateFlow()

    // Search and filter parameters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedFilter = MutableStateFlow("Newest") // "Newest", "Highest Reward", "Most Popular", "Difficulty"
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    // Filtered Tasks list
    val filteredTasks: StateFlow<List<TaskEntity>> = combine(
        activeTasks, searchQuery, selectedCategory, selectedFilter
    ) { tasks, query, category, filter ->
        var list = tasks
        if (query.isNotEmpty()) {
            list = list.filter { it.title.contains(query, ignoreCase = true) || it.taskType.contains(query, ignoreCase = true) }
        }
        if (category != "All") {
            list = list.filter { it.category.equals(category, ignoreCase = true) }
        }
        when (filter) {
            "Highest Reward" -> list.sortedByDescending { it.reward }
            "Difficulty" -> list.sortedByDescending { when(it.difficulty) { "Hard" -> 3; "Medium" -> 2; else -> 1 } }
            "Most Popular" -> list.sortedBy { it.remainingSlots } // fewer remaining slots = more popular
            else -> list.sortedByDescending { it.id } // Newest
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Toast Messages or events
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    // Accepted Task IDs in current session
    private val _acceptedTaskIds = MutableStateFlow<Set<Int>>(emptySet())
    val acceptedTaskIds: StateFlow<Set<Int>> = _acceptedTaskIds.asStateFlow()

    fun acceptTask(taskId: Int) {
        val current = _acceptedTaskIds.value
        if (taskId !in current) {
            _acceptedTaskIds.value = current + taskId
            viewModelScope.launch {
                val task = repository.getTaskById(taskId)
                val title = task?.title ?: "Task"
                showGlobalNotification(
                    title = "Task Accepted! 🎉",
                    message = "You have accepted '$title'. Follow the instructions to complete it.",
                    type = NotificationType.SUCCESS
                )
            }
        }
    }

    // Custom Global Success/Info Notifications
    private val _activeNotification = MutableStateFlow<GlobalNotification?>(null)
    val activeNotification: StateFlow<GlobalNotification?> = _activeNotification.asStateFlow()

    fun showGlobalNotification(title: String, message: String, type: NotificationType = NotificationType.SUCCESS) {
        viewModelScope.launch {
            _activeNotification.value = GlobalNotification(title, message, type)
            kotlinx.coroutines.delay(4000)
            if (_activeNotification.value?.title == title) {
                _activeNotification.value = null
            }
        }
    }

    fun dismissGlobalNotification() {
        _activeNotification.value = null
    }

    init {
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
            val prefs = getApplication<Application>().getSharedPreferences("taskhub_prefs", android.content.Context.MODE_PRIVATE)
            val savedId = prefs.getString("telegram_id", "738291032") ?: "738291032"
            val savedUsername = prefs.getString("username", "jrshakibyt") ?: "jrshakibyt"
            
            var user = repository.getUserByTelegramId(savedId)
            if (user == null) {
                // Auto create account with Telegram ID
                val newUser = UserEntity(
                    telegramId = savedId,
                    username = savedUsername,
                    totalBalance = 145.0, // Initial balance in BDT
                    pendingBalance = 25.0,
                    totalEarned = 170.0,
                    role = if (savedId == "1122334455") "admin" else "user"
                )
                val newId = repository.insertUser(newUser).toInt()
                user = newUser.copy(id = newId)
            }
            _currentUser.value = user
            observeUserData(user.id)
        }
    }

    fun loginWithTelegram(telegramId: String, username: String) {
        viewModelScope.launch {
            val prefs = getApplication<Application>().getSharedPreferences("taskhub_prefs", android.content.Context.MODE_PRIVATE)
            prefs.edit()
                .putString("telegram_id", telegramId)
                .putString("username", username)
                .apply()

            var user = repository.getUserByTelegramId(telegramId)
            if (user == null) {
                val newUser = UserEntity(
                    telegramId = telegramId,
                    username = username,
                    totalBalance = 50.0, // Starter bonus in BDT
                    pendingBalance = 0.0,
                    totalEarned = 50.0,
                    role = if (telegramId == "1122334455") "admin" else "user"
                )
                val newId = repository.insertUser(newUser).toInt()
                user = newUser.copy(id = newId)
            }
            _currentUser.value = user
            observeUserData(user.id)
            showGlobalNotification(
                title = "Telegram Connected ⚡",
                message = "Logged in as @$username ($telegramId)",
                type = NotificationType.SUCCESS
            )
        }
    }

    private fun observeUserData(userId: Int) {
        viewModelScope.launch {
            repository.getSubmissionsForUser(userId).collect { _userSubmissions.value = it }
        }
        viewModelScope.launch {
            repository.getWithdrawalsForUser(userId).collect { _userWithdrawals.value = it }
        }
        viewModelScope.launch {
            repository.getNotificationsForUser(userId).collect { _userNotifications.value = it }
        }
        viewModelScope.launch {
            repository.getTransactionsForUser(userId).collect { _userTransactions.value = it }
        }
        viewModelScope.launch {
            repository.getAchievementsForUser(userId).collect { _userAchievements.value = it }
        }
    }

    fun toggleRoleMode() {
        viewModelScope.launch {
            val nextAdminMode = !_isAdminMode.value
            _isAdminMode.value = nextAdminMode
            if (!nextAdminMode) {
                lockAdmin()
            }
            val targetTelegramId = if (nextAdminMode) "1122334455" else "738291032"
            val targetUser = repository.getUserByTelegramId(targetTelegramId)
            if (targetUser != null) {
                _currentUser.value = targetUser
                observeUserData(targetUser.id)
                _toastMessage.emit("Switched to ${targetUser.role.uppercase()} panel.")
            }
        }
    }

    // --- Search / Filters ---
    fun updateSearchQuery(query: String) { _searchQuery.value = query }
    fun updateSelectedCategory(category: String) { _selectedCategory.value = category }
    fun updateSelectedFilter(filter: String) { _selectedFilter.value = filter }
    fun selectTask(task: TaskEntity?) { _selectedTask.value = task }

    // --- Daily Bonus & Check-ins ---
    fun claimDailyCheckIn() {
        val user = _currentUser.value ?: return
        if (user.isBanned) return
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        if (now - user.lastCheckIn < oneDayMs) {
            viewModelScope.launch { _toastMessage.emit("Already checked-in today! Try again tomorrow.") }
            return
        }

        viewModelScope.launch {
            val newStreak = if (now - user.lastCheckIn < 2 * oneDayMs) user.streakDays + 1 else 1
            val bonusReward = 5.0 + (newStreak * 0.5) // basic 5.0 coins + streak bonus
            val updatedUser = user.copy(
                totalBalance = user.totalBalance + bonusReward,
                totalEarned = user.totalEarned + bonusReward,
                streakDays = newStreak,
                lastCheckIn = now
            )
            repository.updateUser(updatedUser)
            _currentUser.value = updatedUser

            repository.insertTransaction(TransactionEntity(
                userId = user.id,
                amount = bonusReward,
                type = "Bonus Income",
                description = "Daily Check-in (Streak: $newStreak days)"
            ))

            repository.insertNotification(NotificationEntity(
                userId = user.id,
                title = "Daily Bonus Claimed!",
                message = "Earned $bonusReward BDT. Day $newStreak streak active!",
                type = "Bonus"
            ))

            _toastMessage.emit("Checked-in! Streak: $newStreak days. +$bonusReward BDT!")
            checkAchievements(user.id)
        }
    }

    fun spinWheel() {
        val user = _currentUser.value ?: return
        if (user.isBanned) return
        viewModelScope.launch {
            val rewards = listOf(1.0, 2.5, 5.0, 10.0, 15.0, 25.0, 50.0)
            val index = (rewards.indices).random()
            val winAmount = rewards[index]

            val updatedUser = user.copy(
                totalBalance = user.totalBalance + winAmount,
                totalEarned = user.totalEarned + winAmount
            )
            repository.updateUser(updatedUser)
            _currentUser.value = updatedUser

            repository.insertTransaction(TransactionEntity(
                userId = user.id,
                amount = winAmount,
                type = "Bonus Income",
                description = "Lucky Spin Wheel reward"
            ))

            repository.insertNotification(NotificationEntity(
                userId = user.id,
                title = "Lucky Spin Wheel!",
                message = "You won $winAmount BDT from the spin wheel!",
                type = "Bonus"
            ))

            _toastMessage.emit("Spin Result: Won $winAmount BDT!")
            checkAchievements(user.id)
        }
    }

    fun openLuckyBox() {
        val user = _currentUser.value ?: return
        if (user.isBanned) return
        viewModelScope.launch {
            val boxReward = (2..50).random().toDouble()
            val updatedUser = user.copy(
                totalBalance = user.totalBalance + boxReward,
                totalEarned = user.totalEarned + boxReward
            )
            repository.updateUser(updatedUser)
            _currentUser.value = updatedUser

            repository.insertTransaction(TransactionEntity(
                userId = user.id,
                amount = boxReward,
                type = "Bonus Income",
                description = "Lucky Box secret reward"
            ))

            repository.insertNotification(NotificationEntity(
                userId = user.id,
                title = "Lucky Box Opened!",
                message = "You opened a secret lucky box and found $boxReward BDT!",
                type = "Bonus"
            ))

            _toastMessage.emit("Lucky Box: Found $boxReward BDT!")
            checkAchievements(user.id)
        }
    }

    // --- Submit Task ---
    fun submitTask(taskId: Int, screenshots: List<String>, note: String) {
        val user = _currentUser.value ?: return
        if (user.isBanned) return
        viewModelScope.launch {
            val task = repository.getTaskById(taskId) ?: return@launch
            if (task.remainingSlots <= 0) {
                _toastMessage.emit("Task slots are full!")
                return@launch
            }

            val submission = SubmissionEntity(
                userId = user.id,
                telegramId = user.telegramId,
                username = user.username,
                taskId = task.id,
                taskTitle = task.title,
                taskReward = task.reward,
                screenshotsJson = screenshots.joinToString("|||"),
                userNote = note,
                status = "Pending Review"
            )

            repository.insertSubmission(submission)

            // Temporarily hold as pending balance
            val updatedUser = user.copy(
                pendingBalance = user.pendingBalance + task.reward
            )
            repository.updateUser(updatedUser)
            _currentUser.value = updatedUser

            repository.insertNotification(NotificationEntity(
                userId = user.id,
                title = "Task Submitted",
                message = "Your submission for '${task.title}' is pending review.",
                type = "Task"
            ))

            showGlobalNotification(
                title = "Task Completed! 🚀",
                message = "Your proof for '${task.title}' has been submitted successfully and is pending review.",
                type = NotificationType.SUCCESS
            )
            _toastMessage.emit("Submission received! Pending Review.")
        }
    }

    // --- Withdrawal Flow ---
    fun requestWithdraw(amount: Double, method: String, address: String) {
        val user = _currentUser.value ?: return
        if (user.isBanned) return
        viewModelScope.launch {
            if (amount < 20.0) {
                _toastMessage.emit("Minimum withdrawal is 20.0 BDT.")
                return@launch
            }
            if (amount > user.totalBalance) {
                _toastMessage.emit("Insufficient balance.")
                return@launch
            }

            val fee = amount * 0.02 // 2% fee
            val updatedUser = user.copy(
                totalBalance = user.totalBalance - amount
            )
            repository.updateUser(updatedUser)
            _currentUser.value = updatedUser

            val withdrawal = WithdrawalEntity(
                userId = user.id,
                amount = amount - fee,
                fee = fee,
                method = method,
                accountAddress = address,
                status = "Pending"
            )
            repository.insertWithdrawal(withdrawal)

            repository.insertTransaction(TransactionEntity(
                userId = user.id,
                amount = -amount,
                type = "Withdrawal",
                description = "Withdrawal request via $method (Fee: $fee)"
            ))

            repository.insertNotification(NotificationEntity(
                userId = user.id,
                title = "Withdrawal Pending",
                message = "Your request to withdraw ${amount - fee} BDT via $method is processing.",
                type = "Withdrawal Update"
            ))

            _toastMessage.emit("Withdrawal request submitted!")
        }
    }

    // --- Promo Code System ---
    fun applyPromoCode(code: String) {
        val user = _currentUser.value ?: return
        if (user.isBanned) return
        viewModelScope.launch {
            val amount = when (code.uppercase().trim()) {
                "WELCOME10" -> 10.0
                "TASKHUB50" -> 50.0
                "GOLDENCOIN" -> 100.0
                else -> 0.0
            }

            if (amount > 0.0) {
                // Check if already used by scanning transactions
                val alreadyUsed = _userTransactions.value.any { it.description.contains("Promo: $code", ignoreCase = true) }
                if (alreadyUsed) {
                    _toastMessage.emit("Promo code already used!")
                    return@launch
                }

                val updatedUser = user.copy(
                    totalBalance = user.totalBalance + amount,
                    totalEarned = user.totalEarned + amount
                )
                repository.updateUser(updatedUser)
                _currentUser.value = updatedUser

                repository.insertTransaction(TransactionEntity(
                    userId = user.id,
                    amount = amount,
                    type = "Bonus Income",
                    description = "Promo: $code bonus"
                ))

                repository.insertNotification(NotificationEntity(
                    userId = user.id,
                    title = "Promo Code Applied!",
                    message = "Successfully claimed $amount BDT from code: $code!",
                    type = "Bonus"
                ))

                _toastMessage.emit("Success! +$amount BDT added.")
            } else {
                _toastMessage.emit("Invalid promo code.")
            }
        }
    }

    // --- Admin Actions: Submissions Review ---
    fun approveSubmission(submissionId: Int) {
        viewModelScope.launch {
            val submission = repository.getSubmissionById(submissionId) ?: return@launch
            if (submission.status != "Pending Review") return@launch

            val user = repository.getUserById(submission.userId) ?: return@launch
            val task = repository.getTaskById(submission.taskId) ?: return@launch

            // Complete approval:
            // Deduct pending balance, add to actual earned balance (already did pending adjustment, let's add to balance)
            // Wait, when submitting, we increased pending balance. On approval, we decrease pending balance, and increment actual totalBalance + totalEarned!
            val updatedUser = user.copy(
                pendingBalance = maxOf(0.0, user.pendingBalance - task.reward),
                totalBalance = user.totalBalance + task.reward,
                totalEarned = user.totalEarned + task.reward
            )
            repository.updateUser(updatedUser)

            // Auto decrease remaining task slot
            val updatedTask = task.copy(
                remainingSlots = maxOf(0, task.remainingSlots - 1)
            )
            repository.updateTask(updatedTask)

            // Update submission status
            repository.updateSubmission(submission.copy(status = "Approved"))

            // Add task income transaction
            repository.insertTransaction(TransactionEntity(
                userId = user.id,
                amount = task.reward,
                type = "Task Income",
                description = "Approved task: ${task.title}"
            ))

            // Trigger Referral Earnings: Level 1 (5%), Level 2 (5%), Level 3 (2%)
            distributeReferralCommission(user.id, task.reward, task.title)

            // Send notification
            repository.insertNotification(NotificationEntity(
                userId = user.id,
                title = "Task Approved!",
                message = "Your submission for '${task.title}' has been approved! +${task.reward} BDT added.",
                type = "Task Approved"
            ))

            _toastMessage.emit("Submission Approved successfully!")
            checkAchievements(user.id)
        }
    }

    private suspend fun distributeReferralCommission(userId: Int, taskEarning: Double, taskTitle: String) {
        // Simple mock parent tree setup for demonstration.
        // Let's assume parent chain:
        // Main user (jrshakibyt) refers ashik_l1, who refers kamal_l2, who refers hasan_l3.
        // If hasan_l3 earns, kamal_l2 gets 5% (L1), ashik_l1 gets 5% (L2), and main user gets 2% (L3)!
        // In this Room db setup, we can fetch users by scanning their IDs or tree settings.
        // Let's trace back from the current user. Since we seeded:
        // idL1_1, idL1_2, idL2_1, idL3_1, mainUserId.
        // Let's implement real referral tiering payout:
        // L1 refers User: L1 gets 5%. L2 gets 5%. L3 gets 2%.
        val childUser = repository.getUserById(userId) ?: return
        if (childUser.username == "hasan_l3") {
            // hasan_l3 -> parent is kamal_l2 (id 3) -> grandparent is ashik_l1 (id 1) -> great-grandparent is jrshakibyt (id 5)
            payReferralLevel(3, taskEarning * 0.05, "Level 1 referral commission from @hasan_l3")
            payReferralLevel(1, taskEarning * 0.05, "Level 2 referral commission from @hasan_l3")
            payReferralLevel(5, taskEarning * 0.02, "Level 3 referral commission from @hasan_l3")
        } else if (childUser.username == "kamal_l2") {
            // kamal_l2 -> parent is ashik_l1 (id 1) -> grandparent is jrshakibyt (id 5)
            payReferralLevel(1, taskEarning * 0.05, "Level 1 referral commission from @kamal_l2")
            payReferralLevel(5, taskEarning * 0.05, "Level 2 referral commission from @kamal_l2")
        } else if (childUser.username == "ashik_l1" || childUser.username == "mimi_l1") {
            // Parent is jrshakibyt (id 5)
            payReferralLevel(5, taskEarning * 0.05, "Level 1 referral commission from @${childUser.username}")
        }
    }

    private suspend fun payReferralLevel(referrerId: Int, reward: Double, desc: String) {
        val referrer = repository.getUserById(referrerId) ?: return
        val updatedReferrer = referrer.copy(
            totalBalance = referrer.totalBalance + reward,
            totalEarned = referrer.totalEarned + reward,
            referralEarnings = referrer.referralEarnings + reward
        )
        repository.updateUser(updatedReferrer)

        repository.insertTransaction(TransactionEntity(
            userId = referrer.id,
            amount = reward,
            type = "Referral Income",
            description = desc
        ))

        repository.insertNotification(NotificationEntity(
            userId = referrer.id,
            title = "Referral Earnings!",
            message = "Received $reward BDT: $desc",
            type = "Referral Reward"
        ))
    }

    fun rejectSubmission(submissionId: Int, reason: String) {
        viewModelScope.launch {
            val submission = repository.getSubmissionById(submissionId) ?: return@launch
            if (submission.status != "Pending Review") return@launch

            val user = repository.getUserById(submission.userId) ?: return@launch
            val task = repository.getTaskById(submission.taskId) ?: return@launch

            // Revoke from pending balance
            val updatedUser = user.copy(
                pendingBalance = maxOf(0.0, user.pendingBalance - task.reward)
            )
            repository.updateUser(updatedUser)

            repository.updateSubmission(submission.copy(status = "Rejected", rejectReason = reason))

            // Notify user
            repository.insertNotification(NotificationEntity(
                userId = user.id,
                title = "Task Rejected!",
                message = "Your submission for '${task.title}' was rejected. Reason: $reason",
                type = "Task Rejected"
            ))

            _toastMessage.emit("Submission Rejected successfully!")
        }
    }

    fun requestResubmit(submissionId: Int, reason: String) {
        viewModelScope.launch {
            val submission = repository.getSubmissionById(submissionId) ?: return@launch
            if (submission.status != "Pending Review") return@launch

            val user = repository.getUserById(submission.userId) ?: return@launch
            val task = repository.getTaskById(submission.taskId) ?: return@launch

            // Revoke from pending balance
            val updatedUser = user.copy(
                pendingBalance = maxOf(0.0, user.pendingBalance - task.reward)
            )
            repository.updateUser(updatedUser)

            repository.updateSubmission(submission.copy(status = "Request Resubmit", rejectReason = reason))

            // Notify user
            repository.insertNotification(NotificationEntity(
                userId = user.id,
                title = "Resubmission Requested!",
                message = "Please edit and resubmit your proof for '${task.title}'. Comment: $reason",
                type = "Task Rejected"
            ))

            _toastMessage.emit("Resubmission requested successfully!")
        }
    }

    // --- Admin Actions: Withdrawals Review ---
    fun approveWithdrawal(withdrawalId: Int) {
        viewModelScope.launch {
            val withdrawal = repository.allWithdrawals.firstOrNull()?.find { it.id == withdrawalId } ?: return@launch
            if (withdrawal.status != "Pending") return@launch

            repository.updateWithdrawal(withdrawal.copy(status = "Approved"))

            // Notify
            repository.insertNotification(NotificationEntity(
                userId = withdrawal.userId,
                title = "Withdrawal Approved!",
                message = "Your cashout request of ${withdrawal.amount} BDT via ${withdrawal.method} has been approved.",
                type = "Withdrawal Update"
            ))

            _toastMessage.emit("Withdrawal Approved!")
        }
    }

    fun rejectWithdrawal(withdrawalId: Int) {
        viewModelScope.launch {
            val withdrawal = repository.allWithdrawals.firstOrNull()?.find { it.id == withdrawalId } ?: return@launch
            if (withdrawal.status != "Pending") return@launch

            repository.updateWithdrawal(withdrawal.copy(status = "Rejected"))

            // Return funds back to user
            val user = repository.getUserById(withdrawal.userId) ?: return@launch
            val updatedUser = user.copy(
                totalBalance = user.totalBalance + withdrawal.amount + withdrawal.fee
            )
            repository.updateUser(updatedUser)

            // Insert rollback txn
            repository.insertTransaction(TransactionEntity(
                userId = user.id,
                amount = withdrawal.amount + withdrawal.fee,
                type = "Bonus Income",
                description = "Refunded withdrawal request (Rejected)"
            ))

            // Notify
            repository.insertNotification(NotificationEntity(
                userId = withdrawal.userId,
                title = "Withdrawal Rejected!",
                message = "Your cashout request of ${withdrawal.amount} BDT via ${withdrawal.method} was rejected. Funds returned.",
                type = "Withdrawal Update"
            ))

            _toastMessage.emit("Withdrawal Rejected & Refunded!")
        }
    }

    // --- Admin Actions: Task Management ---
    fun createTask(
        title: String,
        reward: Double,
        totalSlots: Int,
        taskType: String,
        estimatedTime: String,
        difficulty: String,
        category: String,
        steps: List<TaskStep>
    ) {
        viewModelScope.launch {
            val task = TaskEntity(
                title = title,
                reward = reward,
                totalSlots = totalSlots,
                remainingSlots = totalSlots,
                taskType = taskType,
                estimatedTime = estimatedTime,
                difficulty = difficulty,
                expiryTime = System.currentTimeMillis() + 604800000, // 7 days default
                category = category,
                instructionsJson = TaskStepSerializer.serialize(steps)
            )
            repository.insertTask(task)

            // Global Notification
            repository.insertNotification(NotificationEntity(
                userId = -1, // global
                title = "New Task Available!",
                message = "Complete '$title' and earn $reward BDT instantly!",
                type = "New Task"
            ))

            _toastMessage.emit("Task created successfully!")
        }
    }

    fun updateTaskStatus(task: TaskEntity, isPaused: Boolean) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isPaused = isPaused))
            _toastMessage.emit("Task ${if (isPaused) "Paused" else "Resumed"}")
        }
    }

    fun duplicateTask(task: TaskEntity) {
        viewModelScope.launch {
            val duplicate = task.copy(
                id = 0,
                title = "${task.title} (Copy)",
                remainingSlots = task.totalSlots
            )
            repository.insertTask(duplicate)
            _toastMessage.emit("Task duplicated successfully!")
        }
    }

    fun editTask(
        taskId: Int,
        title: String,
        reward: Double,
        totalSlots: Int,
        taskType: String,
        estimatedTime: String,
        difficulty: String,
        category: String,
        steps: List<TaskStep>
    ) {
        viewModelScope.launch {
            val existing = repository.getTaskById(taskId) ?: return@launch
            val updated = existing.copy(
                title = title,
                reward = reward,
                totalSlots = totalSlots,
                remainingSlots = minOf(totalSlots, existing.remainingSlots),
                taskType = taskType,
                estimatedTime = estimatedTime,
                difficulty = difficulty,
                category = category,
                instructionsJson = TaskStepSerializer.serialize(steps)
            )
            repository.updateTask(updated)
            _toastMessage.emit("Task edited successfully!")
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
            _toastMessage.emit("Task deleted successfully!")
        }
    }

    // --- Admin Actions: User Management ---
    fun updateUserBanned(user: UserEntity, isBanned: Boolean) {
        viewModelScope.launch {
            repository.updateUser(user.copy(isBanned = isBanned))
            _toastMessage.emit("User @${user.username} has been ${if (isBanned) "BANNED" else "UNBANNED"}.")
        }
    }

    fun adjustUserBalance(user: UserEntity, adjustment: Double, reason: String) {
        viewModelScope.launch {
            val updatedUser = user.copy(
                totalBalance = maxOf(0.0, user.totalBalance + adjustment),
                totalEarned = maxOf(0.0, user.totalEarned + (if (adjustment > 0) adjustment else 0.0))
            )
            repository.updateUser(updatedUser)

            repository.insertTransaction(TransactionEntity(
                userId = user.id,
                amount = adjustment,
                type = if (adjustment > 0) "Bonus Income" else "Withdrawal",
                description = "Admin adjustment: $reason"
            ))

            repository.insertNotification(NotificationEntity(
                userId = user.id,
                title = "Balance Adjusted",
                message = "Admin adjusted your balance by $adjustment BDT. Reason: $reason",
                type = "Bonus"
            ))

            _toastMessage.emit("User balance adjusted by $adjustment BDT.")
        }
    }

    fun broadcastAnnouncement(title: String, message: String) {
        viewModelScope.launch {
            repository.insertNotification(NotificationEntity(
                userId = -1, // Global
                title = title,
                message = message,
                type = "Announcement"
            ))
            _toastMessage.emit("Announcement broadcasted globally!")
        }
    }

    // --- Achievement Engine ---
    private suspend fun checkAchievements(userId: Int) {
        val approvedSubmissionsCount = _userSubmissions.value.count { it.status == "Approved" }
        val achievements = _userAchievements.value.map { it.code }

        val possibleBadges = listOf(
            Triple("TASKS_10", "Bronze Earner", "Completed 10 tasks successfully."),
            Triple("TASKS_50", "Silver Earner", "Completed 50 tasks successfully."),
            Triple("TASKS_100", "Gold Earner", "Completed 100 tasks successfully."),
            Triple("TASKS_500", "Platinum Specialist", "Completed 500 tasks successfully."),
            Triple("TASKS_1000", "Diamond Legend", "Completed 1000 tasks successfully.")
        )

        for ((code, title, desc) in possibleBadges) {
            val threshold = code.substringAfter("TASKS_").toInt()
            if (approvedSubmissionsCount >= threshold && !achievements.contains(code)) {
                val achievement = AchievementEntity(
                    userId = userId,
                    code = code,
                    title = title,
                    description = desc
                )
                repository.insertAchievement(achievement)

                // Add bonus coin reward for unlocking badge!
                val bonusCoins = threshold * 0.5 // e.g. +5.0 for 10 tasks, +25.0 for 50, etc.
                val user = repository.getUserById(userId)
                if (user != null) {
                    val updatedUser = user.copy(
                        totalBalance = user.totalBalance + bonusCoins,
                        totalEarned = user.totalEarned + bonusCoins
                    )
                    repository.updateUser(updatedUser)
                    _currentUser.value = updatedUser

                    repository.insertTransaction(TransactionEntity(
                        userId = userId,
                        amount = bonusCoins,
                        type = "Bonus Income",
                        description = "Badge Unlocked: $title Bonus"
                    ))
                }

                repository.insertNotification(NotificationEntity(
                    userId = userId,
                    title = "Badge Unlocked: $title!",
                    message = "Congratulations! You have completed $threshold tasks. +$bonusCoins BDT added!",
                    type = "Bonus"
                ))
            }
        }
    }
}

class TaskHubViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskHubViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskHubViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

enum class NotificationType {
    SUCCESS, INFO, WARNING
}

data class GlobalNotification(
    val title: String,
    val message: String,
    val type: NotificationType = NotificationType.SUCCESS
)
