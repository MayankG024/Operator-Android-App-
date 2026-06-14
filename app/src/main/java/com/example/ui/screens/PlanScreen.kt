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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.MissionEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.OperatorViewModel

@Composable
fun PlanScreen(
    viewModel: OperatorViewModel,
    modifier: Modifier = Modifier
) {
    val enteredGoals by viewModel.enteredGoals.collectAsState()
    val plannedMissions by viewModel.plannedMissions.collectAsState()
    val isPlanningLoading by viewModel.isPlanningLoading.collectAsState()
    val isBriefLoading by viewModel.isBriefLoading.collectAsState()
    val apiError by viewModel.apiError.collectAsState()
    val dailyBrief by viewModel.dailyBrief.collectAsState()

    var showHelpDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Title Block ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PLAN TOMORROW",
                        color = NeonGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Commit Daily Intentions",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = (-0.5).sp
                    )
                }
                
                IconButton(
                    onClick = { showHelpDialog = true },
                    modifier = Modifier.testTag("plan_help_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = "Planning guidance",
                        tint = ElectricCyan
                    )
                }
            }
        }

        // --- Goals Input Sheet ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "WHAT WOULD MAKE TOMORROW A SUCCESSFUL DAY?",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = enteredGoals,
                        onValueChange = { viewModel.updateEnteredGoals(it) },
                        placeholder = {
                            Text(
                                text = "Enter 1 to 5 goals/targets...\n- Finish Shopify collection page\n- Apply to high-tier career roles\n- Outline Kubernetes cluster config",
                                color = SlateTextSecondary,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("goals_input_field"),
                        textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            focusedContainerColor = Color(0xFF131316),
                            unfocusedContainerColor = Color(0xFF131316)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (apiError != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
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

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Button(
                            onClick = { viewModel.enhanceNightGoals() },
                            enabled = enteredGoals.trim().isNotEmpty() && !isPlanningLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonGreen,
                                disabledContainerColor = DarkTextSecondary
                            ),
                            modifier = Modifier.testTag("enhance_ai_button")
                        ) {
                            if (isPlanningLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color(0xFF00391B),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ANALYZING...", color = Color(0xFF00391B), fontWeight = FontWeight.Bold)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFF00391B),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("COACH ANALYSIS", color = Color(0xFF00391B), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // --- Core Daily Brief Briefing (AI Output) ---
        if (isBriefLoading) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateCard)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = ElectricCyan)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Drafting daily execution brief...", color = Color.White, fontSize = 13.sp)
                    }
                }
            }
        } else if (dailyBrief != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DAILY MISSION BRIEF",
                                color = ElectricCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                        
                        Divider(color = Color(0xFF1B354C), modifier = Modifier.padding(vertical = 8.dp))
                        
                        Text(
                            text = dailyBrief?.briefingText ?: "",
                            color = Color.White,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Text(
                            text = "Focus Hierarchy: ${dailyBrief?.recommendedFocusOrder}",
                            color = NeonGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // --- Resulting Refined Missions Summary ---
        if (plannedMissions.isNotEmpty()) {
            item {
                Text(
                    text = "REFINED TARGETS GENERATED",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(plannedMissions) { mission ->
                PlannedMissionReviewCard(mission)
            }

            item {
                Button(
                    onClick = {
                        viewModel.navigateTo("home")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("commit_planned_missions_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                ) {
                    Text(
                        text = "LOCK IN roadmap FOR TOMORROW",
                        color = Color(0xFF00391B),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = {
                Text("Operational Planning", color = Color.White)
            },
            text = {
                Text(
                    "Each night, input 1-5 raw goals representing what you want to execute tomorrow.\n\n" +
                    "The Operator AI Agent analyzes your goals, clarifies them into definite outcomes, structures 3 to 7 sequential subtasks, " +
                    "estimates completion times, and flags blockers to build your daily brief. Work completion builds real XP and increases Skill Levels.",
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("UNDERSTOD", color = NeonGreen)
                }
            },
            containerColor = SlateCard
        )
    }
}

@Composable
fun PlannedMissionReviewCard(mission: MissionEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SlateCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = mission.title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF1E1E28)
                ) {
                    Text(
                        text = "${mission.estimatedMinutes}m | ${mission.difficulty.uppercase()}",
                        color = NeonGreen,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Success outcome: ${mission.refinedOutcome}",
                color = SlateTextSecondary,
                fontSize = 12.sp
            )
        }
    }
}
