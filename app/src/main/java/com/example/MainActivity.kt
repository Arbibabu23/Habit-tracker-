package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.HabitOSTheme
import com.example.ui.theme.SlateDarkBackground

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      HabitOSTheme {
        val viewModel: MainViewModel = viewModel()
        val currentScreen by viewModel.currentScreen.collectAsState()

        val showBottomBar = currentScreen != MainViewModel.Screen.Login && 
                            currentScreen != MainViewModel.Screen.Onboarding

        Scaffold(
          modifier = Modifier.fillMaxSize(),
          containerColor = SlateDarkBackground,
          bottomBar = {
            if (showBottomBar) {
              NavigationBar(
                containerColor = SlateDarkBackground,
                contentColor = Color.White
              ) {
                // Home (Dashboard)
                NavigationBarItem(
                  selected = currentScreen == MainViewModel.Screen.Dashboard,
                  onClick = { viewModel.navigateTo(MainViewModel.Screen.Dashboard) },
                  icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                  label = { Text("Home", fontSize = 11.sp) },
                  colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = Color.LightGray,
                    unselectedTextColor = Color.LightGray
                  ),
                  modifier = Modifier.testTag("nav_home")
                )

                // Habits
                NavigationBarItem(
                  selected = currentScreen == MainViewModel.Screen.Habits,
                  onClick = { viewModel.navigateTo(MainViewModel.Screen.Habits) },
                  icon = { Icon(Icons.Default.FormatListBulleted, contentDescription = "Habits") },
                  label = { Text("Habits", fontSize = 11.sp) },
                  colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = Color.LightGray,
                    unselectedTextColor = Color.LightGray
                  ),
                  modifier = Modifier.testTag("nav_habits")
                )

                // Trackers
                NavigationBarItem(
                  selected = currentScreen == MainViewModel.Screen.Trackers,
                  onClick = { viewModel.navigateTo(MainViewModel.Screen.Trackers) },
                  icon = { Icon(Icons.Default.Tune, contentDescription = "Trackers") },
                  label = { Text("Trackers", fontSize = 11.sp) },
                  colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = Color.LightGray,
                    unselectedTextColor = Color.LightGray
                  ),
                  modifier = Modifier.testTag("nav_trackers")
                )

                // Analytics
                NavigationBarItem(
                  selected = currentScreen == MainViewModel.Screen.Analytics,
                  onClick = { viewModel.navigateTo(MainViewModel.Screen.Analytics) },
                  icon = { Icon(Icons.Default.PieChart, contentDescription = "Charts") },
                  label = { Text("Analytics", fontSize = 11.sp) },
                  colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = Color.LightGray,
                    unselectedTextColor = Color.LightGray
                  ),
                  modifier = Modifier.testTag("nav_analytics")
                )

                // Community
                NavigationBarItem(
                  selected = currentScreen == MainViewModel.Screen.Community,
                  onClick = { viewModel.navigateTo(MainViewModel.Screen.Community) },
                  icon = { Icon(Icons.Default.Group, contentDescription = "Arena") },
                  label = { Text("Arena", fontSize = 11.sp) },
                  colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = Color.LightGray,
                    unselectedTextColor = Color.LightGray
                  ),
                  modifier = Modifier.testTag("nav_arena")
                )

                // Profile
                NavigationBarItem(
                  selected = currentScreen == MainViewModel.Screen.Profile,
                  onClick = { viewModel.navigateTo(MainViewModel.Screen.Profile) },
                  icon = { Icon(Icons.Default.Person, contentDescription = "Identity") },
                  label = { Text("Identity", fontSize = 11.sp) },
                  colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = Color.LightGray,
                    unselectedTextColor = Color.LightGray
                  ),
                  modifier = Modifier.testTag("nav_identity")
                )
              }
            }
          }
        ) { innerPadding ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding)
          ) {
            when (currentScreen) {
              MainViewModel.Screen.Login -> LoginScreen(viewModel = viewModel)
              MainViewModel.Screen.Onboarding -> OnboardingScreen(viewModel = viewModel)
              MainViewModel.Screen.Dashboard -> DashboardScreen(viewModel = viewModel)
              MainViewModel.Screen.Habits -> HabitsScreen(viewModel = viewModel)
              MainViewModel.Screen.Trackers -> TrackersScreen(viewModel = viewModel)
              MainViewModel.Screen.Analytics -> AnalyticsScreen(viewModel = viewModel)
              MainViewModel.Screen.Community -> CommunityScreen(viewModel = viewModel)
              MainViewModel.Screen.Profile -> ProfileScreen(viewModel = viewModel)
            }
          }
        }
      }
    }
  }
}
