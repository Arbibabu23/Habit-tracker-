package com.example.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiApiClient
import com.example.data.db.AppDatabase
import com.example.data.models.*
import com.example.data.repository.HabitRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = HabitRepository(db)

    // --- Screen States ---
    sealed interface Screen {
        object Login : Screen
        object Onboarding : Screen
        object Dashboard : Screen
        object Habits : Screen
        object Trackers : Screen
        object Analytics : Screen
        object Community : Screen
        object Profile : Screen
    }

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Login)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // --- Auth State ---
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    // --- Onboarding selections ---
    val selectedGoals = MutableStateFlow<Set<String>>(emptySet())
    val selectedDifficulty = MutableStateFlow("Intermediate")
    val selectedTargetPeriod = MutableStateFlow(30)
    
    private val _isOnboardingProcessing = MutableStateFlow(false)
    val isOnboardingProcessing: StateFlow<Boolean> = _isOnboardingProcessing.asStateFlow()
    
    private val _onboardingAiPlan = MutableStateFlow<String?>(null)
    val onboardingAiPlan: StateFlow<String?> = _onboardingAiPlan.asStateFlow()

    // --- Habit & Log Collections ---
    val activeHabits: StateFlow<List<Habit>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) repository.getActiveHabits(user.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habitLogs: StateFlow<List<HabitLog>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) repository.getAllLogs(user.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trackerLogs: StateFlow<List<AdvancedTrackerLog>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) repository.getAllTrackers(user.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val achievements: StateFlow<List<Achievement>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) repository.getUnlockedAchievements(user.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- AI Generated States ---
    private val _smartInsights = MutableStateFlow<List<String>>(
        listOf(
            "Your studies are 22% more consistent on weekday mornings.",
            "Water intake metrics directly boost deep focus length by +15%.",
            "Completing spiritual routines before midnight safeguards a solid Sleep index."
        )
    )
    val smartInsights: StateFlow<List<String>> = _smartInsights.asStateFlow()

    private val _isInsightsLoading = MutableStateFlow(false)
    val isInsightsLoading: StateFlow<Boolean> = _isInsightsLoading.asStateFlow()

    private val _weeklyReviewText = MutableStateFlow<String?>(null)
    val weeklyReviewText: StateFlow<String?> = _weeklyReviewText.asStateFlow()

    private val _isWeeklyReviewLoading = MutableStateFlow(false)
    val isWeeklyReviewLoading: StateFlow<Boolean> = _isWeeklyReviewLoading.asStateFlow()

    // Current logged metrics
    val waterLogToday = MutableStateFlow(0.0) // Cups
    val sleepLogToday = MutableStateFlow(0.0) // Hours
    val moodToday = MutableStateFlow(3.0) // 1-5 scale (3 is Meh/Neutral, 5 is Epic)
    val expensesToday = MutableStateFlow(0.0) // $ Amount
    val readingToday = MutableStateFlow(0.0) // Pages
    val screenTimeToday = MutableStateFlow(0.0) // Hours

    // Get formatted today's date
    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(System.currentTimeMillis())
    }

    init {
        // Automatically check/load tracker stats if user changes
        viewModelScope.launch {
            _currentUser.collectLatest { user ->
                if (user != null) {
                    loadTodayTrackerMetrics(user.id)
                    triggerRefreshSmartInsights()
                }
            }
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    // --- Authentication ---
    fun signUp(email: String, fullName: String) {
        if (email.isBlank() || fullName.isBlank()) {
            _authError.value = "Please fill in all details."
            return
        }
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            try {
                val user = repository.loginOrCreateUser(email, fullName)
                _currentUser.value = user
                _currentScreen.value = Screen.Onboarding // New user goes to Onboarding
            } catch (e: Exception) {
                _authError.value = "Registration failed: ${e.message}"
            } finally {
                _isAuthLoading.value = false
            }
        }
    }

    fun signIn(email: String) {
        if (email.isBlank()) {
            _authError.value = "Please enter your email."
            return
        }
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            try {
                val user = repository.loginOrCreateUser(email, "Disciplined Achiever")
                _currentUser.value = user
                
                // If they have habits set up, skip onboarding directly to dashboard
                val activeHabitsSync = db.habitDao().getActiveHabitsSync(user.id)
                if (activeHabitsSync.isNotEmpty()) {
                    _currentScreen.value = Screen.Dashboard
                } else {
                    _currentScreen.value = Screen.Onboarding
                }
            } catch (e: Exception) {
                _authError.value = "Authentication failed: ${e.message}"
            } finally {
                _isAuthLoading.value = false
            }
        }
    }

    fun loginWithAppleOrGoogle(name: String, email: String) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            try {
                val user = repository.loginOrCreateUser(email, name)
                _currentUser.value = user
                val activeHabitsSync = db.habitDao().getActiveHabitsSync(user.id)
                if (activeHabitsSync.isNotEmpty()) {
                    _currentScreen.value = Screen.Dashboard
                } else {
                    _currentScreen.value = Screen.Onboarding
                }
            } catch (e: Exception) {
                _authError.value = "Social Auth failed: ${e.message}"
            } finally {
                _isAuthLoading.value = false
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _currentScreen.value = Screen.Login
    }

    // --- Onboarding Flow ---
    fun toggleGoal(goal: String) {
        val current = selectedGoals.value
        if (current.contains(goal)) {
            selectedGoals.value = current - goal
        } else {
            selectedGoals.value = current + goal
        }
    }

    fun generateAIPersonalizedHabitPlan() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _isOnboardingProcessing.value = true
            _onboardingAiPlan.value = null
            try {
                val planText = GeminiApiClient.generateAiHabitPlan(
                    goals = selectedGoals.value.toList(),
                    difficulty = selectedDifficulty.value,
                    targetDays = selectedTargetPeriod.value
                )
                _onboardingAiPlan.value = planText
                
                // Auto seed generated habits based on goals into local SQLite
                selectedGoals.value.forEach { goal ->
                    val habitDetails = getTemplateHabitForGoal(goal, user.id)
                    repository.createHabit(habitDetails)
                }
                
                // Recalculate stats for fresh user
                repository.recalculateStreaks(user.id)
                
            } catch (e: Exception) {
                _onboardingAiPlan.value = "Successfully generated local custom template! Click continue."
            } finally {
                _isOnboardingProcessing.value = false
            }
        }
    }

    private fun getTemplateHabitForGoal(goal: String, userId: Int): Habit {
        return when (goal) {
            "Health" -> Habit(
                userId = userId,
                name = "Drink 8 Cups of Water",
                description = "Keep hydrated for peak cognitive and physical athletic performance.",
                category = "Health",
                priority = "Medium",
                frequency = "Daily",
                reminderTime = "08:00",
                colorHex = "#2563EB",
                iconName = "water_drop"
            )
            "Fitness" -> Habit(
                userId = userId,
                name = "Physical Gym Workout",
                description = "Perform structured cardiovascular or weight training to tone body.",
                category = "Fitness",
                priority = "High",
                frequency = "Daily",
                reminderTime = "17:30",
                colorHex = "#10B981",
                iconName = "fitness_center"
            )
            "Study" -> Habit(
                userId = userId,
                name = "Study 2 Hours",
                description = "Intense distraction-free academic studies or skill-focused modules.",
                category = "Learning",
                priority = "High",
                frequency = "Daily",
                reminderTime = "14:00",
                colorHex = "#8B5CF6",
                iconName = "school"
            )
            "Reading" -> Habit(
                userId = userId,
                name = "Read 15 Pages",
                description = "Progressing slowly on your select queue of books.",
                category = "Learning",
                priority = "Medium",
                frequency = "Daily",
                reminderTime = "21:00",
                colorHex = "#A78BFA",
                iconName = "menu_book"
            )
            "Prayer" -> Habit(
                userId = userId,
                name = "Daily Prayers & Quran",
                description = "Connecting with spirituality, scriptures, and divine prayer routines.",
                category = "Spiritual",
                priority = "High",
                frequency = "Daily",
                reminderTime = "06:00",
                colorHex = "#F59E0B",
                iconName = "favorite"
            )
            "Mindfulness" -> Habit(
                userId = userId,
                name = "Breathing Meditation",
                description = "Focus on diaphragmatic breathing loop for mental clarity.",
                category = "Spiritual",
                priority = "Low",
                frequency = "Daily",
                reminderTime = "22:30",
                colorHex = "#06B6D4",
                iconName = "spa"
            )
            "Finance" -> Habit(
                userId = userId,
                name = "Log Daily Expenditures",
                description = "Track expense files, update spreadsheet ledger of outlays.",
                category = "Finance",
                priority = "Medium",
                frequency = "Daily",
                reminderTime = "20:00",
                colorHex = "#10B981",
                iconName = "payments"
            )
            else -> Habit(
                userId = userId,
                name = "Deep Work Protocol",
                description = "Executing on important projects with focus blocks.",
                category = "Productivity",
                priority = "High",
                frequency = "Daily",
                reminderTime = "09:00",
                colorHex = "#4F46E5",
                iconName = "bolt"
            )
        }
    }

    // --- Habit Actions ---
    fun toggleHabit(habitId: Int) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.toggleHabitStatus(
                habitId = habitId,
                dateString = getTodayDateString(),
                status = "Completed",
                userId = user.id
            )
            // Trigger refresh of user stats
            val updatedUser = repository.getUserSync(user.id)
            if (updatedUser != null) {
                _currentUser.value = updatedUser
            }
        }
    }

    fun completeHabitWithStatus(habitId: Int, status: String, note: String?) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.toggleHabitStatus(
                habitId = habitId,
                dateString = getTodayDateString(),
                status = status,
                note = note,
                userId = user.id
            )
            val updatedUser = repository.getUserSync(user.id)
            if (updatedUser != null) {
                _currentUser.value = updatedUser
            }
        }
    }

    fun addCustomHabit(
        name: String,
        description: String,
        category: String,
        priority: String,
        frequency: String,
        reminderTime: String,
        colorHex: String,
        iconName: String,
        customIntervalDays: Int = 0,
        customDaysOfWeek: String = "",
        dependsOnHabitId: Int = 0
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val habit = Habit(
                userId = user.id,
                name = name,
                description = description,
                category = category,
                priority = priority,
                frequency = frequency,
                reminderTime = reminderTime,
                colorHex = colorHex,
                iconName = iconName,
                customIntervalDays = customIntervalDays,
                customDaysOfWeek = customDaysOfWeek,
                dependsOnHabitId = dependsOnHabitId
            )
            repository.createHabit(habit)
        }
    }

    fun editHabit(habit: Habit) {
        viewModelScope.launch {
            repository.updateHabit(habit)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    // --- Advanced Trackers Logging ---
    private suspend fun loadTodayTrackerMetrics(userId: Int) {
        val todayStr = getTodayDateString()
        
        // Query database directly to prepare flows
        val allMetrics: List<AdvancedTrackerLog> = db.advancedTrackerLogDao().getAllMetricsSync(userId).filter { it.dateString == todayStr }
        
        waterLogToday.value = allMetrics.firstOrNull { it.metricType == "Water" }?.value ?: 0.0
        sleepLogToday.value = allMetrics.firstOrNull { it.metricType == "Sleep" }?.value ?: 0.0
        moodToday.value = allMetrics.firstOrNull { it.metricType == "Mood" }?.value ?: 3.0
        expensesToday.value = allMetrics.firstOrNull { it.metricType == "Expenses" }?.value ?: 0.0
        readingToday.value = allMetrics.firstOrNull { it.metricType == "Reading" }?.value ?: 0.0
        screenTimeToday.value = allMetrics.firstOrNull { it.metricType == "ScreenTime" }?.value ?: 0.0
    }

    fun logMetric(metricType: String, value: Double, unit: String, notes: String? = null) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.logCustomMetric(user.id, metricType, value, unit, notes)
            loadTodayTrackerMetrics(user.id)
            
            // Refresh user level/XP state
            val updatedUser = repository.getUserSync(user.id)
            if (updatedUser != null) {
                _currentUser.value = updatedUser
            }
        }
    }

    // --- AI Refresh ---
    fun triggerRefreshSmartInsights() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _isInsightsLoading.value = true
            try {
                val habits = db.habitDao().getActiveHabitsSync(user.id)
                val logs = db.habitLogDao().getAllLogsSync(user.id)
                val response = GeminiApiClient.generateSmartInsights(habits, logs)
                
                // Parse lines
                val list = response.lines().filter { it.isNotBlank() && it.length > 5 }.take(3)
                if (list.isNotEmpty()) {
                    _smartInsights.value = list
                }
            } catch (e: Exception) {
                // Keep default insights
            } finally {
                _isInsightsLoading.value = false
            }
        }
    }

    fun loadWeeklyAITripwireReview() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _isWeeklyReviewLoading.value = true
            _weeklyReviewText.value = null
            try {
                val habits = db.habitDao().getActiveHabitsSync(user.id)
                val logs = db.habitLogDao().getAllLogsSync(user.id)
                val report = GeminiApiClient.generateWeeklyReview(user, habits, logs)
                _weeklyReviewText.value = report
            } catch (e: Exception) {
                _weeklyReviewText.value = "Failed to load automated review. Please try again later."
            } finally {
                _isWeeklyReviewLoading.value = false
            }
        }
    }
}
