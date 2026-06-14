package com.example.ui.viewmodel

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.*
import com.example.data.network.AICoachAdviceResponse
import com.example.data.network.AIDailyBriefResponse
import com.example.data.network.AIDebriefResponse
import com.example.data.repository.OperatorRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException

class OperatorViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = OperatorRepository(database.operatorDao())

    // --- Core UI Observables ---
    val userState: StateFlow<UserEntity?> = repository.userFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allMissionsState: StateFlow<List<MissionEntity>> = repository.allMissionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSkillsState: StateFlow<List<SkillEntity>> = repository.allSkillsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAchievementsState: StateFlow<List<AchievementEntity>> = repository.allAchievementsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBossMissionsState: StateFlow<List<BossMissionEntity>> = repository.allBossMissionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInsightsState: StateFlow<List<InsightEntity>> = repository.allInsightsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReviewsState: StateFlow<List<ReviewEntity>> = repository.allReviewsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val xpEventsState: StateFlow<List<XPEventEntity>> = repository.allXpEventsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- State helpers ---
    val todayDateString: String get() = repository.getCurrentDateString()
    val tomorrowDateString: String get() = repository.getTomorrowDateString()

    // --- Loading & Error States ---
    private val _isPlanningLoading = MutableStateFlow(false)
    val isPlanningLoading: StateFlow<Boolean> = _isPlanningLoading.asStateFlow()

    private val _isBriefLoading = MutableStateFlow(false)
    val isBriefLoading: StateFlow<Boolean> = _isBriefLoading.asStateFlow()

    private val _isDebriefLoading = MutableStateFlow(false)
    val isDebriefLoading: StateFlow<Boolean> = _isDebriefLoading.asStateFlow()

    private val _isCoachLoading = MutableStateFlow(false)
    val isCoachLoading: StateFlow<Boolean> = _isCoachLoading.asStateFlow()

    private val _apiError = MutableStateFlow<String?>(null)
    val apiError: StateFlow<String?> = _apiError.asStateFlow()

    // --- Navigation Helper ---
    private val _currentScreen = MutableStateFlow("home")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    // --- Planning Flow States ---
    private val _enteredGoals = MutableStateFlow("")
    val enteredGoals: StateFlow<String> = _enteredGoals.asStateFlow()

    private val _plannedMissions = MutableStateFlow<List<MissionEntity>>(emptyList())
    val plannedMissions: StateFlow<List<MissionEntity>> = _plannedMissions.asStateFlow()

    fun updateEnteredGoals(goals: String) {
        _enteredGoals.value = goals
    }

    // --- Daily Brief States ---
    private val _dailyBrief = MutableStateFlow<AIDailyBriefResponse?>(null)
    val dailyBrief: StateFlow<AIDailyBriefResponse?> = _dailyBrief.asStateFlow()

    // --- Focus Mode Active States ---
    private val _selectedFocusMission = MutableStateFlow<MissionEntity?>(null)
    val selectedFocusMission: StateFlow<MissionEntity?> = _selectedFocusMission.asStateFlow()

    private val _activeSubtasks = MutableStateFlow<List<SubtaskEntity>>(emptyList())
    val activeSubtasks: StateFlow<List<SubtaskEntity>> = _activeSubtasks.asStateFlow()

    // Timer variables
    private val _focusTimerSeconds = MutableStateFlow(0)
    val focusTimerSeconds: StateFlow<Int> = _focusTimerSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _interruptionsCount = MutableStateFlow(0)
    val interruptionsCount: StateFlow<Int> = _interruptionsCount.asStateFlow()

    private var timerJob: Job? = null

    // Blocker query & Coach panel
    private val _focusProgressStatus = MutableStateFlow("On Track") // On Track, Delayed, Blocked, Complete
    val focusProgressStatus: StateFlow<String> = _focusProgressStatus.asStateFlow()

    private val _showBlockerInput = MutableStateFlow(false)
    val showBlockerInput: StateFlow<Boolean> = _showBlockerInput.asStateFlow()

    private val _blockerReasonText = MutableStateFlow("")
    val blockerReasonText: StateFlow<String> = _blockerReasonText.asStateFlow()

    private val _liveCoachAdvice = MutableStateFlow<AICoachAdviceResponse?>(null)
    val liveCoachAdvice: StateFlow<AICoachAdviceResponse?> = _liveCoachAdvice.asStateFlow()

    private val _coachChatHistory = MutableStateFlow<List<Pair<String, String>>>(emptyList()) // Pair(Sender, Text)
    val coachChatHistory: StateFlow<List<Pair<String, String>>> = _coachChatHistory.asStateFlow()

    // --- Debriefing Flow States ---
    private val _debriefEnergyLevel = MutableStateFlow(7)
    val debriefEnergyLevel: StateFlow<Int> = _debriefEnergyLevel.asStateFlow()

    private val _debriefWhyBlockersText = MutableStateFlow("")
    val debriefWhyBlockersText: StateFlow<String> = _debriefWhyBlockersText.asStateFlow()

    private val _debriefAnalysisResult = MutableStateFlow<AIDebriefResponse?>(null)
    val debriefAnalysisResult: StateFlow<AIDebriefResponse?> = _debriefAnalysisResult.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initDefaultDataIfNeeded()
            // Pull in database and update insights once
            repository.generateAndPersistHourlyInsights()
        }
    }

    fun clearApiError() {
        _apiError.value = null
    }

    // --- Operations ---

    fun enhanceNightGoals() {
        if (_enteredGoals.value.trim().isEmpty()) {
            _apiError.value = "Goals input cannot be empty."
            return
        }
        viewModelScope.launch {
            _isPlanningLoading.value = true
            _apiError.value = null
            try {
                val list = repository.executeNightPlanningGoals(_enteredGoals.value)
                _plannedMissions.value = list
                _enteredGoals.value = ""
                // Generate brief immediately
                generateTomorrowBrief()
            } catch (e: Exception) {
                Log.e("OperatorVM", "Failed to enhance night planning", e)
                _apiError.value = e.message ?: "Failed to reach AI. Confirm internet and correct Gemini API Key."
            } finally {
                _isPlanningLoading.value = false
            }
        }
    }

    private fun generateTomorrowBrief() {
        viewModelScope.launch {
            _isBriefLoading.value = true
            try {
                val response = repository.generateDailyBriefTextForDate(tomorrowDateString)
                _dailyBrief.value = response
            } catch (e: Exception) {
                Log.e("OperatorVM", "Failed to generate brief", e)
            } finally {
                _isBriefLoading.value = false
            }
        }
    }

    fun loadTodayBriefIfNeeded() {
        val todayMissions = allMissionsState.value.filter { it.dateString == todayDateString }
        if (todayMissions.isNotEmpty() && _dailyBrief.value == null) {
            viewModelScope.launch {
                _isBriefLoading.value = true
                try {
                    val response = repository.generateDailyBriefTextForDate(todayDateString)
                    _dailyBrief.value = response
                } catch (e: Exception) {
                    Log.e("OperatorVM", "Failed to load brief", e)
                } finally {
                    _isBriefLoading.value = false
                }
            }
        }
    }

    // Boss Mission creation helper
    fun submitBossMission(title: String, description: String, objectiveCount: Int) {
        viewModelScope.launch {
            val bossId = repository.insertBossMission(
                BossMissionEntity(
                    title = title,
                    description = description,
                    progressPercentage = 0f,
                    isCompleted = false,
                    rewardXp = objectiveCount * 125 // dynamic XP
                )
            ).toInt()

            // Automatically create placeholder sub-missions for tomorrow or today
            for (i in 1..objectiveCount) {
                val missionEntity = MissionEntity(
                    title = "$title: Milestone $i",
                    refinedOutcome = "Reach milestone $i of the larger epic project",
                    successCriteria = "Milestone criteria complete",
                    difficulty = "Hard",
                    estimatedMinutes = 120,
                    focusOrder = i,
                    category = "Career",
                    status = "PENDING",
                    isBossMission = true,
                    bossMissionId = bossId,
                    dateString = todayDateString
                )
                val mId = repository.insertMission(missionEntity).toInt()
                repository.insertSubtask(SubtaskEntity(missionId = mId, title = "Analyze milestone details", isCompleted = false))
                repository.insertSubtask(SubtaskEntity(missionId = mId, title = "Execute coding or direct development work", isCompleted = false))
                repository.insertSubtask(SubtaskEntity(missionId = mId, title = "Verify outcomes and commit changes", isCompleted = false))
            }
        }
    }

    // --- Focus Session Workflows ---

    fun selectMissionForFocus(mission: MissionEntity) {
        _selectedFocusMission.value = mission
        _focusTimerSeconds.value = 0
        _isTimerRunning.value = false
        _interruptionsCount.value = 0
        _focusProgressStatus.value = "On Track"
        _showBlockerInput.value = false
        _blockerReasonText.value = ""
        _liveCoachAdvice.value = null
        _coachChatHistory.value = emptyList()
        stopFocusTimer()

        // Sync Subtasks
        viewModelScope.launch {
            repository.getSubtasksForMissionFlow(mission.id).collectLatest { list ->
                _activeSubtasks.value = list
            }
        }
    }

    fun startFocusTimer() {
        if (_isTimerRunning.value) return
        _isTimerRunning.value = true
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _focusTimerSeconds.value++
            }
        }
    }

    fun pauseFocusTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
    }

    fun recordInterruption() {
        _interruptionsCount.value++
    }

    fun stopFocusTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
        timerJob = null
    }

    fun toggleSubtaskCompletion(subtask: SubtaskEntity) {
        viewModelScope.launch {
            val updated = subtask.copy(isCompleted = !subtask.isCompleted)
            repository.completeSubtaskAndTrackProgress(updated)
        }
    }

    fun updateProgressStatus(status: String) {
        _focusProgressStatus.value = status
        if (status == "Blocked") {
            _showBlockerInput.value = true
        } else {
            _showBlockerInput.value = false
        }

        if (status == "Complete") {
            completeActiveFocusMission()
        }
    }

    fun submitBlockerReason(reason: String) {
        _blockerReasonText.value = reason
        _showBlockerInput.value = false
        
        // Save blocker on the mission database entity
        val mission = _selectedFocusMission.value ?: return
        viewModelScope.launch {
            val updated = mission.copy(
                status = "BLOCKED",
                blockerReason = reason
            )
            repository.updateMission(updated)
            _selectedFocusMission.value = updated

            // Ask the live execution coach for recovery advice immediately!
            askExecutionCoach("How can I recover from this blocker: $reason")
        }
    }

    fun askExecutionCoach(message: String) {
        val mission = _selectedFocusMission.value ?: return
        val currentSubtaskTitle = _activeSubtasks.value.find { !it.isCompleted }?.title ?: "No active subtasks"

        viewModelScope.launch {
            _isCoachLoading.value = true
            _coachChatHistory.value = _coachChatHistory.value + Pair("user", message)
            try {
                val response = repository.getCoachAdviceLive(
                    missionId = mission.id,
                    activeSubtask = currentSubtaskTitle,
                    userQuery = message
                )
                _liveCoachAdvice.value = response
                
                var adviceParsed = response.advice
                if (response.recoveryTip.isNotEmpty()) {
                    adviceParsed += "\n\n💡 Recovery Tip: ${response.recoveryTip}"
                }
                
                _coachChatHistory.value = _coachChatHistory.value + Pair("coach", adviceParsed)
            } catch (e: Exception) {
                Log.e("OperatorVM", "Failed to retrieve coach advice", e)
                _coachChatHistory.value = _coachChatHistory.value + Pair("coach", "I was unable to reach my processor core. Review network or Gemini Credentials. Keep focus and complete tasks!")
            } finally {
                _isCoachLoading.value = false
            }
        }
    }

    fun completeActiveFocusMission() {
        val mission = _selectedFocusMission.value ?: return
        stopFocusTimer()
        viewModelScope.launch {
            // Log Focus Session
            repository.recordFocusSession(
                missionId = mission.id,
                durationSeconds = _focusTimerSeconds.value,
                completed = true,
                interruptions = _interruptionsCount.value
            )

            // Mark mission completed in repository & issue all relevant XP / Leveling gains
            repository.completeMission(mission.id)

            // Reload mission model in local flow
            _selectedFocusMission.value = mission.copy(status = "COMPLETED")
            _focusProgressStatus.value = "Complete"
        }
    }

    // --- Debriefing Flow ---

    fun setDebriefEnergyLevel(energy: Int) {
        _debriefEnergyLevel.value = energy
    }

    fun updateDebriefWhyBlockersText(text: String) {
        _debriefWhyBlockersText.value = text
    }

    fun runTodayDebrief() {
        val todayMissions = allMissionsState.value.filter { it.dateString == todayDateString }
        val completed = todayMissions.filter { it.status == "COMPLETED" }
        val failed = todayMissions.filter { it.status != "COMPLETED" }

        viewModelScope.launch {
            _isDebriefLoading.value = true
            _apiError.value = null
            try {
                val review = repository.executeEveningDebrief(
                    dateString = todayDateString,
                    completedMissionsList = completed,
                    failedMissionsList = failed,
                    failedReasons = _debriefWhyBlockersText.value,
                    energyLevel = _debriefEnergyLevel.value
                )

                // Render structured reviews
                _debriefAnalysisResult.value = AIDebriefResponse(
                    summary = review.summary,
                    lessonsLearned = review.lessonsLearned,
                    suggestedAdjustments = review.suggestedAdjustments
                )

                // Refresh analytics insights
                repository.generateAndPersistHourlyInsights()
            } catch (e: Exception) {
                Log.e("OperatorVM", "Debrief analysis error", e)
                _apiError.value = "Failed to run debrief analysis: ${e.message}"
            } finally {
                _isDebriefLoading.value = false
            }
        }
    }

    fun clearDebriefState() {
        _debriefWhyBlockersText.value = ""
        _debriefAnalysisResult.value = null
    }
}
