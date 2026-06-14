package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.SlateCard
import com.example.ui.viewmodel.OperatorViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppLayout()
            }
        }
    }
}

@Composable
fun MainAppLayout() {
    val viewModel: OperatorViewModel = viewModel()
    val currentScreen by viewModel.currentScreen.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize().background(ObsidianBackground),
        bottomBar = {
            NavigationBar(
                containerColor = SlateCard,
                tonalElevation = 8.dp,
                modifier = Modifier.height(72.dp)
            ) {
                NavigationBarItem(
                    selected = currentScreen == "home",
                    onClick = { viewModel.navigateTo("home") },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home Screen", modifier = Modifier.size(24.dp)) },
                    label = { Text("Home", color = if (currentScreen == "home") NeonGreen else Color.Gray) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = NeonGreen.copy(alpha = 0.15f),
                        selectedIconColor = NeonGreen,
                        unselectedIconColor = Color.Gray
                    ),
                    modifier = Modifier.testTag("nav_tab_home")
                )
                NavigationBarItem(
                    selected = currentScreen == "plan",
                    onClick = { viewModel.navigateTo("plan") },
                    icon = { Icon(Icons.Default.EditNote, contentDescription = "Plan Tomorrow", modifier = Modifier.size(24.dp)) },
                    label = { Text("Plan", color = if (currentScreen == "plan") NeonGreen else Color.Gray) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = NeonGreen.copy(alpha = 0.15f),
                        selectedIconColor = NeonGreen,
                        unselectedIconColor = Color.Gray
                    ),
                    modifier = Modifier.testTag("nav_tab_plan")
                )
                NavigationBarItem(
                    selected = currentScreen == "focus",
                    onClick = { viewModel.navigateTo("focus") },
                    icon = { Icon(Icons.Default.Timer, contentDescription = "Focus Mode Timer", modifier = Modifier.size(24.dp)) },
                    label = { Text("Focus", color = if (currentScreen == "focus") NeonGreen else Color.Gray) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = NeonGreen.copy(alpha = 0.15f),
                        selectedIconColor = NeonGreen,
                        unselectedIconColor = Color.Gray
                    ),
                    modifier = Modifier.testTag("nav_tab_focus")
                )
                NavigationBarItem(
                    selected = currentScreen == "debrief",
                    onClick = { viewModel.navigateTo("debrief") },
                    icon = { Icon(Icons.Default.Assignment, contentDescription = "Daily Reflection Debrief", modifier = Modifier.size(24.dp)) },
                    label = { Text("Debrief", color = if (currentScreen == "debrief") NeonGreen else Color.Gray) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = NeonGreen.copy(alpha = 0.15f),
                        selectedIconColor = NeonGreen,
                        unselectedIconColor = Color.Gray
                    ),
                    modifier = Modifier.testTag("nav_tab_debrief")
                )
                NavigationBarItem(
                    selected = currentScreen == "progress",
                    onClick = { viewModel.navigateTo("progress") },
                    icon = { Icon(Icons.Default.MilitaryTech, contentDescription = "Progression Hub", modifier = Modifier.size(24.dp)) },
                    label = { Text("Progress", color = if (currentScreen == "progress") NeonGreen else Color.Gray) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = NeonGreen.copy(alpha = 0.15f),
                        selectedIconColor = NeonGreen,
                        unselectedIconColor = Color.Gray
                    ),
                    modifier = Modifier.testTag("nav_tab_progress")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ObsidianBackground)
        ) {
            when (currentScreen) {
                "home" -> HomeScreen(viewModel = viewModel)
                "plan" -> PlanScreen(viewModel = viewModel)
                "focus" -> FocusScreen(viewModel = viewModel)
                "debrief" -> DebriefScreen(viewModel = viewModel)
                "progress" -> ProgressScreen(viewModel = viewModel)
                else -> HomeScreen(viewModel = viewModel)
            }
        }
    }
}
