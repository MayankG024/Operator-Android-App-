package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface OperatorDao {

    // --- User Queries ---
    @Query("SELECT * FROM users WHERE id = 1 LIMIT 1")
    fun getUserFlow(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = 1 LIMIT 1")
    suspend fun getUserSync(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    // --- Mission Queries ---
    @Query("SELECT * FROM missions ORDER BY focusOrder ASC, id DESC")
    fun getAllMissionsFlow(): Flow<List<MissionEntity>>

    @Query("SELECT * FROM missions WHERE dateString = :date ORDER BY focusOrder ASC")
    fun getMissionsForDateFlow(date: String): Flow<List<MissionEntity>>

    @Query("SELECT * FROM missions WHERE dateString = :date ORDER BY focusOrder ASC")
    suspend fun getMissionsForDateSync(date: String): List<MissionEntity>

    @Query("SELECT * FROM missions WHERE id = :id LIMIT 1")
    fun getMissionFlow(id: Int): Flow<MissionEntity?>

    @Query("SELECT * FROM missions WHERE id = :id LIMIT 1")
    suspend fun getMissionSync(id: Int): MissionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMission(mission: MissionEntity): Long

    @Update
    suspend fun updateMission(mission: MissionEntity)

    @Delete
    suspend fun deleteMission(mission: MissionEntity)

    // --- Subtask Queries ---
    @Query("SELECT * FROM subtasks WHERE missionId = :missionId")
    fun getSubtasksForMissionFlow(missionId: Int): Flow<List<SubtaskEntity>>

    @Query("SELECT * FROM subtasks WHERE missionId = :missionId")
    suspend fun getSubtasksForMissionSync(missionId: Int): List<SubtaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtask(subtask: SubtaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtasks(subtasks: List<SubtaskEntity>)

    @Update
    suspend fun updateSubtask(subtask: SubtaskEntity)

    @Query("DELETE FROM subtasks WHERE missionId = :missionId")
    suspend fun deleteSubtasksForMission(missionId: Int)

    // --- Focus Session Queries ---
    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC")
    fun getAllFocusSessionsFlow(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE missionId = :missionId")
    fun getFocusSessionsForMissionFlow(missionId: Int): Flow<List<FocusSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusSession(session: FocusSessionEntity): Long

    // --- Review Queries ---
    @Query("SELECT * FROM reviews ORDER BY dateString DESC")
    fun getAllReviewsFlow(): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews WHERE dateString = :dateString LIMIT 1")
    suspend fun getReviewForDateSync(dateString: String): ReviewEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)

    // --- XP Event Queries ---
    @Query("SELECT * FROM xp_events ORDER BY timestamp DESC")
    fun getAllXPEventsFlow(): Flow<List<XPEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertXPEvent(event: XPEventEntity): Long

    // --- Skills Queries ---
    @Query("SELECT * FROM skills")
    fun getAllSkillsFlow(): Flow<List<SkillEntity>>

    @Query("SELECT * FROM skills WHERE category = :category LIMIT 1")
    suspend fun getSkillSync(category: String): SkillEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkill(skill: SkillEntity)

    // --- Achievements Queries ---
    @Query("SELECT * FROM achievements")
    fun getAllAchievementsFlow(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)

    // --- Boss Mission Queries ---
    @Query("SELECT * FROM boss_missions ORDER BY createdAt DESC")
    fun getAllBossMissionsFlow(): Flow<List<BossMissionEntity>>

    @Query("SELECT * FROM boss_missions WHERE id = :id LIMIT 1")
    suspend fun getBossMissionSync(id: Int): BossMissionEntity?

    @Query("SELECT * FROM boss_missions WHERE id = :id LIMIT 1")
    fun getBossMissionFlow(id: Int): Flow<BossMissionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBossMission(bossMission: BossMissionEntity): Long

    @Update
    suspend fun updateBossMission(bossMission: BossMissionEntity)

    // --- Insights Queries ---
    @Query("SELECT * FROM insights ORDER BY timestamp DESC")
    fun getAllInsightsFlow(): Flow<List<InsightEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInsight(insight: InsightEntity): Long
}
