package com.nfcscanner.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nfcscanner.app.ui.screens.*
import com.nfcscanner.app.viewmodel.MainViewModel

/**
 * Navigation principale dell'app con Bottom Navigation
 * Mantiene la stessa struttura dell'app React Native
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NFCScannerNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { 
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "scanner",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("scanner") {
                ScannerScreen(viewModel = viewModel)
            }
            composable("history") {
                HistoryScreen(
                    viewModel = viewModel,
                    onTagClick = { tagId ->
                        navController.navigate("details/$tagId")
                    }
                )
            }
            composable("details/{tagId}") { backStackEntry ->
                val tagId = backStackEntry.arguments?.getString("tagId") ?: ""
                TagDetailsScreen(
                    tagId = tagId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("emulator") {
                EmulatorScreen(
                    viewModel = viewModel,
                    onTagClick = { tagId ->
                        navController.navigate("details/$tagId")
                    }
                )
            }
            composable("settings") {
                SettingsScreen(viewModel = viewModel)
            }
        }
    }
}

data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

val bottomNavItems = listOf(
    BottomNavItem("Scanner", Icons.Default.Search, "scanner"),
    BottomNavItem("History", Icons.Default.History, "history"),
    BottomNavItem("Emulator", Icons.Default.FlashOn, "emulator"),
    BottomNavItem("Settings", Icons.Default.Settings, "settings")
)