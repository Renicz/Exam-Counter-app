package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.repository.ExamRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class ExamViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExamRepository

    // Current Time ticker updated every 1 second
    private val _currentTime = MutableStateFlow(System.currentTimeMillis())
    val currentTime: StateFlow<Long> = _currentTime.asStateFlow()

    private var tickerJob: Job? = null

    // Authentication & Profile States
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    // Interactive UI filters and state
    var searchQuery = MutableStateFlow("")
    var selectedCategoryId = MutableStateFlow<Int?>(null) // null = All
    var sortBy = MutableStateFlow("DATE") // "DATE", "FAVORITE", "PROGRESS", "ALPHABETICAL"

    // Local Data flows
    val allExams: StateFlow<List<ExamEntity>>
    val allCategories: StateFlow<List<CategoryEntity>>
    val allNotifications: StateFlow<List<NotificationEntity>>
    val allSessions: StateFlow<List<StudySessionEntity>>
    val allUsers: StateFlow<List<UserEntity>>

    // Study Planner States
    private val _dailyGoalMinutes = MutableStateFlow(60) // 1 Hour default
    val dailyGoalMinutes = _dailyGoalMinutes.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning = _isTimerRunning.asStateFlow()

    private val _timerSecondsRemaining = MutableStateFlow(1500) // 25 Min Pomodoro default
    val timerSecondsRemaining = _timerSecondsRemaining.asStateFlow()

    private var activeStudyJob: Job? = null
    var activeTimerExamId: Int = 0

    // Feature Toggles (Admin Controlled)
    private val _featureToggles = MutableStateFlow<Map<String, Boolean>>(
        mapOf(
            "PushNotifications" to true,
            "DailyReminders" to true,
            "SmartAnalytics" to true,
            "SyncOnBackground" to true
        )
    )
    val featureToggles = _featureToggles.asStateFlow()

    // System Logs (Admin Screen)
    private val _systemLogs = MutableStateFlow<List<String>>(emptyList())
    val systemLogs = _systemLogs.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ExamRepository(database)

        // Seed default parameters eagerly
        viewModelScope.launch {
            repository.seedDatabaseIfNeeded()
            // Set guest check or initial auto login for elegant preview experience
            _currentUser.value = UserEntity(
                id = "guest_pro",
                email = "student@university.edu",
                displayName = "Alex Rivera",
                role = "ADMIN" // Standard is admin so user can explore admin dashboard easily!
            )
            addLog("System initialized. Welcome, Alex Rivera.")
        }

        // Live Ticker loop for seconds update
        startTicker()

        // Sync Data Flows from Repository
        allExams = repository.allExams.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        allCategories = repository.allCategories.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        allNotifications = repository.allNotifications.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        allSessions = repository.allSessions.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        allUsers = repository.allUsers.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _currentTime.value = System.currentTimeMillis()
            }
        }
    }

    fun addLog(log: String) {
        val stamp = Calendar.getInstance().let {
            String.format("%02d:%02d:%02d", it.get(Calendar.HOUR_OF_DAY), it.get(Calendar.MINUTE), it.get(Calendar.SECOND))
        }
        _systemLogs.value = listOf("[$stamp] $log") + _systemLogs.value.take(29)
    }

    // --- Core Feature: Authentications ---
    fun loginWithEmail(email: String, pword: String) {
        if (email.isBlank() || pword.isBlank()) {
            _authError.value = "Email and Password cannot be empty."
            return
        }
        _isAuthLoading.value = true
        _authError.value = null
        viewModelScope.launch {
            delay(1000) // Simulated network
            val isMockAdmin = email.contains("admin", ignoreCase = true)
            val id = "user_" + email.hashCode().coerceAtLeast(0)
            val displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() }
            val role = if (isMockAdmin) "ADMIN" else "USER"

            val user = UserEntity(id = id, email = email, displayName = displayName, role = role)
            repository.insertUser(user)
            _currentUser.value = user
            _isAuthLoading.value = false
            addLog("User $displayName logged in successfully ($role).")
        }
    }

    fun signupWithEmail(name: String, email: String, pword: String) {
        if (name.isBlank() || email.isBlank() || pword.isBlank()) {
            _authError.value = "All parameters are mandatory."
            return
        }
        _isAuthLoading.value = true
        _authError.value = null
        viewModelScope.launch {
            delay(1000)
            val id = "user_" + email.hashCode().coerceAtLeast(0)
            val user = UserEntity(id = id, email = email, displayName = name, role = "USER")
            repository.insertUser(user)
            _currentUser.value = user
            _isAuthLoading.value = false
            addLog("User registration complete: $name ($email)")
        }
    }

    fun loginWithGoogle() {
        _isAuthLoading.value = true
        _authError.value = null
        viewModelScope.launch {
            delay(800)
            val user = UserEntity(id = "google_user", email = "google.student@gmail.com", displayName = "Google Student", role = "USER")
            repository.insertUser(user)
            _currentUser.value = user
            _isAuthLoading.value = false
            addLog("Logged in via Google Sign-In.")
        }
    }

    fun requestPasswordReset(email: String) {
        if (email.isBlank()) {
            _authError.value = "Please enter your email."
            return
        }
        viewModelScope.launch {
            addLog("Password reset link request transmitted for $email.")
            repository.insertNotification(
                NotificationEntity(
                    title = "Security Alert",
                    message = "Password reset instructions sent to $email.",
                    type = "SYSTEM"
                )
            )
        }
    }

    fun logout() {
        _currentUser.value = null
        addLog("User logged out.")
    }

    // --- Core Feature: Exam Countdowns ---
    fun addExam(title: String, dateTime: Long, categoryId: Int, notes: String, isFavorite: Boolean, prepProgress: Int) {
        viewModelScope.launch {
            val exam = ExamEntity(
                title = title,
                dateTime = dateTime,
                categoryId = categoryId,
                notes = notes,
                isFavorite = isFavorite,
                preparationProgress = prepProgress.coerceIn(0, 100)
            )
            repository.insertExam(exam)
            addLog("Created exam: '$title'")
            triggerNotificationForNewExam(title, dateTime)
        }
    }

    fun editExam(id: Int, title: String, dateTime: Long, categoryId: Int, notes: String, isFavorite: Boolean, prepProgress: Int) {
        viewModelScope.launch {
            val exam = ExamEntity(
                id = id,
                title = title,
                dateTime = dateTime,
                categoryId = categoryId,
                notes = notes,
                isFavorite = isFavorite,
                preparationProgress = prepProgress.coerceIn(0, 100)
            )
            repository.updateExam(exam)
            addLog("Modified exam id: $id ('$title')")
        }
    }

    fun deleteExam(id: Int, title: String) {
        viewModelScope.launch {
            repository.deleteExam(id)
            addLog("Deleted exam '$title'")
        }
    }

    fun duplicateExam(exam: ExamEntity) {
        viewModelScope.launch {
            val duplicated = exam.copy(
                id = 0,
                title = "${exam.title} (Copy)",
                isDuplicated = true
            )
            repository.insertExam(duplicated)
            addLog("Duplicated exam: '${exam.title}'")
        }
    }

    fun toggleExamFavorite(exam: ExamEntity) {
        viewModelScope.launch {
            val updated = exam.copy(isFavorite = !exam.isFavorite)
            repository.updateExam(updated)
            addLog("Favorite toggled for '${exam.title}'")
        }
    }

    fun updateExamProgress(exam: ExamEntity, newProgress: Int) {
        viewModelScope.launch {
            val updated = exam.copy(preparationProgress = newProgress.coerceIn(0, 100))
            repository.updateExam(updated)
            addLog("Updated registration: '${exam.title}' progress to $newProgress%")
        }
    }

    // --- Core Feature: Categories ---
    fun addCustomCategory(name: String, colorHex: String) {
        viewModelScope.launch {
            val category = CategoryEntity(name = name, colorHex = colorHex, isCustom = true)
            repository.insertCategory(category)
            addLog("Added custom category: '$name'")
        }
    }

    fun deleteCategory(id: Int, name: String) {
        viewModelScope.launch {
            repository.deleteCategory(id)
            addLog("Deleted category: '$name'")
        }
    }

    // --- Core Feature: Study Session Tracker / Pomodoro ---
    fun setDailyGoal(minutes: Int) {
        _dailyGoalMinutes.value = minutes
        addLog("Daily study goal updated to $minutes mins.")
    }

    fun startStudyTimer(examId: Int) {
        activeTimerExamId = examId
        _isTimerRunning.value = true
        activeStudyJob?.cancel()
        activeStudyJob = viewModelScope.launch {
            while (_isTimerRunning.value && _timerSecondsRemaining.value > 0) {
                delay(1000)
                _timerSecondsRemaining.value -= 1
            }
            if (_timerSecondsRemaining.value == 0) {
                // Done! Complete session
                completeCurrentStudySession()
            }
        }
        addLog("Study timer started for Exam Id $examId.")
    }

    fun stopStudyTimer() {
        _isTimerRunning.value = false
        activeStudyJob?.cancel()
        addLog("Study timer paused manually.")
    }

    fun resetStudyTimer(durationMinutes: Int) {
        _isTimerRunning.value = false
        activeStudyJob?.cancel()
        _timerSecondsRemaining.value = durationMinutes * 60
        addLog("Study timer reset to $durationMinutes minutes.")
    }

    fun completeCurrentStudySession() {
        val durationMins = (1500 - _timerSecondsRemaining.value) / 60
        val minsLogged = if (durationMins <= 0) 25 else durationMins // Default to mock session of 25 min if done
        saveStudySessionDirect(activeTimerExamId, minsLogged, "Completed focused Pomodoro study interval.")
        resetStudyTimer(25)
    }

    fun saveStudySessionDirect(examId: Int, durationMins: Int, notes: String) {
        viewModelScope.launch {
            val exam = repository.getExamById(examId)
            val subjectStr = exam?.title ?: "General Study"
            val session = StudySessionEntity(
                examId = examId,
                subjectName = subjectStr,
                durationMinutes = durationMins,
                notes = notes
            )
            repository.insertStudySession(session)

            // Update daily analytics
            val todayTruncated = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val currentAnalytics = repository.allAnalytics.firstOrNull()?.find { it.date == todayTruncated }
            if (currentAnalytics != null) {
                repository.insertAnalytics(
                    currentAnalytics.copy(
                        totalStudyMinutes = currentAnalytics.totalStudyMinutes + durationMins
                    )
                )
            } else {
                repository.insertAnalytics(
                    AnalyticsEntity(
                        date = todayTruncated,
                        totalStudyMinutes = durationMins
                    )
                )
            }

            // Create notification alert
            repository.insertNotification(
                NotificationEntity(
                    title = "Study Session Logged!",
                    message = "Awesome! You studied '$subjectStr' for $durationMins minutes. Your statistics have been updated.",
                    type = "REMINDER"
                )
            )
            addLog("Saved study session: $durationMins mins of '$subjectStr'")
        }
    }

    // --- Core Feature: Smart Simulated Notifications ---
    private suspend fun triggerNotificationForNewExam(title: String, dateTime: Long) {
        val formattedDate = Calendar.getInstance().apply { timeInMillis = dateTime }.let {
            "${it.get(Calendar.YEAR)}-${it.get(Calendar.MONTH) + 1}-${it.get(Calendar.DAY_OF_MONTH)}"
        }
        val notif = NotificationEntity(
            title = "New Exam Added",
            message = "Your countdown timer for '$title' scheduled on $formattedDate has begun ticking!",
            type = "REMINDER"
        )
        repository.insertNotification(notif)
    }

    fun deleteNotification(id: Int) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }

    fun markNotificationAsRead(id: Int) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    // --- Admin Operations & Control Panels ---
    fun banUser(userId: String, name: String) {
        viewModelScope.launch {
            repository.getUser(userId).firstOrNull()?.let {
                val updated = it.copy(isBanned = true)
                repository.updateUser(updated)
                addLog("Banned user: $name (ID: $userId)")
                repository.insertNotification(
                    NotificationEntity(
                        title = "Administrative Action",
                        message = "User $name has been locked by administrator.",
                        type = "ALERT"
                    )
                )
            }
        }
    }

    fun unbanUser(userId: String, name: String) {
        viewModelScope.launch {
            repository.getUser(userId).firstOrNull()?.let {
                val updated = it.copy(isBanned = false)
                repository.updateUser(updated)
                addLog("Unbanned user: $name (ID: $userId)")
            }
        }
    }

    fun deleteUser(userId: String, name: String) {
        viewModelScope.launch {
            repository.deleteUser(userId)
            addLog("Deleted user account: $name (ID: $userId)")
        }
    }

    fun setFeatureToggle(key: String, enabled: Boolean) {
        val updated = _featureToggles.value.toMutableMap()
        updated[key] = enabled
        _featureToggles.value = updated
        addLog("Admin toggle '$key' set to $enabled")
    }

    fun sendGlobalAnnouncement(title: String, body: String) {
        if (title.isBlank() || body.isBlank()) return
        viewModelScope.launch {
            val announcement = NotificationEntity(
                title = "[Global Announcement] $title",
                message = body,
                type = "SYSTEM"
            )
            repository.insertNotification(announcement)
            addLog("Published announcements: '$title'")
        }
    }

    fun triggerGlobalPushNotification(title: String, text: String) {
        viewModelScope.launch {
            repository.insertNotification(
                NotificationEntity(
                    title = "🔔 Push Alert: $title",
                    message = text,
                    type = "ALERT"
                )
            )
            addLog("FCM/APNS Push alert sent to all devices.")
        }
    }

    fun triggerDatabaseBackup() {
        viewModelScope.launch {
            delay(1200) // simulated backup latency
            addLog("Secure sqlite db copy exported successfully to cloud cloud-backup-v1.sqlite")
            repository.insertNotification(
                NotificationEntity(
                    title = "Database Backup Succeeded",
                    message = "All user countdowns, sessions, and settings successfully encrypted and backed up.",
                    type = "SYSTEM"
                )
            )
        }
    }

    fun triggerDatabaseRestore() {
        viewModelScope.launch {
            delay(1500) // simulated restore latency
            addLog("Secure cloud backup state successfully verified and loaded.")
        }
    }
}
