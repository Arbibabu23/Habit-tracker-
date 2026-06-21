package com.example.data.db

import androidx.room.*
import com.example.data.models.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserFlow(userId: Int): Flow<User?>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserSync(userId: Int): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE userId = :userId AND isArchived = 0 ORDER BY createdAt DESC")
    fun getActiveHabitsFlow(userId: Int): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE userId = :userId AND isArchived = 0 ORDER BY createdAt DESC")
    suspend fun getActiveHabitsSync(userId: Int): List<Habit>

    @Query("SELECT * FROM habits WHERE id = :habitId LIMIT 1")
    suspend fun getHabitById(habitId: Int): Habit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Update
    suspend fun updateHabit(habit: Habit)
}

@Dao
interface HabitLogDao {
    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY timestamp DESC")
    fun getLogsForHabitFlow(habitId: Int): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE habitId IN (SELECT id FROM habits WHERE userId = :userId) ORDER BY timestamp DESC")
    fun getAllLogsFlow(userId: Int): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE habitId IN (SELECT id FROM habits WHERE userId = :userId) ORDER BY timestamp DESC")
    suspend fun getAllLogsSync(userId: Int): List<HabitLog>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND dateString = :dateString LIMIT 1")
    suspend fun getLogForHabitOnDate(habitId: Int, dateString: String): HabitLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HabitLog): Long

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND dateString = :dateString")
    suspend fun deleteLogForHabitOnDate(habitId: Int, dateString: String)
}

@Dao
interface AdvancedTrackerLogDao {
    @Query("SELECT * FROM advanced_tracker_logs WHERE userId = :userId AND metricType = :metricType ORDER BY timestamp DESC")
    fun getLogsByMetricFlow(userId: Int, metricType: String): Flow<List<AdvancedTrackerLog>>

    @Query("SELECT * FROM advanced_tracker_logs WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllMetricsFlow(userId: Int): Flow<List<AdvancedTrackerLog>>

    @Query("SELECT * FROM advanced_tracker_logs WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getAllMetricsSync(userId: Int): List<AdvancedTrackerLog>

    @Query("SELECT * FROM advanced_tracker_logs WHERE userId = :userId AND metricType = :metricType AND dateString = :dateString LIMIT 1")
    suspend fun getLogForMetricOnDate(userId: Int, metricType: String, dateString: String): AdvancedTrackerLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackerLog(log: AdvancedTrackerLog): Long
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements WHERE userId = :userId ORDER BY unlockedTimestamp DESC")
    fun getAchievementsFlow(userId: Int): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE userId = :userId ORDER BY unlockedTimestamp DESC")
    suspend fun getAchievementsSync(userId: Int): List<Achievement>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun unlockAchievement(achievement: Achievement): Long
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM app_settings WHERE userId = :userId LIMIT 1")
    fun getSettingsFlow(userId: Int): Flow<AppSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: AppSettings)
}

@Database(
    entities = [
        User::class,
        Habit::class,
        HabitLog::class,
        AdvancedTrackerLog::class,
        Achievement::class,
        AppSettings::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao
    abstract fun advancedTrackerLogDao(): AdvancedTrackerLogDao
    abstract fun achievementDao(): AchievementDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habitos_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
