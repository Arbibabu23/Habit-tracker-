package com.example.data.repository

import com.example.data.db.*
import com.example.data.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HabitRepository(private val db: AppDatabase) {

    private val userDao = db.userDao()
    private val habitDao = db.habitDao()
    private val habitLogDao = db.habitLogDao()
    private val trackerDao = db.advancedTrackerLogDao()
    private val achievementDao = db.achievementDao()
    private val settingsDao = db.settingsDao()

    // --- Authentication & User Operations ---
    fun getUserFlow(userId: Int): Flow<User?> = userDao.getUserFlow(userId)

    suspend fun getUserSync(userId: Int): User? = userDao.getUserSync(userId)

    suspend fun loginOrCreateUser(email: String, fullName: String): User {
        val existing = userDao.getUserByEmail(email)
        if (existing != null) {
            return existing
        }
        val newUser = User(
            email = email,
            username = email.substringBefore("@").lowercase(),
            fullName = fullName
        )
        val id = userDao.insertUser(newUser)
        
        // Setup default settings
        settingsDao.insertSettings(AppSettings(userId = id.toInt()))
        
        return newUser.copy(id = id.toInt())
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    // --- Habits Operations ---
    fun getActiveHabits(userId: Int): Flow<List<Habit>> = habitDao.getActiveHabitsFlow(userId)

    suspend fun createHabit(habit: Habit): Long {
        val id = habitDao.insertHabit(habit)
        
        // Check for "first habit" achievement
        val userId = habit.userId
        val activeHabits = habitDao.getActiveHabitsSync(userId)
        if (activeHabits.size == 1) {
            triggerUnlockAchievement(
                userId = userId,
                badgeId = "first_habit",
                name = "Step One",
                description = "Created your very first habit in HabitOS AI. The journey begins!",
                iconName = "emoji_events"
            )
        }
        return id
    }

    suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habit)
    }

    suspend fun deleteHabit(habit: Habit) {
        habitDao.deleteHabit(habit)
    }

    // --- Habit Logs & Streaks Engine ---
    fun getAllLogs(userId: Int): Flow<List<HabitLog>> = habitLogDao.getAllLogsFlow(userId)

    fun getLogsForHabit(habitId: Int): Flow<List<HabitLog>> = habitLogDao.getLogsForHabitFlow(habitId)

    suspend fun toggleHabitStatus(habitId: Int, dateString: String, status: String, note: String? = null, userId: Int): Boolean {
        // Retrieve current log
        val existingLog = habitLogDao.getLogForHabitOnDate(habitId, dateString)
        
        if (existingLog != null) {
            if (existingLog.status == status) {
                // Untoggling: remove completion
                habitLogDao.deleteLogForHabitOnDate(habitId, dateString)
                adjustUserXp(userId, -10)
                recalculateStreaks(userId)
                return false
            } else {
                // Overwriting status
                val isOldCompleted = existingLog.status == "Completed"
                val isNewCompleted = status == "Completed"
                
                val updatedLog = existingLog.copy(status = status, note = note)
                habitLogDao.insertLog(updatedLog)
                
                if (isOldCompleted && !isNewCompleted) adjustUserXp(userId, -10)
                if (!isOldCompleted && isNewCompleted) adjustUserXp(userId, 10)
                
                recalculateStreaks(userId)
                return true
            }
        } else {
            // New entry
            val newLog = HabitLog(habitId = habitId, dateString = dateString, status = status, note = note)
            habitLogDao.insertLog(newLog)
            
            if (status == "Completed") {
                adjustUserXp(userId, 10)
            }
            
            recalculateStreaks(userId)
            
            // Check achievement milestones
            val allLogs = habitLogDao.getAllLogsSync(userId).filter { it.status == "Completed" }
            if (allLogs.size == 10) {
                triggerUnlockAchievement(
                    userId = userId,
                    badgeId = "consistency_master",
                    name = "Consistency Aspirant",
                    description = "Successfully checked off 10 habit completions.",
                    iconName = "star"
                )
            } else if (allLogs.size == 25) {
                triggerUnlockAchievement(
                    userId = userId,
                    badgeId = "productivity_legend",
                    name = "Productivity Legend",
                    description = "Completed 25 habits across categories! Brilliant.",
                    iconName = "bolt"
                )
            }
            
            return true
        }
    }

    private suspend fun adjustUserXp(userId: Int, xpAmount: Int) {
        val user = userDao.getUserSync(userId) ?: return
        var newXp = user.xp + xpAmount
        if (newXp < 0) newXp = 0
        
        // XP system: 150 XP per level
        val newLevel = (newXp / 150) + 1
        val levelUpOccurred = newLevel > user.level
        
        val updatedUser = user.copy(
            xp = newXp,
            level = newLevel
        )
        userDao.updateUser(updatedUser)
        
        if (levelUpOccurred) {
            triggerUnlockAchievement(
                userId = userId,
                badgeId = "level_${newLevel}",
                name = "Power Level $newLevel",
                description = "Elevated your life score capabilities to level $newLevel by staying disciplined.",
                iconName = "military_tech"
            )
        }
    }

    suspend fun recalculateStreaks(userId: Int) {
        val user = userDao.getUserSync(userId) ?: return
        val habits = habitDao.getActiveHabitsSync(userId)
        val logs = habitLogDao.getAllLogsSync(userId).filter { it.status == "Completed" }
        
        if (habits.isEmpty() || logs.isEmpty()) {
            userDao.updateUser(user.copy(currentStreak = 0))
            return
        }

        // Streak analysis based on completed calendar dates
        val datesWithLogs = logs.map { it.dateString }.distinct().toSet()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        
        // Gather sorted date structures
        val calendarDates = datesWithLogs.mapNotNull { 
            try { sdf.parse(it) } catch (e: Exception) { null }
        }.sortedDescending()

        if (calendarDates.isEmpty()) {
            userDao.updateUser(user.copy(currentStreak = 0))
            return
        }

        val todayCal = Calendar.getInstance()
        val formattedToday = sdf.format(todayCal.time)
        todayCal.add(Calendar.DAY_OF_YEAR, -1)
        val formattedYesterday = sdf.format(todayCal.time)

        // Streak holds if there is a completion today or at least yesterday
        val hasCompletedRecently = datesWithLogs.contains(formattedToday) || datesWithLogs.contains(formattedYesterday)
        
        var currentStreak = 0
        if (hasCompletedRecently) {
            val checkCal = Calendar.getInstance()
            while (true) {
                val checkStr = sdf.format(checkCal.time)
                if (datesWithLogs.contains(checkStr)) {
                    currentStreak++
                    checkCal.add(Calendar.DAY_OF_YEAR, -1)
                } else {
                    break
                }
            }
        }

        // Compute longest streak
        var maxStreak = user.longestStreak
        if (currentStreak > maxStreak) {
            maxStreak = currentStreak
        }

        // Compute aggregate Life Score based on category completions
        // Health = 25%, Learning = 25%, Productivity = 25%, Spiritual = 15%, Finance = 10%
        val totalActiveCount = habits.size
        
        var lifeScore = 60.0 // starts at basic baseline
        if (totalActiveCount > 0) {
            val categories = habits.groupBy { it.category }
            var weightedSum = 0.0
            categories.forEach { (cat, list) ->
                val weight = when (cat) {
                    "Health" -> 25.0
                    "Learning" -> 25.0
                    "Productivity" -> 25.0
                    "Spiritual" -> 15.0
                    "Finance" -> 10.0
                    else -> 10.0 // Relationships, etc.
                }
                
                // Let's find completions in list for the last 7 days
                val last7Days = mutableListOf<String>()
                val cal = Calendar.getInstance()
                for (i in 0..6) {
                    last7Days.add(sdf.format(cal.time))
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                }
                
                val completionsCount = logs.filter { log ->
                    last7Days.contains(log.dateString) && list.any { it.id == log.habitId }
                }.size
                
                val targetsCount = list.size * 7.0
                val ratio = if (targetsCount > 0) (completionsCount / targetsCount).coerceAtMost(1.0) else 0.0
                weightedSum += (ratio * weight)
            }
            lifeScore = 40.0 + (weightedSum * 0.6) // Scale it beautifully between 40 - 100
        }

        val updatedUser = user.copy(
            currentStreak = currentStreak,
            longestStreak = maxStreak,
            lifeScore = lifeScore.coerceIn(0.0, 100.0)
        )
        userDao.updateUser(updatedUser)

        // Milestones
        if (currentStreak >= 7) {
            triggerUnlockAchievement(
                userId = userId,
                badgeId = "streak_7",
                name = "Aweekened Power",
                description = "Sustained a disciplined 7-day habit completion streak.",
                iconName = "local_fire_department"
            )
        }
        if (currentStreak >= 30) {
            triggerUnlockAchievement(
                userId = userId,
                badgeId = "streak_30",
                name = "Monthly Conqueror",
                description = "Sustained an unbeatable 30-day streak! Pure perfection.",
                iconName = "whatshot"
            )
        }
    }

    // --- Advanced Trackers ---
    fun getTrackerLogs(userId: Int, metricType: String): Flow<List<AdvancedTrackerLog>> =
        trackerDao.getLogsByMetricFlow(userId, metricType)

    fun getAllTrackers(userId: Int): Flow<List<AdvancedTrackerLog>> =
        trackerDao.getAllMetricsFlow(userId)

    suspend fun logCustomMetric(userId: Int, metricType: String, value: Double, unit: String, notes: String? = null) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dateString = sdf.format(System.currentTimeMillis())

        val existingLog = trackerDao.getLogForMetricOnDate(userId, metricType, dateString)
        if (existingLog != null) {
            val updated = existingLog.copy(value = value, notes = notes, timestamp = System.currentTimeMillis())
            trackerDao.insertTrackerLog(updated)
        } else {
            val newLog = AdvancedTrackerLog(
                userId = userId,
                dateString = dateString,
                metricType = metricType,
                value = value,
                unit = unit,
                notes = notes
            )
            trackerDao.insertTrackerLog(newLog)
        }

        // Adjust XP for logging metrics
        adjustUserXp(userId, 5)

        // Category milestone achievements
        if (metricType == "Study") {
            val studyLogs = trackerDao.getAllMetricsSync(userId).filter { it.metricType == "Study" }
            if (studyLogs.size >= 3) {
                triggerUnlockAchievement(
                    userId = userId,
                    badgeId = "study_champ",
                    name = "Study Champion",
                    description = "Logged learning stats 3+ times. Sharpening the mind daily.",
                    iconName = "school"
                )
            }
        } else if (metricType == "Workout") {
            val workoutLogs = trackerDao.getAllMetricsSync(userId).filter { it.metricType == "Workout" }
            if (workoutLogs.size >= 3) {
                triggerUnlockAchievement(
                    userId = userId,
                    badgeId = "fitness_warrior",
                    name = "Fitness Warrior",
                    description = "Logged physical workout activities 3+ times. Body is a temple.",
                    iconName = "fitness_center"
                )
            }
        }
    }

    // --- Achievements ---
    fun getUnlockedAchievements(userId: Int): Flow<List<Achievement>> =
        achievementDao.getAchievementsFlow(userId)

    private suspend fun triggerUnlockAchievement(userId: Int, badgeId: String, name: String, description: String, iconName: String) {
        val achievement = Achievement(
            userId = userId,
            badgeId = badgeId,
            name = name,
            description = description,
            iconName = iconName
        )
        val rowId = achievementDao.unlockAchievement(achievement)
        if (rowId != -1L) {
            val user = userDao.getUserSync(userId)
            if (user != null) {
                userDao.updateUser(user.copy(achievementsCount = user.achievementsCount + 1))
            }
        }
    }

    // --- App Settings ---
    fun getSettings(userId: Int): Flow<AppSettings?> = settingsDao.getSettingsFlow(userId)

    suspend fun saveSettings(settings: AppSettings) {
        settingsDao.insertSettings(settings)
    }
}
