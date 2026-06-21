package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val username: String,
    val fullName: String,
    val bio: String = "",
    val occupation: String = "",
    val country: String = "United States",
    val timezone: String = "UTC",
    val avatarUrl: String = "",
    val coverUrl: String = "",
    val level: Int = 1,
    val xp: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lifeScore: Double = 80.0,
    val achievementsCount: Int = 0,
    val joinTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val name: String,
    val description: String,
    val category: String, // Health, Fitness, Learning, Productivity, Spiritual, Finance, Relationships
    val priority: String, // Low, Medium, High
    val frequency: String, // Daily, Weekly, Monthly, Custom
    val reminderTime: String, // e.g. "08:00"
    val colorHex: String,
    val iconName: String,
    val isArchived: Boolean = false,
    val customIntervalDays: Int = 0,
    val customDaysOfWeek: String = "",
    val dependsOnHabitId: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "habit_logs")
data class HabitLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val habitId: Int,
    val dateString: String, // "YYYY-MM-DD"
    val status: String, // "Completed", "Skipped", "Rescheduled"
    val note: String? = null,
    val evidencePath: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "advanced_tracker_logs")
data class AdvancedTrackerLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val dateString: String, // "YYYY-MM-DD"
    val metricType: String, // "Water", "Sleep", "Weight", "Expenses", "Mood", "Reading", "Study", "Workout", "Prayer", "ScreenTime"
    val value: Double,
    val unit: String,
    val notes: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val badgeId: String, // "first_habit", "streak_7", "streak_30", "streak_100", "consistency_master", "study_champ", "fitness_warrior", "productivity_legend"
    val name: String,
    val description: String,
    val unlockedTimestamp: Long = System.currentTimeMillis(),
    val iconName: String
)

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey val userId: Int,
    val notificationsEnabled: Boolean = true,
    val emailNudges: Boolean = true,
    val smartAlerts: Boolean = true
)
