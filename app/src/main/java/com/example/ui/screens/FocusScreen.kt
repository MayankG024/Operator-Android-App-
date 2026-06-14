package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.MissionEntity
import com.example.data.database.SubtaskEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.OperatorViewModel

@Composable
fun FocusScreen(
    viewModel: OperatorViewModel,
    modifier: Modifier = Modifier
) {
    val selectedMission by viewModel.selectedFocusMission.collectAsState()
    val allMissions by viewModel.allMissionsState.collectAsState()
    val todayDate = viewModel.todayDateString
    val pendingTodayMissions = allMissions.filter { it.dateString == todayDate && it.status != "COMPLETED" }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBackground)
    ) {
        if (selectedMission == null) {
            FocusEmptyState(pendingTodayMissions) { mission ->
                viewModel.selectMissionForFocus(mission)
            }
        } else {
            ActiveFocusState(viewModel, selectedMission!!)
        }
    }
}

@Composable
fun FocusEmptyState(
    pendingMissions: List<MissionEntity>,
    onSelectMission: (MissionEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = "Timer",
                tint = NeonGreen,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "FOCUS MODE GATEWAY",
                color = NeonGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Text(
                text = "Deploy Operational Runtimes",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = (-0.5).sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )
        }

        if (pendingMissions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "NO RUNNING DAILY TARGETS AVAILABLE",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add tasks inside night planning mode or mark pending work as active before invoking Deep Focus Mode.",
                            color = SlateTextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        } else {
            item {
                Text(
                    text = "SELECT RUNNING MISSION FOR DEEP FOCUS:",
                    color = SlateTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    textAlign = TextAlign.Start
                )
            }

            items(pendingMissions) { mission ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onSelectMission(mission) }
                        .testTag("select_focus_mission_${mission.id}"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = "Select",
                            tint = NeonGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = mission.title,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${mission.category} | ${mission.difficulty} | ${mission.estimatedMinutes} mins",
                                color = SlateTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveFocusState(
    viewModel: OperatorViewModel,
    mission: MissionEntity
) {
    val activeSubtasks by viewModel.activeSubtasks.collectAsState()
    val timerSeconds by viewModel.focusTimerSeconds.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val interruptions by viewModel.interruptionsCount.collectAsState()
    val progressStatus by viewModel.focusProgressStatus.collectAsState()
    val showBlockerInput by viewModel.showBlockerInput.collectAsState()
    val blockerReasonText by viewModel.blockerReasonText.collectAsState()
    val isCoachLoading by viewModel.isCoachLoading.collectAsState()
    val coachChatHistory by viewModel.coachChatHistory.collectAsState()

    var chatInputField by remember { mutableStateOf("") }

    // Convert seconds to HH:MM:SS
    val hours = timerSeconds / 3600
    val minutes = (timerSeconds % 3600) / 60
    val seconds = timerSeconds % 60
    val timerString = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header Core Info ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF1E1E28)
                        ) {
                            Text(
                                text = mission.category.uppercase(),
                                color = ElectricCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF2E1C1C)
                        ) {
                            Text(
                                text = mission.difficulty.uppercase(),
                                color = BossRed,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = mission.title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                IconButton(
                    onClick = { viewModel.selectMissionForFocus(mission) }, // Reloads / resets focus selecting states
                    modifier = Modifier.testTag("reset_focus_mission_selection")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Exit focus mode",
                        tint = SlateTextSecondary
                    )
                }
            }
        }

        // --- Core Timer Chronometer ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ELAPSED FOCUS SESSION TIME",
                        color = SlateTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = timerString,
                        color = if (isTimerRunning) NeonGreen else Color.White,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.testTag("focus_timer_text")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        if (!isTimerRunning) {
                            Button(
                                onClick = { viewModel.startFocusTimer() },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                modifier = Modifier.testTag("start_timer_button").width(110.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF00391B))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("START", color = Color(0xFF00391B), fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = { viewModel.pauseFocusTimer() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2E8F0)),
                                modifier = Modifier.testTag("pause_timer_button").width(110.dp)
                            ) {
                                Icon(Icons.Default.Pause, contentDescription = null, tint = Color(0xFF1E293B))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PAUSE", color = Color(0xFF1E293B), fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = { viewModel.recordInterruption() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2B36)),
                            modifier = Modifier.testTag("interruption_button")
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("DISTRACTED ($interruptions)", color = ElectricCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- Progress Check-In Status Sheet ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "HOW IS PROGRESS CURRENTLY?",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("On Track", "Delayed", "Blocked").forEach { status ->
                            val isSelected = progressStatus == status
                            val color = when (status) {
                                "On Track" -> NeonGreen
                                "Delayed" -> GoldProgress
                                "Blocked" -> BossRed
                                else -> Color.White
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        color = if (isSelected) color.copy(alpha = 0.15f) else Color(0xFF16161D),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.updateProgressStatus(status) }
                                    .padding(vertical = 10.dp)
                                    .testTag("progress_selection_$status"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = status.uppercase(),
                                    color = if (isSelected) color else SlateTextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = showBlockerInput) {
                        var tempBlockerReason by remember { mutableStateOf("") }
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            Text(
                                text = "STIPULATE THE BLOCKER CRITERIA / REASON:",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = tempBlockerReason,
                                onValueChange = { tempBlockerReason = it },
                                placeholder = { Text("What is impeding execution? (e.g. documentation issues, focus issues)", color = SlateTextSecondary, fontSize = 12.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp)
                                    .testTag("blocker_reason_input_field"),
                                textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BossRed,
                                    unfocusedBorderColor = DarkTextSecondary,
                                    focusedContainerColor = Color(0xFF0F0F12),
                                    unfocusedContainerColor = Color(0xFF0F0F12)
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.submitBlockerReason(tempBlockerReason) },
                                colors = ButtonDefaults.buttonColors(containerColor = BossRed),
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .testTag("submit_blocker_reason_btn")
                            ) {
                                Text("LOG BLOCKER TO WORKSPACE", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (mission.status == "COMPLETED") {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF11381E)
                        ) {
                            Text(
                                text = "✓ SUCCESS: MISSION MARKED COMPLETED. ALL EARNED XP AND PROGRESSION BONUSES APPLIED TO CORE LOGS.",
                                color = NeonGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.completeActiveFocusMission() },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("complete_active_mission_button")
                        ) {
                            Text("COMPLETE MISSION & EARN XP", color = Color(0xFF00391B), fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        // --- Active Subtasks List Checklist ---
        item {
            Text(
                text = "ACTIONABLE SUBTASKS SCHEDULE",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        if (activeSubtasks.isEmpty()) {
            item {
                Text("Analyzing tasks details...", color = SlateTextSecondary, fontSize = 12.sp)
            }
        } else {
            items(activeSubtasks) { subtask ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SlateCard, shape = RoundedCornerShape(8.dp))
                        .clickable { viewModel.toggleSubtaskCompletion(subtask) }
                        .padding(12.dp)
                        .testTag("subtask_item_${subtask.id}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = subtask.isCompleted,
                        onCheckedChange = { viewModel.toggleSubtaskCompletion(subtask) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = NeonGreen,
                            uncheckedColor = DarkTextSecondary
                        ),
                        modifier = Modifier.testTag("subtask_checkbox_${subtask.id}")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = subtask.title,
                        color = if (subtask.isCompleted) SlateTextSecondary else Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        style = if (subtask.isCompleted) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        // --- AI Execution Coach Chat/Panel ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SlateCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SupportAgent, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI EXECUTION COACH",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }

                        if (isCoachLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = ElectricCyan)
                        }
                    }

                    Divider(color = Color(0xFF2E2E3E), modifier = Modifier.padding(vertical = 12.dp))

                    if (coachChatHistory.isEmpty()) {
                        Text(
                            text = "Need micro-task breakdown, concepts explanations, resource advice or recovery suggestions? Write message below.",
                            color = SlateTextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp)
                                .padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            coachChatHistory.forEach { chat ->
                                val isUser = chat.first == "user"
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                                ) {
                                    Text(
                                        text = if (isUser) "OPERATOR (YOU)" else "AI EXECUTION COACH",
                                        color = if (isUser) NeonGreen else ElectricCyan,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isUser) Color(0xFF1E2E24) else Color(0xFF11252C),
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Text(
                                            text = chat.second,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = chatInputField,
                            onValueChange = { chatInputField = it },
                            placeholder = { Text("Ask for blockers, task splits, tips...", color = SlateTextSecondary, fontSize = 12.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("coach_chat_input_field"),
                            textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = DarkTextSecondary,
                                focusedContainerColor = Color(0xFF0F0F12),
                                unfocusedContainerColor = Color(0xFF0F0F12)
                            )
                        )

                        IconButton(
                            onClick = {
                                if (chatInputField.trim().isNotEmpty()) {
                                    viewModel.askExecutionCoach(chatInputField)
                                    chatInputField = ""
                                }
                            },
                            enabled = chatInputField.trim().isNotEmpty() && !isCoachLoading,
                            modifier = Modifier
                                .background(ElectricCyan, shape = RoundedCornerShape(8.dp))
                                .size(48.dp)
                                .testTag("coach_chat_send_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send advice query",
                                tint = Color(0xFF00373F),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
