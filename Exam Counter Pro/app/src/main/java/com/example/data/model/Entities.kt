package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// 1. Users Table
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val displayName: String,
    val isBanned: Boolean = false,
    val role: String = "USER", // "USER" or "ADMIN"
    val registerDate: Long = System.currentTimeMillis()
)

// 2. Exams Table
@Entity(tableName = "exams")
data class ExamEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val dateTime: Long, // timestamp in ms
    val categoryId: Int, // references Category ID
    val notes: String = "",
    val isFavorite: Boolean = false,
    val preparationProgress: Int = 0, // 0 to 100%
    val isDuplicated: Boolean = false
)

// 3. Categories Table
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val colorHex: String, // String color code e.g. "#4FACFE"
    val isCustom: Boolean = false
)

// 4. Notifications Table
@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val dateTime: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: String = "REMINDER" // "REMINDER", "SYSTEM", "ALERT"
)

// 5. Study Sessions Table
@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val examId: Int, // references Exam ID (0 for general study)
    val subjectName: String,
    val durationMinutes: Int,
    val date: Long = System.currentTimeMillis(),
    val notes: String = ""
)

// 6. Admin Table
@Entity(tableName = "admins")
data class AdminEntity(
    @PrimaryKey val userId: String,
    val adminLevel: String = "SUPER_ADMIN", // "SUPER_ADMIN" or "MODERATOR"
    val isRoot: Boolean = true
)

// 7. Analytics Table
@Entity(tableName = "analytics")
data class AnalyticsEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long, // truncate to day
    val totalStudyMinutes: Int = 0,
    val examsCompleted: Int = 0
)

// 8. Settings Table
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val settingKey: String,
    val settingValue: String
)
