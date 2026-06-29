package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- Room Type Converters ---
class Converters {
    @TypeConverter
    fun fromStringList(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        return value.split("|||")
    }

    @TypeConverter
    fun toStringList(list: List<String>?): String {
        if (list == null) return ""
        return list.joinToString("|||")
    }
}

// --- Entities ---

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val telegramId: String,
    val username: String,
    val totalBalance: Double,
    val pendingBalance: Double,
    val totalEarned: Double,
    val referralEarnings: Double = 0.0,
    val referralsL1: Int = 0,
    val referralsL2: Int = 0,
    val referralsL3: Int = 0,
    val referralTreeJson: String = "", // Simulated referral chain
    val streakDays: Int = 0,
    val lastCheckIn: Long = 0,
    val isBanned: Boolean = false,
    val role: String = "user" // "user" or "admin"
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val reward: Double,
    val totalSlots: Int,
    val remainingSlots: Int,
    val taskType: String, // "YouTube Like", "Telegram Join", "Twitter Follow", "Signup", "Comment"
    val estimatedTime: String, // "2 min"
    val difficulty: String, // "Easy", "Medium", "Hard"
    val expiryTime: Long,
    val instructionsJson: String, // List of TaskStep encoded as JSON or plain string
    val isPaused: Boolean = false,
    val category: String = "Social",
    val maxSubmissions: Int = 1,
    val dailyLimit: Int = 100
)

@Entity(tableName = "submissions")
data class SubmissionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val telegramId: String,
    val username: String,
    val taskId: Int,
    val taskTitle: String,
    val taskReward: Double,
    val screenshotsJson: String, // List of screenshots
    val userNote: String,
    val status: String, // "Pending Review", "Approved", "Rejected", "Request Resubmit"
    val rejectReason: String = "",
    val submittedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "withdrawals")
data class WithdrawalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val amount: Double,
    val fee: Double,
    val method: String, // "Bkash", "Nagad", "Rocket", "Binance Pay", "USDT"
    val accountAddress: String,
    val status: String, // "Pending", "Approved", "Rejected"
    val requestedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int, // -1 for global announcements
    val title: String,
    val message: String,
    val type: String, // "Task Approved", "Task Rejected", "New Task", "Bonus", "Referral Reward", "Withdrawal Update", "Announcement"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val amount: Double,
    val type: String, // "Task Income", "Referral Income", "Bonus Income", "Withdrawal"
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val code: String, // "TASKS_10", "TASKS_50", "TASKS_100", "TASKS_500", "TASKS_1000"
    val title: String,
    val description: String,
    val unlockedAt: Long = System.currentTimeMillis()
)

// --- DAOs ---

@Dao
interface TaskHubDao {
    // User Queries
    @Query("SELECT * FROM users WHERE telegramId = :telegramId LIMIT 1")
    suspend fun getUserByTelegramId(telegramId: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): UserEntity?

    @Query("SELECT * FROM users ORDER BY totalEarned DESC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    // Task Queries
    @Query("SELECT * FROM tasks WHERE isPaused = 0 ORDER BY id DESC")
    fun getActiveTasksFlow(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun getAllTasksFlow(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Int): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    // Submissions Queries
    @Query("SELECT * FROM submissions ORDER BY submittedAt DESC")
    fun getAllSubmissionsFlow(): Flow<List<SubmissionEntity>>

    @Query("SELECT * FROM submissions WHERE userId = :userId ORDER BY submittedAt DESC")
    fun getSubmissionsForUserFlow(userId: Int): Flow<List<SubmissionEntity>>

    @Query("SELECT * FROM submissions WHERE id = :id LIMIT 1")
    suspend fun getSubmissionById(id: Int): SubmissionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(submission: SubmissionEntity): Long

    @Update
    suspend fun updateSubmission(submission: SubmissionEntity)

    // Withdrawals Queries
    @Query("SELECT * FROM withdrawals ORDER BY requestedAt DESC")
    fun getAllWithdrawalsFlow(): Flow<List<WithdrawalEntity>>

    @Query("SELECT * FROM withdrawals WHERE userId = :userId ORDER BY requestedAt DESC")
    fun getWithdrawalsForUserFlow(userId: Int): Flow<List<WithdrawalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithdrawal(withdrawal: WithdrawalEntity): Long

    @Update
    suspend fun updateWithdrawal(withdrawal: WithdrawalEntity)

    // Notifications Queries
    @Query("SELECT * FROM notifications WHERE userId = :userId OR userId = -1 ORDER BY timestamp DESC")
    fun getNotificationsForUserFlow(userId: Int): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markNotificationsAsRead(userId: Int)

    // Transactions Queries
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getTransactionsForUserFlow(userId: Int): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    // Achievements Queries
    @Query("SELECT * FROM achievements WHERE userId = :userId")
    fun getAchievementsForUserFlow(userId: Int): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: AchievementEntity)
}

// --- Database ---

@Database(
    entities = [
        UserEntity::class,
        TaskEntity::class,
        SubmissionEntity::class,
        WithdrawalEntity::class,
        NotificationEntity::class,
        TransactionEntity::class,
        AchievementEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TaskHubDatabase : RoomDatabase() {
    abstract fun taskHubDao(): TaskHubDao
}
