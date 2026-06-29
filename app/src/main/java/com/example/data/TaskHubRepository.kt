package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class TaskHubRepository(private val dao: TaskHubDao) {

    val allUsers: Flow<List<UserEntity>> = dao.getAllUsersFlow()
    val activeTasks: Flow<List<TaskEntity>> = dao.getActiveTasksFlow()
    val allTasks: Flow<List<TaskEntity>> = dao.getAllTasksFlow()
    val allSubmissions: Flow<List<SubmissionEntity>> = dao.getAllSubmissionsFlow()
    val allWithdrawals: Flow<List<WithdrawalEntity>> = dao.getAllWithdrawalsFlow()

    fun getSubmissionsForUser(userId: Int): Flow<List<SubmissionEntity>> =
        dao.getSubmissionsForUserFlow(userId)

    fun getWithdrawalsForUser(userId: Int): Flow<List<WithdrawalEntity>> =
        dao.getWithdrawalsForUserFlow(userId)

    fun getNotificationsForUser(userId: Int): Flow<List<NotificationEntity>> =
        dao.getNotificationsForUserFlow(userId)

    fun getTransactionsForUser(userId: Int): Flow<List<TransactionEntity>> =
        dao.getTransactionsForUserFlow(userId)

    fun getAchievementsForUser(userId: Int): Flow<List<AchievementEntity>> =
        dao.getAchievementsForUserFlow(userId)

    suspend fun getUserByTelegramId(telegramId: String): UserEntity? =
        dao.getUserByTelegramId(telegramId)

    suspend fun getUserById(id: Int): UserEntity? =
        dao.getUserById(id)

    suspend fun insertUser(user: UserEntity): Long =
        dao.insertUser(user)

    suspend fun updateUser(user: UserEntity) =
        dao.updateUser(user)

    suspend fun getTaskById(id: Int): TaskEntity? =
        dao.getTaskById(id)

    suspend fun insertTask(task: TaskEntity): Long =
        dao.insertTask(task)

    suspend fun updateTask(task: TaskEntity) =
        dao.updateTask(task)

    suspend fun deleteTask(task: TaskEntity) =
        dao.deleteTask(task)

    suspend fun getSubmissionById(id: Int): SubmissionEntity? =
        dao.getSubmissionById(id)

    suspend fun insertSubmission(submission: SubmissionEntity): Long =
        dao.insertSubmission(submission)

    suspend fun updateSubmission(submission: SubmissionEntity) =
        dao.updateSubmission(submission)

    suspend fun insertWithdrawal(withdrawal: WithdrawalEntity): Long =
        dao.insertWithdrawal(withdrawal)

    suspend fun updateWithdrawal(withdrawal: WithdrawalEntity) =
        dao.updateWithdrawal(withdrawal)

    suspend fun insertNotification(notification: NotificationEntity) =
        dao.insertNotification(notification)

    suspend fun markNotificationsAsRead(userId: Int) =
        dao.markNotificationsAsRead(userId)

    suspend fun insertTransaction(transaction: TransactionEntity) =
        dao.insertTransaction(transaction)

    suspend fun insertAchievement(achievement: AchievementEntity) =
        dao.insertAchievement(achievement)

    suspend fun seedDatabaseIfEmpty() {
        // Seed Users
        val existingUsers = dao.getAllUsersFlow().firstOrNull()
        if (existingUsers.isNullOrEmpty()) {
            val userL1_1 = UserEntity(
                telegramId = "8899001",
                username = "ashik_l1",
                totalBalance = 10.0,
                pendingBalance = 0.0,
                totalEarned = 10.0,
                referralEarnings = 0.0,
                role = "user"
            )
            val userL1_2 = UserEntity(
                telegramId = "8899002",
                username = "mimi_l1",
                totalBalance = 20.0,
                pendingBalance = 5.0,
                totalEarned = 25.0,
                referralEarnings = 0.0,
                role = "user"
            )
            val userL2_1 = UserEntity(
                telegramId = "8899003",
                username = "kamal_l2",
                totalBalance = 50.0,
                pendingBalance = 0.0,
                totalEarned = 50.0,
                referralEarnings = 0.0,
                role = "user"
            )
            val userL3_1 = UserEntity(
                telegramId = "8899004",
                username = "hasan_l3",
                totalBalance = 15.0,
                pendingBalance = 0.0,
                totalEarned = 15.0,
                referralEarnings = 0.0,
                role = "user"
            )

            val idL1_1 = dao.insertUser(userL1_1).toInt()
            val idL1_2 = dao.insertUser(userL1_2).toInt()
            val idL2_1 = dao.insertUser(userL2_1).toInt()
            val idL3_1 = dao.insertUser(userL3_1).toInt()

            // Main User
            val mainUser = UserEntity(
                telegramId = "738291032",
                username = "jrshakibyt",
                totalBalance = 145.0,
                pendingBalance = 25.0,
                totalEarned = 170.0,
                referralEarnings = 18.5,
                referralsL1 = 2,
                referralsL2 = 1,
                referralsL3 = 1,
                referralTreeJson = "L1: @ashik_l1, @mimi_l1 | L2: @kamal_l2 (via @ashik_l1) | L3: @hasan_l3 (via @kamal_l2)",
                streakDays = 3,
                lastCheckIn = System.currentTimeMillis() - 86400000, // yesterday
                role = "user"
            )
            val mainUserId = dao.insertUser(mainUser).toInt()

            // Admin User
            val adminUser = UserEntity(
                telegramId = "1122334455",
                username = "admin_shakib",
                totalBalance = 0.0,
                pendingBalance = 0.0,
                totalEarned = 0.0,
                referralEarnings = 0.0,
                role = "admin"
            )
            dao.insertUser(adminUser)

            // Seed Tasks
            val ytSteps = listOf(
                TaskStep(1, "Open the YouTube link: https://youtube.com", "text"),
                TaskStep(2, "Search for 'TaskHub Earning Platform'", "bold"),
                TaskStep(3, "Watch the latest video for at least 1 minute.", "text"),
                TaskStep(4, "Like the video and Subscribe to the channel.", "bold"),
                TaskStep(5, "Take a screenshot showing that you liked & subscribed.", "warning"),
                TaskStep(6, "Submit the screenshot here as proof of completion.", "note")
            )
            val tgSteps = listOf(
                TaskStep(1, "Open Telegram link: https://t.me/taskhub_news", "text"),
                TaskStep(2, "Click Join Channel", "bold"),
                TaskStep(3, "Keep notifications unmuted for 24 hours.", "warning"),
                TaskStep(4, "Take a screenshot showing you have successfully joined.", "text"),
                TaskStep(5, "Submit the screenshot.", "note")
            )
            val twSteps = listOf(
                TaskStep(1, "Open Twitter/X link: https://twitter.com/taskhub", "text"),
                TaskStep(2, "Follow the account and Retweet the pinned post.", "bold"),
                TaskStep(3, "Take a screenshot showing your Retweet.", "text"),
                TaskStep(4, "Warning: Accounts with 0 followers will not be accepted.", "warning"),
                TaskStep(5, "Submit screenshot and your X handle in notes.", "note")
            )
            val binanceSteps = listOf(
                TaskStep(1, "Open Binance Signup link: https://binance.com", "text"),
                TaskStep(2, "Register a new account and complete Basic Identity Verification (KYC).", "bold"),
                TaskStep(3, "Take a screenshot of your Profile page showing 'Verified' status.", "warning"),
                TaskStep(4, "Note: This task is only for new users who don't have a Binance account.", "note"),
                TaskStep(5, "Submit the screenshot showing your verified badge and UID.", "bold")
            )

            dao.insertTask(TaskEntity(
                title = "YouTube Subscribe & Like",
                reward = 10.0,
                totalSlots = 1000,
                remainingSlots = 998,
                taskType = "YouTube Like",
                estimatedTime = "1 min",
                difficulty = "Easy",
                expiryTime = System.currentTimeMillis() + 864000000,
                instructionsJson = TaskStepSerializer.serialize(ytSteps),
                category = "YouTube"
            ))

            dao.insertTask(TaskEntity(
                title = "Join Telegram News Channel",
                reward = 8.0,
                totalSlots = 500,
                remainingSlots = 485,
                taskType = "Telegram Join",
                estimatedTime = "30 sec",
                difficulty = "Easy",
                expiryTime = System.currentTimeMillis() + 604800000,
                instructionsJson = TaskStepSerializer.serialize(tgSteps),
                category = "Telegram"
            ))

            dao.insertTask(TaskEntity(
                title = "Follow on Twitter/X & Retweet",
                reward = 15.0,
                totalSlots = 300,
                remainingSlots = 295,
                taskType = "Twitter Follow",
                estimatedTime = "2 min",
                difficulty = "Medium",
                expiryTime = System.currentTimeMillis() + 518400000,
                instructionsJson = TaskStepSerializer.serialize(twSteps),
                category = "Twitter"
            ))

            dao.insertTask(TaskEntity(
                title = "Register Binance KYC Account",
                reward = 120.0,
                totalSlots = 50,
                remainingSlots = 48,
                taskType = "Signup",
                estimatedTime = "10 min",
                difficulty = "Hard",
                expiryTime = System.currentTimeMillis() + 1209600000,
                instructionsJson = TaskStepSerializer.serialize(binanceSteps),
                category = "Signup"
            ))

            // Seed Notifications
            dao.insertNotification(NotificationEntity(
                userId = mainUserId,
                title = "Welcome to TaskHub!",
                message = "Earn money by completing tasks. Daily bonuses, spin wheels, and lucky boxes are waiting for you!",
                type = "Announcement"
            ))
            dao.insertNotification(NotificationEntity(
                userId = mainUserId,
                title = "Task Approved!",
                message = "Your submission for 'YouTube Comment' has been approved. +5.0 BDT added.",
                type = "Task Approved"
            ))

            // Seed Transactions
            dao.insertTransaction(TransactionEntity(
                userId = mainUserId,
                amount = 100.0,
                type = "Bonus Income",
                description = "Starter onboarding reward"
            ))
            dao.insertTransaction(TransactionEntity(
                userId = mainUserId,
                amount = 26.5,
                type = "Task Income",
                description = "Completed YouTube Subscribe Task"
            ))
            dao.insertTransaction(TransactionEntity(
                userId = mainUserId,
                amount = 18.5,
                type = "Referral Income",
                description = "Level 1 referral commission from @ashik_l1"
            ))

            // Seed Achievements
            dao.insertAchievement(AchievementEntity(
                userId = mainUserId,
                code = "TASKS_10",
                title = "Bronze Earner",
                description = "Completed 10 online tasks successfully."
            ))

            // Seed Some Submissions
            dao.insertSubmission(SubmissionEntity(
                userId = mainUserId,
                telegramId = "738291032",
                username = "jrshakibyt",
                taskId = 1,
                taskTitle = "YouTube Subscribe & Like",
                taskReward = 10.0,
                screenshotsJson = "https://picsum.photos/400/800?random=1",
                userNote = "Done subscribing and liking. Verified!",
                status = "Pending Review"
            ))

            dao.insertSubmission(SubmissionEntity(
                userId = mainUserId,
                telegramId = "738291032",
                username = "jrshakibyt",
                taskId = 2,
                taskTitle = "Join Telegram News Channel",
                taskReward = 8.0,
                screenshotsJson = "https://picsum.photos/400/800?random=2",
                userNote = "Username: @jrshakibyt. Joined successfully.",
                status = "Approved"
            ))

            // Seed Withdrawals
            dao.insertWithdrawal(WithdrawalEntity(
                userId = mainUserId,
                amount = 50.0,
                fee = 1.0,
                method = "Bkash",
                accountAddress = "01712345678",
                status = "Approved"
            ))
            dao.insertWithdrawal(WithdrawalEntity(
                userId = mainUserId,
                amount = 25.0,
                fee = 0.5,
                method = "Binance Pay",
                accountAddress = "88392193",
                status = "Pending"
            ))
        }
    }
}
