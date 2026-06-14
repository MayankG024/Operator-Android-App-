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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.MissionEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.OperatorViewModel

@Composable
fun HomeScreen(
    viewModel: OperatorViewModel,
    modifier: Modifier = Modifier
) {
    val user by viewModel.userState.collectAsState()
    val allMissions by viewModel.allMissionsState.collectAsState()
    val activeFocusMission by viewModel.selectedFocusMission.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()

    val todayDate = viewModel.todayDateString
    val todayMissions = allMissions.filter { it.dateString == todayDate }

    LaunchedEffect(Unit) {
        viewModel.loadTodayBriefIfNeeded()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header Section ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "OPERATOR / SYSTEM",
                        color = NeonGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Execution Mode",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = (-0.5).sp
                    )
                }
                user?.let { userData ->
                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = SlateCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(NeonGreen, shape = androidx.compose.foundation.shape.CircleShape)
                                )
                                Text(
                                    text = "${userData.lifetimeXp} XP",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                        Text(
                            text = "LEVEL ${userData.level} SPECIALIST",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        )
                    }
                } ?: Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = SlateCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(NeonGreen, shape = androidx.compose.foundation.shape.CircleShape)
                        )
                        Text(
                            text = "ONLINE",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // --- User Core Stats Cards ---
        item {
            user?.let { userData ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "LEVEL ${userData.level} SYSTEM",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Light,
                                    letterSpacing = (-0.5).sp
                                )
                                Text(
                                    text = "SPECIALIST RANK",
                                    color = SlateTextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${userData.xp} XP",
                                    color = NeonGreen,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.5).sp
                                )
                                Text(
                                    text = "CURRENT SESSION XP",
                                    color = SlateTextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // XP Progress bar
                        val xpNeeded = userData.level * 100
                        val progressFraction = (userData.xp.toFloat() / xpNeeded.toFloat()).coerceIn(0f, 1f)
                        
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "PROGRESS TO NEXT LEVEL: ${(progressFraction * 100).toInt()}%",
                                    color = NeonGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "$xpNeeded XP REQ",
                                    color = SlateTextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = NeonGreen,
                                trackColor = Color.White.copy(alpha = 0.05f)
                            )
                        }

                        Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 16.dp))

                        // Streaks Section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StreakCard(
                                title = "PLANNING",
                                count = userData.planningStreak,
                                activeColor = NeonGreen,
                                testTag = "planning_streak_indicator"
                            )
                            StreakCard(
                                title = "EXECUTION",
                                count = userData.executionStreak,
                                activeColor = ElectricCyan,
                                testTag = "execution_streak_indicator"
                            )
                            StreakCard(
                                title = "REFLECTION",
                                count = userData.reflectionStreak,
                                activeColor = GoldProgress,
                                testTag = "reflection_streak_indicator"
                            )
                        }
                    }
                }
            }
        }

        // --- Active Focus Overlay Link ---
        if (activeFocusMission != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo("focus") },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen)
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(NeonGreen.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Active Timer",
                                tint = NeonGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ACTIVE FOCUS SESSION",
                                color = NeonGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = activeFocusMission?.title ?: "N/A",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = if (isTimerRunning) Color(0xFF142F24) else Color(0xFF2D251A),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isTimerRunning) Color(0xFF2E6B52) else Color(0xFF5D4A27))
                        ) {
                            Text(
                                text = if (isTimerRunning) "RUNNING" else "PAUSED",
                                color = if (isTimerRunning) Color(0xFF34D399) else GoldProgress,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- Today's Missions List ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TODAY'S MISSION LOG",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${todayMissions.count { it.status == "COMPLETED" }} / ${todayMissions.size} COMPLETED",
                    color = if (todayMissions.isNotEmpty() && todayMissions.all { it.status == "COMPLETED" }) NeonGreen else SlateTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (todayMissions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateCard)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = "No Plans",
                            tint = SlateTextSecondary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "NO MISSIONS ASSIGNED FOR TODAY",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Please utilize the night planning system on the 'Plan' screen to formulate and load tomorrow's execution roadmap.",
                            color = SlateTextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.navigateTo("plan") },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            modifier = Modifier.testTag("navigate_plan_button")
                        ) {
                            Text("PLAN TARGETS NOW", color = Color(0xFF00391B), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(todayMissions) { mission ->
                MissionCard(
                    mission = mission,
                    onStartFocus = {
                        viewModel.selectMissionForFocus(mission)
                        viewModel.navigateTo("focus")
                    },
                    onToggleComplete = {
                        viewModel.selectMissionForFocus(mission)
                        viewModel.completeActiveFocusMission()
                    }
                )
            }
        }
    }
}

@Composable
fun StreakCard(
    title: String,
    count: Int,
    activeColor: Color,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.Black.copy(alpha = 0.2f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = Modifier.width(100.dp).testTag(testTag)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = "Streak",
                tint = if (count > 0) activeColor else SlateTextSecondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$count DAYS",
                color = if (count > 0) Color.White else SlateTextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                color = SlateTextSecondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}


@Composable
fun MissionCard(
    mission: MissionEntity,
    onStartFocus: () -> Unit,
    onToggleComplete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val diffColor = when (mission.difficulty) {
        "Easy" -> Color(0xFF4CAF50)
        "Medium" -> ActiveBlue
        "Hard" -> Color(0xFFFF9800)
        "Epic" -> BossRed
        else -> NeonGreen
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("mission_card_${mission.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SlateCard),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
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
                            color = diffColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = mission.difficulty.uppercase(),
                                color = diffColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = mission.title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.testTag("mission_expand_${mission.id}")
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand details",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Duration",
                        tint = SlateTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${mission.estimatedMinutes}m",
                        color = SlateTextSecondary,
                        fontSize = 12.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (mission.status == "COMPLETED") {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF11381E)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Success",
                                    tint = NeonGreen,
                                    modifier = Modifier.size(14.dp)
                               )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("COMPLETED", color = NeonGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Button(
                            onClick = onStartFocus,
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("start_focus_btn_${mission.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color(0xFF00373F),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("FOCUS", color = Color(0xFF00373F), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = onToggleComplete,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("complete_instantly_btn_${mission.id}")
                        ) {
                            Text("DONE", color = Color(0xFF00391B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .background(Color(0xFF121216), shape = RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "REFINED OUTCOME",
                        color = ElectricCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = mission.refinedOutcome.ifEmpty { "No outcome described." },
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "SUCCESS CRITERIA",
                        color = NeonGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    
                    val criteria = mission.successCriteria.split("\n").filter { it.trim().isNotEmpty() }
                    if (criteria.isEmpty()) {
                        Text("No specific success criteria provided.", color = SlateTextSecondary, fontSize = 12.sp)
                    } else {
                        criteria.forEach { criterion ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = NeonGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(criterion, color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }

                    if (!mission.blockerReason.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "PREVIOUS BLOCKER HISTORY",
                            color = OrangeBlocker,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = mission.blockerReason ?: "",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

val OrangeBlocker = Color(0xFFFF9800)
