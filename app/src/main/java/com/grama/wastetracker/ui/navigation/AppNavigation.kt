package com.grama.wastetracker.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.grama.wastetracker.data.model.UserRole
import com.grama.wastetracker.ui.components.BottomNavBar
import com.grama.wastetracker.ui.screens.*
import com.grama.wastetracker.viewmodel.AuthViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.state.collectAsState()

    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route

    // Show loading while auth resolves
    if (authState.loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val isLoggedIn = authState.user != null && authState.profile != null
    val isAdmin = authState.profile?.userRole == UserRole.ADMIN
    val showBottomBar = isLoggedIn && currentRoute != "login"

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) "dashboard" else "login"
        ) {
            composable("login") {
                LoginScreen(
                    authViewModel = authViewModel,
                    onLoginSuccess = {
                        navController.navigate("dashboard") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }

            composable("dashboard") {
                DashboardScreen(
                    profile = authState.profile,
                    authViewModel = authViewModel,
                    onNavigate = { route -> navController.navigate(route) },
                    onSignOut = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable("map") {
                LiveMapScreen()
            }

            composable("report") {
                ReportIssueScreen(
                    onSubmitSuccess = {
                        navController.navigate("dashboard") {
                            popUpTo("dashboard") { inclusive = true }
                        }
                    }
                )
            }

            composable("education") {
                EducationScreen()
            }

            composable("admin") {
                // Guard: only admins
                if (isAdmin) {
                    AdminDashboardScreen()
                } else {
                    LaunchedEffect(Unit) {
                        navController.navigate("dashboard") {
                            popUpTo("admin") { inclusive = true }
                        }
                    }
                }
            }
        }

        // Bottom Navigation
        if (showBottomBar) {
            BottomNavBar(
                currentRoute = currentRoute,
                isAdmin = isAdmin,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo("dashboard") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
