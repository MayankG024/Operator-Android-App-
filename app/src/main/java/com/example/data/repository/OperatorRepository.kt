package com.example.data.repository

import android.util.Log
import com.example.data.database.*
import com.example.data.network.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*

class OperatorRepository(
    private val dao: OperatorDao,
    private val geminiService: GeminiService = GeminiService()
) {

    // --- Date Helpers ---
    fun getCurrentDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun getTomorrowDateString(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, 1)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }

    fun getYesterdayDateString(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -1)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }

    // --- Inits & Checks ---
    suspend fun initDefaultDataIfNeeded() {
        val user = dao.getUserSync()
        if (user == null) {
            val newUser = UserEntity(
                name = "Operator",
                email = "user@operator.ai",
                level = 1,
                xp = 0,
                lifetimeXp = 0,
                planningStreak = 0,
                executionStreak = 0,
                reflectionStreak = 0
            )
            dao.insertUser(newUser)

            // Insert initial skills
            val categories = listOf("Development", "Learning", "Career", "Business", "Fitness", "Writing")
            for (cat in categories) {
                if (dao.getSkillSync(cat) == null) {
                    dao.insertSkill(SkillEntity(category = cat, xp = 0, level = 1))
                }
            }

            // Insert achievements
            val initialAchievements = listOf(
                AchievementEntity("first_mission", "First Step Complete", "Successfully execute and finish your first daily mission.", isUnlocked = false),
                AchievementEntity("7_day_streak", "Absolute Execution", "Maintain an execution streak of 7 successful days.", isUnlocked = false),
                AchievementEntity("10_hours_focus", "Deep Work Specialist", "Accumulate more than 10 hours of active focus sessions.", isUnlocked = false),
                AchievementEntity("first_boss", "Giant Slayer", "Complete your first multi-day Boss Mission.", isUnlocked = false)
            )
            dao.insertAchievements(initialAchievements)
        }
    }

    // --- Flows ---
    val userFlow: Flow<UserEntity?> = dao.getUserFlow()
    val allMissionsFlow: Flow<List<MissionEntity>> = dao.getAllMissionsFlow()
    val allSkillsFlow: Flow<List<SkillEntity>> = dao.getAllSkillsFlow()
    val allAchievementsFlow: Flow<List<AchievementEntity>> = dao.getAllAchievementsFlow()
    val allBossMissionsFlow: Flow<List<BossMissionEntity>> = dao.getAllBossMissionsFlow()
    val allInsightsFlow: Flow<List<InsightEntity>> = dao.getAllInsightsFlow()
    val allReviewsFlow: Flow<List<ReviewEntity>> = dao.getAllReviewsFlow()
    val allXpEventsFlow: Flow<List<XPEventEntity>> = dao.getAllXPEventsFlow()

    fun getMissionsForDateFlow(date: String): Flow<List<MissionEntity>> {
        return dao.getMissionsForDateFlow(date)
    }

    fun getSubtasksForMissionFlow(missionId: Int): Flow<List<SubtaskEntity>> {
        return dao.getSubtasksForMissionFlow(missionId)
    }

    fun getMissionFlow(missionId: Int): Flow<MissionEntity?> {
        return dao.getMissionFlow(missionId)
    }

    fun getBossMissionFlow(id: Int): Flow<BossMissionEntity?> {
        return dao.getBossMissionFlow(id)
    }

    // --- CRUD Write Methods ---
    suspend fun insertMission(mission: MissionEntity): Long {
        return dao.insertMission(mission)
    }

    suspend fun updateMission(mission: MissionEntity) {
        dao.updateMission(mission)
    }

    suspend fun deleteMission(mission: MissionEntity) {
        dao.deleteSubtasksForMission(mission.id)
        dao.deleteMission(mission)
    }

    suspend fun insertSubtask(subtask: SubtaskEntity): Long {
        return dao.insertSubtask(subtask)
    }

    suspend fun updateSubtask(subtask: SubtaskEntity) {
        dao.updateSubtask(subtask)
    }

    suspend fun insertBossMission(boss: BossMissionEntity): Long {
        return dao.insertBossMission(boss)
    }

    suspend fun updateBossMission(boss: BossMissionEntity) {
        dao.updateBossMission(boss)
    }

    suspend fun getMissionsForDateSync(date: String): List<MissionEntity> {
        return dao.getMissionsForDateSync(date)
    }

    // --- Gamification logic: Complete Mission and Award XP ---
    suspend fun completeSubtaskAndTrackProgress(subtask: SubtaskEntity) {
        dao.updateSubtask(subtask)
        
        // Let's recalculate boss mission progress if this mission is linked to a boss mission
        val mission = dao.getMissionSync(subtask.missionId) ?: return
        if (mission.isBossMission && mission.bossMissionId != null) {
            recalculateBossMissionProgress(mission.bossMissionId)
        }
    }

    suspend fun completeMission(missionId: Int) {
        val mission = dao.getMissionSync(missionId) ?: return
        if (mission.status == "COMPLETED") return

        val completedMission = mission.copy(status = "COMPLETED")
        dao.updateMission(completedMission)

        // Award XP based on Difficulty
        val xpAmt = when (mission.difficulty) {
            "Easy" -> 25
            "Medium" -> 50
            "Hard" -> 100
            "Epic" -> 250
            else -> 50
        }

        // Add XP Event
        val sourceStr = "Completed Mission: ${mission.title}"
        dao.insertXPEvent(XPEventEntity(amount = xpAmt, source = sourceStr, category = mission.category))

        // Update Skill XP
        updateSkillXp(mission.category, xpAmt)

        // Add to User XP
        awardUserXp(xpAmt)

        // Check and trigger "First Mission Complete" Achievement
        unlockAchievement("first_mission")

        // Track execution streak
        updateExecutionStreak()

        // If this is a boss mission, recalculate and check if boss mission completed
        if (mission.isBossMission && mission.bossMissionId != null) {
            recalculateBossMissionProgress(mission.bossMissionId)
        }

        // Bonus XP for completing all missions today
        checkAndAwardAllMissionsCompletedBonus(completedMission.dateString)
    }

    suspend fun recordFocusSession(missionId: Int, durationSeconds: Int, completed: Boolean, interruptions: Int) {
        val dateString = getCurrentDateString()
        val session = FocusSessionEntity(
            missionId = missionId,
            dateString = dateString,
            durationSeconds = durationSeconds,
            completed = completed,
            interruptions = interruptions
        )
        dao.insertFocusSession(session)

        // Award XP for focused work: 1 XP per minute of focus, up to max 50 XP
        val focusMinutes = durationSeconds / 60
        if (focusMinutes > 0) {
            val focusXp = focusMinutes.coerceAtMost(50)
            val sourceStr = "Deep Focus Work (${focusMinutes}m)"
            val mission = dao.getMissionSync(missionId)
            val category = mission?.category ?: "Learning"

            dao.insertXPEvent(XPEventEntity(amount = focusXp, source = sourceStr, category = category))
            updateSkillXp(category, focusXp)
            awardUserXp(focusXp)
        }

        // Check if 10 Focus Hours Achievement holds true now
        checkFocusHoursAchievement()
    }

    private suspend fun recalculateBossMissionProgress(bossId: Int) {
        val boss = dao.getBossMissionSync(bossId) ?: return
        
        // Find all missions pointing to this boss mission
        val allMissions = dao.getAllMissionsFlow().firstOrNull() ?: emptyList()
        val bossMissions = allMissions.filter { it.isBossMission && it.bossMissionId == bossId }
        
        if (bossMissions.isEmpty()) return

        val completedCount = bossMissions.count { it.status == "COMPLETED" }
        val progress = (completedCount.toFloat() / bossMissions.size.toFloat()) * 100f
        val isNowCompleted = completedCount == bossMissions.size

        val updatedBoss = boss.copy(
            progressPercentage = progress,
            isCompleted = isNowCompleted
        )
        dao.updateBossMission(updatedBoss)

        if (isNowCompleted && !boss.isCompleted) {
            // Unlocked first boss mission achievement
            unlockAchievement("first_boss")
            // Award boss XP
            val bossBonusXp = boss.rewardXp
            dao.insertXPEvent(XPEventEntity(amount = bossBonusXp, source = "Conquered Boss Mission: ${boss.title}", category = "Career"))
            awardUserXp(bossBonusXp)
        }
    }

    private suspend fun checkAndAwardAllMissionsCompletedBonus(dateString: String) {
        val dailyMissions = dao.getMissionsForDateSync(dateString)
        if (dailyMissions.isNotEmpty() && dailyMissions.all { it.status == "COMPLETED" }) {
            val bonusAmt = 75
            val sourceStr = "Daily Mission Swarm Cleared!"
            dao.insertXPEvent(XPEventEntity(amount = bonusAmt, source = sourceStr, category = "Learning"))
            awardUserXp(bonusAmt)
        }
    }

    private suspend fun checkFocusHoursAchievement() {
        val sessions = dao.getAllFocusSessionsFlow().firstOrNull() ?: emptyList()
        val totalSeconds = sessions.sumOf { it.durationSeconds }
        val totalHours = totalSeconds / 3600f
        if (totalHours >= 10f) {
            unlockAchievement("10_hours_focus")
        }
    }

    private suspend fun unlockAchievement(achievementId: String) {
        val achievements = dao.getAllAchievementsFlow().firstOrNull() ?: return
        val achievement = achievements.find { it.id == achievementId }
        if (achievement != null && !achievement.isUnlocked) {
            val updated = achievement.copy(isUnlocked = true, unlockedAt = System.currentTimeMillis())
            dao.updateAchievement(updated)

            // Award 150 Achievement XP!
            val rewardXp = 150
            dao.insertXPEvent(XPEventEntity(amount = rewardXp, source = "Unlocked Achievement: ${achievement.name}", category = "Learning"))
            awardUserXp(rewardXp)
        }
    }

    private suspend fun updateSkillXp(category: String, amount: Int) {
        val skill = dao.getSkillSync(category) ?: SkillEntity(category = category, xp = 0, level = 1)
        val newXp = skill.xp + amount
        
        // Progression level formula for skills: level = 1 + (xp / 150)
        val newLevel = 1 + (newXp / 150)
        
        dao.insertSkill(skill.copy(xp = newXp, level = newLevel))
    }

    private suspend fun awardUserXp(amount: Int) {
        val user = dao.getUserSync() ?: return
        val newXp = user.xp + amount
        val lifetimeXp = user.lifetimeXp + amount
        
        // Level up formula: each level needs level * 100 XP (Level 1: 100 XP, Level 2: 200 XP, Level 3: 300 XP, etc.)
        var level = user.level
        var xpLeft = newXp
        var xpRequiredForNext = level * 100

        while (xpLeft >= xpRequiredForNext) {
            xpLeft -= xpRequiredForNext
            level++
            xpRequiredForNext = level * 100
        }

        val updatedUser = user.copy(
            xp = xpLeft,
            level = level,
            lifetimeXp = lifetimeXp
        )
        dao.updateUser(updatedUser)
    }

    // --- Streak Logic with Graceful Street Protection ---
    private suspend fun updateExecutionStreak() {
        val user = dao.getUserSync() ?: return
        val today = getCurrentDateString()
        val lastExec = user.lastExecutionDate

        if (lastExec == today) return // already tracked execution today

        val yesterday = getYesterdayDateString()
        var newStreak = user.executionStreak

        if (lastExec == yesterday) {
            newStreak++
        } else if (lastExec == null) {
            newStreak = 1
        } else {
            // Streak broken, unless we use streak protection!
            // Check if user missed exactly 2 days (e.g. last execution was day before yesterday) or 1 day
            val cal = Calendar.getInstance()
            cal.add(Calendar.DATE, -2)
            val dayBeforeYesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
            
            if (lastExec == dayBeforeYesterday) {
                // Streak Protected! Handled as grace day.
                newStreak++
                Log.d("OperatorRepository", "Execution streak protected!")
            } else {
                newStreak = 1
            }
        }

        dao.updateUser(user.copy(
            executionStreak = newStreak,
            lastExecutionDate = today
        ))

        if (newStreak == 7) {
            unlockAchievement("7_day_streak")
        }
    }

    suspend fun updatePlanningStreak() {
        val user = dao.getUserSync() ?: return
        val today = getCurrentDateString()
        val lastPlan = user.lastPlanningDate

        if (lastPlan == today) return // already planned today

        val yesterday = getYesterdayDateString()
        var newStreak = user.planningStreak

        if (lastPlan == yesterday) {
            newStreak++
        } else if (lastPlan == null) {
            newStreak = 1
        } else {
            // Check if day before yesterday
            val cal = Calendar.getInstance()
            cal.add(Calendar.DATE, -2)
            val dayBeforeYesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
            if (lastPlan == dayBeforeYesterday) {
                newStreak++ // grace protection
                Log.d("OperatorRepository", "Planning streak protected!")
            } else {
                newStreak = 1
            }
        }

        dao.updateUser(user.copy(
            planningStreak = newStreak,
            lastPlanningDate = today
        ))
    }

    suspend fun updateReflectionStreak() {
        val user = dao.getUserSync() ?: return
        val today = getCurrentDateString()
        val lastReflect = user.lastReflectionDate

        if (lastReflect == today) return

        val yesterday = getYesterdayDateString()
        var newStreak = user.reflectionStreak

        if (lastReflect == yesterday) {
            newStreak++
        } else if (lastReflect == null) {
            newStreak = 1
        } else {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DATE, -2)
            val dayBeforeYesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
            if (lastReflect == dayBeforeYesterday) {
                newStreak++ // grace protection
                Log.d("OperatorRepository", "Reflection streak protected!")
            } else {
                newStreak = 1
            }
        }

        dao.updateUser(user.copy(
            reflectionStreak = newStreak,
            lastReflectionDate = today
        ))
    }

    // --- AI Execution Service Integrations ---

    suspend fun executeNightPlanningGoals(rawGoals: String): List<MissionEntity> {
        val historyContext = getHistoricalPerformanceContext()
        val response = geminiService.enhanceGoals(rawGoals, historyContext)
        val tomorrowDate = getTomorrowDateString()

        val generatedMissions = response.missions.mapIndexed { idx, mission ->
            val missionEntity = MissionEntity(
                title = mission.title,
                refinedOutcome = mission.refinedOutcome,
                successCriteria = mission.successCriteria.joinToString("\n"),
                difficulty = mission.difficulty,
                estimatedMinutes = mission.estimatedMinutes,
                focusOrder = idx + 1,
                category = mission.category,
                status = "PENDING",
                dateString = tomorrowDate
            )
            val id = dao.insertMission(missionEntity).toInt()
            
            // Insert subtasks
            val subtasks = mission.subtasks.map { title ->
                SubtaskEntity(missionId = id, title = title, isCompleted = false)
            }
            dao.insertSubtasks(subtasks)
            
            missionEntity.copy(id = id)
        }

        // Successfully planned tomorrow -> update planning streak!
        updatePlanningStreak()

        return generatedMissions
    }

    suspend fun generateDailyBriefTextForDate(dateString: String): AIDailyBriefResponse {
        val dailyMissions = dao.getMissionsForDateSync(dateString)
        val listText = dailyMissions.joinToString("\n") { m ->
            "- ${m.title} (${m.difficulty}, ${m.estimatedMinutes} mins, ${m.category})"
        }
        return geminiService.generateDailyBrief(listText)
    }

    suspend fun executeEveningDebrief(
        dateString: String,
        completedMissionsList: List<MissionEntity>,
        failedMissionsList: List<MissionEntity>,
        failedReasons: String,
        energyLevel: Int
    ): ReviewEntity {
        val completedStr = completedMissionsList.joinToString(", ") { it.title }
        val failedStr = failedMissionsList.joinToString(", ") { "${it.title} (Reason: ${it.blockerReason ?: "Unknown"})" }

        val response = geminiService.runEveningDebriefAnalysis(
            completedStr,
            failedStr,
            failedReasons,
            energyLevel
        )

        val review = ReviewEntity(
            dateString = dateString,
            summary = response.summary,
            lessonsLearned = response.lessonsLearned,
            suggestedAdjustments = response.suggestedAdjustments,
            energyLevel = energyLevel
        )
        dao.insertReview(review)

        // Reflection completed -> update reflection streak!
        updateReflectionStreak()

        // Also compile and save any insights to the insight table
        generateAndPersistHourlyInsights()

        return review
    }

    suspend fun generateAndPersistHourlyInsights() {
        val allMissions = dao.getAllMissionsFlow().firstOrNull() ?: emptyList()
        val focusSessions = dao.getAllFocusSessionsFlow().firstOrNull() ?: emptyList()

        if (allMissions.size < 3) return // not enough data for rich insights

        val missionsText = allMissions.take(30).joinToString("\n") { m ->
            "- Title: ${m.title}, Category: ${m.category}, Status: ${m.status}, Difficulty: ${m.difficulty}, BlockedReason: ${m.blockerReason}"
        }
        val focusSessionsText = focusSessions.take(30).joinToString("\n") { f ->
            "- Mission ID: ${f.missionId}, Duration: ${f.durationSeconds}s, Completed: ${f.completed}, Interruptions: ${f.interruptions}"
        }

        try {
            val response = geminiService.generateInsights(missionsText, focusSessionsText)
            for (insightContent in response.insights) {
                dao.insertInsight(
                    InsightEntity(
                        content = insightContent,
                        type = "productivity",
                        dateString = getCurrentDateString()
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("OperatorRepository", "Failed to generate dynamic insights", e)
        }
    }

    suspend fun getCoachAdviceLive(missionId: Int, activeSubtask: String, userQuery: String): AICoachAdviceResponse {
        val mission = dao.getMissionSync(missionId)
        val title = mission?.title ?: "N/A"
        
        // Find previous blockers
        val allMissions = dao.getAllMissionsFlow().firstOrNull() ?: emptyList()
        val blockedMissions = allMissions.filter { !it.blockerReason.isNullOrEmpty() }
        val blockerHistory = blockedMissions.take(5).joinToString(", ") { "${it.title}: ${it.blockerReason}" }

        return geminiService.getCoachAdvice(
            currentMission = title,
            activeSubtask = activeSubtask,
            blockerHistory = blockerHistory,
            userQuery = userQuery
        )
    }

    private suspend fun getHistoricalPerformanceContext(): String {
        val allMissions = dao.getAllMissionsFlow().firstOrNull() ?: emptyList()
        if (allMissions.isEmpty()) return ""

        val totalMissions = allMissions.size
        val completed = allMissions.count { it.status == "COMPLETED" }
        val categoryCompletionMap = allMissions.groupBy { it.category }.mapValues { (_, group) ->
            "${group.count { it.status == "COMPLETED" }} / ${group.size} completed"
        }
        val blockerFrequencyMap = allMissions.filter { !it.blockerReason.isNullOrEmpty() }
            .groupBy { it.blockerReason }
            .mapValues { it.value.size }

        return """
            Completed $completed out of $totalMissions total historical missions.
            Completion rate by Category: $categoryCompletionMap
            Top Blockers logged: $blockerFrequencyMap
        """.trimIndent()
    }
}
