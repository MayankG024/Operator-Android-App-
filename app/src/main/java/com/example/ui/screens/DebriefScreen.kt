package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.MissionEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.OperatorViewModel

@Composable
fun DebriefScreen(
    viewModel: OperatorViewModel,
    modifier: Modifier = Modifier
) {
    val allMissions by viewModel.allMissionsState.collectAsState()
    val isDebriefLoading by viewModel.isDebriefLoading.collectAsState()
    val debriefEnergyLevel by viewModel.debriefEnergyLevel.collectAsState()
    val debriefWhyBlockersText by viewModel.debriefWhyBlockersText.collectAsState()
    val debriefAnalysisResult by viewModel.debriefAnalysisResult.collectAsState()
    val apiError by viewModel.apiError.collectAsState()

    val todayDate = viewModel.todayDateString
    val todayMissions = allMissions.filter { it.dateString == todayDate }
    val completedMissions = todayMissions.filter { it.status == "COMPLETED" }
    val failedMissions = todayMissions.filter { it.status != "COMPLETED" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header Section ---
        item {
            Column {
                Text(
                    text = "DAILY DEBRIEF",
                    color = NeonGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Reflect & Consolidate Cores",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-0.5).sp
                )
            }
        }

        if (debriefAnalysisResult == null) {
            // --- Survey Question Sheet ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "TODAY'S EXECUTION RECORD STATUS:",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                text = "Completed Missions: ${completedMissions.size}",
                                color = NeonGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Pending/Blocked Missions: ${failedMissions.size}",
                                color = if (failedMissions.isNotEmpty()) BossRed else SlateTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (todayMissions.isEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "⚠ Note: You have no missions logged today. You can still complete this debrief to check streak metrics.",
                                color = GoldProgress,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // --- Blocker Reasons Input (Why?) ---
            if (failedMissions.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = SlateCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "WHY WERE MISSIONS DELAYED/BLOCKED?",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = debriefWhyBlockersText,
                                onValueChange = { viewModel.updateDebriefWhyBlockersText(it) },
                                placeholder = {
                                    Text(
                                        text = "Log blocker detail triggers (e.g. documentation syntax was confusing, lost focus during hours 13:00-15:00)",
                                        color = SlateTextSecondary,
                                        fontSize = 12.sp
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(84.dp)
                                    .testTag("debrief_why_blocker_input"),
                                textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElectricCyan,
                                    unfocusedBorderColor = DarkTextSecondary,
                                    focusedContainerColor = Color(0xFF0F0F12),
                                    unfocusedContainerColor = Color(0xFF0F0F12)
                                )
                            )
                        }
                    }
                }
            }

            // --- Energy Level Matrix ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CURRENT ENERGY LEVEL GRADE",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$debriefEnergyLevel / 10",
                                color = NeonGreen,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        
                        Slider(
                            value = debriefEnergyLevel.toFloat(),
                            onValueChange = { viewModel.setDebriefEnergyLevel(it.toInt()) },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonGreen,
                                activeTrackColor = ElectricCyan,
                                inactiveTrackColor = DarkTextSecondary
                            ),
                            modifier = Modifier.testTag("debrief_energy_slider")
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Extremely Exhausted", color = SlateTextSecondary, fontSize = 10.sp)
                            Text("Peak Vitality", color = SlateTextSecondary, fontSize = 10.sp)
                        }
                    }
                }
            }

            // --- Error / CTA Trigger ---
            item {
                if (apiError != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF3B1F1F)
                    ) {
                        Text(
                            text = apiError ?: "",
                            color = Color(0xFFFF8B8B),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Button(
                    onClick = { viewModel.runTodayDebrief() },
                    enabled = !isDebriefLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_debrief_analysis_button")
                ) {
                    if (isDebriefLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color(0xFF00391B))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("COMPILING COGNITIVE ANALYSIS...", color = Color(0xFF00391B), fontWeight = FontWeight.Bold)
                    } else {
                        Icon(imageVector = Icons.Default.Analytics, contentDescription = null, tint = Color(0xFF00391B))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SUBMIT REFLECTION CORES FOR ANALYSIS", color = Color(0xFF00391B), fontWeight = FontWeight.Black)
                    }
                }
            }
        } else {
            // --- AI Analysis Outcome Result ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DEBRIEF REFLECTION SECURED",
                                color = NeonGreen,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your daily execution metrics have been written to long-term memory logs. Stacks updated. Streak increments locked.",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            item {
                Text(
                    text = "AI-GENERATED PERFORMANCE STUDY:",
                    color = SlateTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "DAY SUMMARY",
                            color = ElectricCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = debriefAnalysisResult?.summary ?: "No summary text generated.",
                            color = Color.White,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 4.dp),
                            lineHeight = 18.sp
                        )

                        Divider(color = Color(0xFF2E2E3E), modifier = Modifier.padding(vertical = 12.dp))

                        Text(
                            text = "COACH LESSONS LEARNED",
                            color = NeonGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = debriefAnalysisResult?.lessonsLearned ?: "No lessons compiled.",
                            color = Color.White,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 4.dp),
                            lineHeight = 18.sp
                        )

                        Divider(color = Color(0xFF2E2E3E), modifier = Modifier.padding(vertical = 12.dp))

                        Text(
                            text = "ROADMAP ADJUSTMENTS FOR PLANNER",
                            color = GoldProgress,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = debriefAnalysisResult?.suggestedAdjustments ?: "No planning edits required.",
                            color = Color.White,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 4.dp),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        viewModel.clearDebriefState()
                        viewModel.navigateTo("home")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("debrief_dismiss_continue_button")
                ) {
                    Text("DISMISS AND DEPLOY HOME DASHBOARD", color = Color(0xFF00391B), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
