package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val email: String,
    val level: Int = 1,
    val xp: Int = 0,
    val lifetimeXp: Int = 0,
    val planningStreak: Int = 0,
    val executionStreak: Int = 0,
    val reflectionStreak: Int = 0,
    val lastPlanningDate: String? = null,
    val lastExecutionDate: String? = null,
    val lastReflectionDate: String? = null
)

@Entity(tableName = "missions")
data class MissionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val refinedOutcome: String = "",
    val successCriteria: String = "", // Comma-separated or newline-separated values
    val difficulty: String = "Medium", // Easy, Medium, Hard, Epic
    val estimatedMinutes: Int = 0,
    val focusOrder: Int = 0,
    val category: String = "Learning", // Development, Learning, Career, Business, Fitness, Writing
    val status: String = "PENDING", // PENDING, IN_PROGRESS, COMPLETED, DELAYED, BLOCKED
    val blockerReason: String? = null,
    val isBossMission: Boolean = false,
    val bossMissionId: Int? = null,
    val dateString: String, // YYYY-MM-DD
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "subtasks")
data class SubtaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val missionId: Int,
    val title: String,
    val isCompleted: Boolean = false,
    val secondsSpent: Int = 0
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val missionId: Int,
    val dateString: String, // YYYY-MM-DD
    val durationSeconds: Int,
    val completed: Boolean,
    val interruptions: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey val dateString: String, // YYYY-MM-DD (unique per evening)
    val summary: String,
    val lessonsLearned: String,
    val suggestedAdjustments: String,
    val energyLevel: Int, // 1 - 10
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "xp_events")
data class XPEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Int,
    val source: String, // e.g., "Completed Mission: Finish Shopify collection page"
    val category: String, // e.g., "Development"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "skills")
data class SkillEntity(
    @PrimaryKey val category: String, // Development, Learning, Career, Business, Fitness, Writing
    val xp: Int = 0,
    val level: Int = 1
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null
)

@Entity(tableName = "boss_missions")
data class BossMissionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val progressPercentage: Float = 0f,
    val isCompleted: Boolean = false,
    val rewardXp: Int = 500,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "insights")
data class InsightEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val content: String,
    val type: String, // productivity, estimation, blocker
    val dateString: String,
    val timestamp: Long = System.currentTimeMillis()
)
