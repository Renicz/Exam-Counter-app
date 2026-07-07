package com.example.data.repository

import com.example.data.db.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import java.util.Calendar

class ExamRepository(private val db: AppDatabase) {

    // DAOs
    private val userDao = db.userDao()
    private val examDao = db.examDao()
    private val categoryDao = db.categoryDao()
    private val notificationDao = db.notificationDao()
    private val studySessionDao = db.studySessionDao()
    private val adminDao = db.adminDao()
    private val analyticsDao = db.analyticsDao()
    private val settingsDao = db.settingsDao()

    // 1. Users Operations
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsersFlow()
    fun getUser(userId: String): Flow<UserEntity?> = userDao.getUserById(userId)
    suspend fun insertUser(user: UserEntity) = userDao.insertUser(user)
    suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)
    suspend fun deleteUser(userId: String) = userDao.deleteUserById(userId)

    // 2. Exams Operations
    val allExams: Flow<List<ExamEntity>> = examDao.getAllExamsFlow()
    suspend fun getExamById(id: Int): ExamEntity? = examDao.getExamById(id)
    suspend fun insertExam(exam: ExamEntity): Long = examDao.insertExam(exam)
    suspend fun updateExam(exam: ExamEntity) = examDao.updateExam(exam)
    suspend fun deleteExam(id: Int) = examDao.deleteExamById(id)

    // 3. Category Operations
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategoriesFlow()
    suspend fun insertCategory(category: CategoryEntity): Long = categoryDao.insertCategory(category)
    suspend fun deleteCategory(id: Int) = categoryDao.deleteCategoryById(id)

    // 4. Notifications Operations
    val allNotifications: Flow<List<NotificationEntity>> = notificationDao.getAllNotificationsFlow()
    suspend fun insertNotification(notification: NotificationEntity) = notificationDao.insertNotification(notification)
    suspend fun markNotificationAsRead(id: Int) = notificationDao.markAsRead(id)
    suspend fun deleteNotification(id: Int) = notificationDao.deleteNotificationById(id)

    // 5. Study Sessions Operations
    val allSessions: Flow<List<StudySessionEntity>> = studySessionDao.getAllSessionsFlow()
    fun getSessionsForExam(examId: Int): Flow<List<StudySessionEntity>> = studySessionDao.getSessionsForExam(examId)
    suspend fun insertStudySession(session: StudySessionEntity) = studySessionDao.insertSession(session)

    // 6. Admin Panel Operations
    fun getAdminState(userId: String): Flow<AdminEntity?> = adminDao.getAdminById(userId)
    suspend fun registerAdmin(admin: AdminEntity) = adminDao.insertAdmin(admin)

    // 7. Analytics Operations
    val allAnalytics: Flow<List<AnalyticsEntity>> = analyticsDao.getAllAnalyticsFlow()
    suspend fun insertAnalytics(analytics: AnalyticsEntity) = analyticsDao.insertAnalytics(analytics)

    // 8. Settings Operations
    fun getSetting(key: String): Flow<SettingsEntity?> = settingsDao.getSettingFlow(key)
    suspend fun getSettingSync(key: String): SettingsEntity? = settingsDao.getSettingValueSync(key)
    suspend fun saveSetting(key: String, value: String) {
        settingsDao.saveSetting(SettingsEntity(key, value))
    }

    // Seeding default categories list and mock data if empty
    suspend fun seedDatabaseIfNeeded() {
        // Core Category seed check
        val currentCategories = allCategories.firstOrNull() ?: emptyList()
        if (currentCategories.isEmpty()) {
            val defaults = listOf(
                CategoryEntity(name = "School Exams", colorHex = "#3b82f6", isCustom = false),
                CategoryEntity(name = "Board Exams", colorHex = "#ec4899", isCustom = false),
                CategoryEntity(name = "Competitive Exams", colorHex = "#f59e0b", isCustom = false),
                CategoryEntity(name = "University Exams", colorHex = "#a855f7", isCustom = false),
                CategoryEntity(name = "Custom", colorHex = "#10b981", isCustom = false)
            )
            val ids = mutableListOf<Long>()
            for (category in defaults) {
                ids.add(categoryDao.insertCategory(category))
            }

            // Create initial mock users for admin oversight
            val mockUsers = listOf(
                UserEntity(id = "user_1", email = "alex.rivera@university.edu", displayName = "Alex Rivera", isBanned = false, role = "USER"),
                UserEntity(id = "user_2", email = "sophia.lee@highschool.org", displayName = "Sophia Lee", isBanned = false, role = "USER"),
                UserEntity(id = "user_3", email = "marcus.vance@boardprep.com", displayName = "Marcus Vance", isBanned = true, role = "USER")
            )
            for (user in mockUsers) {
                userDao.insertUser(user)
            }

            // Insert initial default exams to showcase countdowns beautifully on startup
            val cal = Calendar.getInstance()
            val schoolCatId = ids.getOrElse(0) { 1L }.toInt()
            val boardCatId = ids.getOrElse(1) { 2L }.toInt()
            val compCatId = ids.getOrElse(2) { 3L }.toInt()
            val uniCatId = ids.getOrElse(3) { 4L }.toInt()

            cal.add(Calendar.DAY_OF_YEAR, 2)
            cal.set(Calendar.HOUR_OF_DAY, 9)
            examDao.insertExam(ExamEntity(title = "AP Calculus Final Exam", dateTime = cal.timeInMillis, categoryId = schoolCatId, notes = "Remember to review integration derivatives sheets.", isFavorite = true, preparationProgress = 65))

            cal.add(Calendar.DAY_OF_YEAR, 10)
            cal.set(Calendar.HOUR_OF_DAY, 14)
            examDao.insertExam(ExamEntity(title = "SAT Board Test", dateTime = cal.timeInMillis, categoryId = boardCatId, notes = "Calculator fully charged, pencils, and formula sheet checked.", isFavorite = false, preparationProgress = 80))

            cal.add(Calendar.DAY_OF_YEAR, 25)
            examDao.insertExam(ExamEntity(title = "Medical College Entrance MCAT", dateTime = cal.timeInMillis, categoryId = compCatId, notes = "Focus on Biology and Organic Chemistry flashcards.", isFavorite = true, preparationProgress = 40))

            cal.add(Calendar.DAY_OF_YEAR, 45)
            examDao.insertExam(ExamEntity(title = "Data Structures Midterm", dateTime = cal.timeInMillis, categoryId = uniCatId, notes = "Graphs, trees, and dynamic programming concepts.", isFavorite = false, preparationProgress = 20))

            // Seed initial notifications that appear in dashboard alerts
            notificationDao.insertNotification(
                NotificationEntity(
                    title = "Welcome to Countdown Pro!",
                    message = "Let's organize your exam schedules, daily goals, and build consistent study streaks! 🚀",
                    type = "SYSTEM"
                )
            )

            // Seed initial study session history for analytics graphics representation
            val today = System.currentTimeMillis()
            studySessionDao.insertSession(StudySessionEntity(examId = 1, subjectName = "AP Calculus", durationMinutes = 45, date = today - (86400000 * 4), notes = "Practiced derivatives questions."))
            studySessionDao.insertSession(StudySessionEntity(examId = 1, subjectName = "AP Calculus", durationMinutes = 60, date = today - (86400000 * 3), notes = "Full mock test section A."))
            studySessionDao.insertSession(StudySessionEntity(examId = 2, subjectName = "SAT Board Test", durationMinutes = 90, date = today - (86400000 * 2), notes = "Reading comprehension review."))
            studySessionDao.insertSession(StudySessionEntity(examId = 3, subjectName = "MCAT Prep", durationMinutes = 120, date = today - 86400000, notes = "In-depth biology test revision."))
            studySessionDao.insertSession(StudySessionEntity(examId = 1, subjectName = "AP Calculus", durationMinutes = 55, date = today, notes = "Calculated double integral formulas."))

            // Seed corresponding analytics days
            analyticsDao.insertAnalytics(AnalyticsEntity(date = today - (86400000 * 4), totalStudyMinutes = 45, examsCompleted = 0))
            analyticsDao.insertAnalytics(AnalyticsEntity(date = today - (86400000 * 3), totalStudyMinutes = 60, examsCompleted = 0))
            analyticsDao.insertAnalytics(AnalyticsEntity(date = today - (86400000 * 2), totalStudyMinutes = 90, examsCompleted = 0))
            analyticsDao.insertAnalytics(AnalyticsEntity(date = today - 86400000, totalStudyMinutes = 120, examsCompleted = 0))
            analyticsDao.insertAnalytics(AnalyticsEntity(date = today, totalStudyMinutes = 55, examsCompleted = 0))
        }
    }
}
