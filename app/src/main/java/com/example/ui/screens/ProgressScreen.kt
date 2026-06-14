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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.AchievementEntity
import com.example.data.database.BossMissionEntity
import com.example.data.database.InsightEntity
import com.example.data.database.SkillEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.OperatorViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProgressScreen(
    viewModel: OperatorViewModel,
    modifier: Modifier = Modifier
) {
    val skills by viewModel.allSkillsState.collectAsState()
    val achievements by viewModel.allAchievementsState.collectAsState()
    val bossMissions by viewModel.allBossMissionsState.collectAsState()
    val insights by viewModel.allInsightsState.collectAsState()
    val allReviews by viewModel.allReviewsState.collectAsState()
    val user by viewModel.userState.collectAsState()

    var showBossCreator by remember { mutableStateOf(false) }
    var bossTitle by remember { mutableStateOf("") }
    var bossDesc by remember { mutableStateOf("") }
    var bossMilestoneCount by remember { mutableStateOf(3) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header Core ---
        item {
            Column {
                Text(
                    text = "PROGRESSION HUB",
                    color = NeonGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Specialty Systems Mastery",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-0.5).sp
                )
            }
        }

        // --- Skill Category Trees ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Category, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SKILL TREE CLASSIFICATION",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (skills.isEmpty()) {
                        Text("Initiating skill categories tree...", color = SlateTextSecondary, fontSize = 12.sp)
                    } else {
                        skills.forEach { skill ->
                            SkillTreeProgressItem(skill)
                        }
                    }
                }
            }
        }

        // --- Streaks & limited grace protection note ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "GRACE STREAK PROTECTION",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "System contains limited streak shielding protection: missing your planning, execution, or reflection deadlines by exactly 1 single day triggers a grace period that shields your consecutive records from total reset.",
                        color = SlateTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    user?.let { userData ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Planning: ${userData.planningStreak}d", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Execution: ${userData.executionStreak}d", color = ElectricCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Reflection: ${userData.reflectionStreak}d", color = GoldProgress, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- Achievements Panel ---
        item {
            Text(
                text = "OPERATIONAL ACHIEVEMENTS",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (achievements.isEmpty()) {
            item {
                Text("Synthesizing achievements...", color = SlateTextSecondary, fontSize = 12.sp)
            }
        } else {
            items(achievements) { ach ->
                AchievementCardItem(ach)
            }
        }

        // --- Boss Missions Panel ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "EPIC BOSS MISSIONS",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Button(
                    onClick = { showBossCreator = !showBossCreator },
                    colors = ButtonDefaults.buttonColors(containerColor = if (showBossCreator) BossRed else Color(0xFF1E293B)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(30.dp).testTag("toggle_boss_creator_btn")
                ) {
                    Icon(imageVector = if (showBossCreator) Icons.Default.Close else Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (showBossCreator) "CANCEL" else "NEW BOSS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- Boss Mission Creator Modal/Drawer Form ---
        item {
            AnimatedVisibility(visible = showBossCreator) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BossRed)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "CREATE MULTI-DAY EPIC PROJECT",
                            color = BossRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )

                        OutlinedTextField(
                            value = bossTitle,
                            onValueChange = { bossTitle = it },
                            placeholder = { Text("Boss Mission Title (e.g. Build Portfolio Website)", color = SlateTextSecondary) },
                            modifier = Modifier.fillMaxWidth().testTag("boss_title_input"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BossRed, focusedContainerColor = Color(0xFF0F0F12), unfocusedContainerColor = Color(0xFF0F0F12)),
                            textStyle = TextStyle(color = Color.White, fontSize = 13.sp)
                        )

                        OutlinedTextField(
                            value = bossDesc,
                            onValueChange = { bossDesc = it },
                            placeholder = { Text("Core epic description...", color = SlateTextSecondary) },
                            modifier = Modifier.fillMaxWidth().height(60.dp).testTag("boss_desc_input"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BossRed, focusedContainerColor = Color(0xFF0F0F12), unfocusedContainerColor = Color(0xFF0F0F12)),
                            textStyle = TextStyle(color = Color.White, fontSize = 13.sp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Desired Milestones Count:", color = Color.White, fontSize = 12.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = { if (bossMilestoneCount > 1) bossMilestoneCount-- },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF13131D)),
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.size(32.dp)
                                ) { Text("-", color = Color.White) }
                                
                                Text("$bossMilestoneCount", color = Color.White, modifier = Modifier.padding(horizontal = 12.dp), fontWeight = FontWeight.Bold)
                                
                                Button(
                                    onClick = { if (bossMilestoneCount < 10) bossMilestoneCount++ },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF13131D)),
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.size(32.dp)
                                ) { Text("+", color = Color.White) }
                            }
                        }

                        Button(
                            onClick = {
                                if (bossTitle.isNotBlank()) {
                                    viewModel.submitBossMission(bossTitle, bossDesc, bossMilestoneCount)
                                    bossTitle = ""
                                    bossDesc = ""
                                    bossMilestoneCount = 3
                                    showBossCreator = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BossRed),
                            modifier = Modifier.fillMaxWidth().testTag("submit_boss_mission_btn")
                        ) {
                            Text("LAUNCH MASTER BOSS MISSION", color = Color.White, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        if (bossMissions.isEmpty()) {
            item {
                Text(
                    text = "No active epic boss missions currently running. Spawn one to execute complex, multi-day master roadmaps.",
                    color = SlateTextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                )
            }
        } else {
            items(bossMissions) { boss ->
                BossMissionCardItem(boss)
            }
        }

        // --- Analytical Insights Panel ---
        item {
            Text(
                text = "ANALYTIC COGNITIVE INSIGHTS",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (insights.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateCard)
                ) {
                    Text(
                        text = "Insufficient historical roadmap metrics to map analytical insights. Execute more sessions to compile performance outputs.",
                        color = SlateTextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        } else {
            items(insights.take(5)) { insight ->
                InsightCardItem(insight)
            }
        }
    }
}

@Composable
fun SkillTreeProgressItem(skill: SkillEntity) {
    val reqXp = skill.level * 150
    val progress = (skill.xp.toFloat() / reqXp.toFloat()).coerceIn(0f, 1f)

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = skill.category,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "GRADE ${skill.level}  (${skill.xp} XP)",
                color = ElectricCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = ElectricCyan,
            trackColor = DarkTextSecondary
        )
    }
}

@Composable
fun AchievementCardItem(ach: AchievementEntity) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val formattedUnlock = remember(ach.unlockedAt) {
        ach.unlockedAt?.let { sdf.format(Date(it)) } ?: ""
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (ach.isUnlocked) Color(0xFF232520) else SlateCard
        ),
        border = if (ach.isUnlocked) androidx.compose.foundation.BorderStroke(1.dp, GoldProgress.copy(alpha = 0.4f)) else androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (ach.isUnlocked) Icons.Default.Stars else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (ach.isUnlocked) GoldProgress else SlateTextSecondary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ach.name,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = ach.description,
                    color = SlateTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
            if (ach.isUnlocked) {
                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF382F10)
                    ) {
                        Text(
                            text = "UNLOCKED",
                            color = GoldProgress,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = formattedUnlock,
                        color = SlateTextSecondary,
                        fontSize = 8.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BossMissionCardItem(boss: BossMissionEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (boss.isCompleted) Color(0xFF14241E) else SlateCard
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (boss.isCompleted) NeonGreen.copy(alpha = 0.4f) else BossRed.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = boss.title,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = boss.description,
                        color = SlateTextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (boss.isCompleted) Color(0xFF11381E) else Color(0xFF3A1818)
                ) {
                    Text(
                        text = "+${boss.rewardXp} XP",
                        color = if (boss.isCompleted) NeonGreen else BossRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { boss.progressPercentage / 100f },
                    modifier = Modifier
                        .weight(1.0f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (boss.isCompleted) NeonGreen else BossRed,
                    trackColor = DarkTextSecondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${boss.progressPercentage.toInt()}%",
                    color = if (boss.isCompleted) NeonGreen else BossRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun InsightCardItem(insight: InsightEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SlateCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.OfflineBolt,
                contentDescription = null,
                tint = ElectricCyan,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = insight.content,
                color = Color.White,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                modifier = Modifier.weight(1.0f)
            )
        }
    }
}
