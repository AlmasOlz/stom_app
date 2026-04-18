package com.example.stomatology.app.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.stomatology.app.presentation.ai_analysis.AiAnalysisScreen
import com.example.stomatology.app.presentation.auth.LoginScreen
import com.example.stomatology.app.presentation.booking.BookingScreen
import com.example.stomatology.app.presentation.clinics.ClinicListScreen
import com.example.stomatology.app.presentation.home.HomeScreen
import com.example.stomatology.app.presentation.theme.PrimaryBlue
import androidx.compose.ui.unit.dp
import com.example.stomatology.app.presentation.auth.RegistrationScreen

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Notifications : BottomNavItem("notifications", Icons.Default.Notifications, "Уведомления")
    object Home : BottomNavItem("home", Icons.Default.Home, "Главная")
    object Records : BottomNavItem("records", Icons.Default.List, "Записи")
    object Profile : BottomNavItem("profile", Icons.Default.Person, "Профиль")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Экраны, на которых нужно показывать нижнее меню
    val bottomBarRoutes = listOf(
        BottomNavItem.Notifications.route,
        BottomNavItem.Home.route,
        BottomNavItem.Records.route,
        BottomNavItem.Profile.route
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomBarRoutes) {
                BottomNavigationBar(navController = navController, currentRoute = currentRoute)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login", // Временно оставляем home для быстрого тестирования
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    onNavigateToClinics = { navController.navigate("clinics") },
                    onNavigateToAi = { navController.navigate("ai_analysis") }
                )
            }

            // Заглушки для новых экранов нижней панели
            composable(BottomNavItem.Notifications.route) {
                // В будущем здесь будет NotificationsScreen
                Text("Экран Уведомлений", modifier = Modifier.padding(16.dp))
            }
            composable(BottomNavItem.Records.route) {
                Text("Экран Записей", modifier = Modifier.padding(16.dp))
            }
            composable(BottomNavItem.Profile.route) {
                Text("Экран Профиля", modifier = Modifier.padding(16.dp))
            }

            composable("ai_analysis") {
                AiAnalysisScreen(onBack = { navController.popBackStack() })
            }
            composable("login") {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate("home") { popUpTo("login") { inclusive = true } }
                    },
                    onNavigateToRegister = { // ВНИМАНИЕ: добавили новый коллбек
                        navController.navigate("register")
                    }
                )
            }

            // НОВЫЙ ЭКРАН РЕГИСТРАЦИИ
            composable("register") {
                RegistrationScreen(
                    onRegisterSuccess = {
                        // После успешной регистрации тоже идем на главную
                        navController.navigate("home") { popUpTo("login") { inclusive = true } }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack() // Возвращаемся назад на логин
                    }
                )
            }
            composable("clinics") {
                ClinicListScreen(
                    onBack = { navController.popBackStack() },
                    onClinicClick = { clinicId -> navController.navigate("booking/$clinicId") }
                )
            }
            composable("booking/{clinicId}") { backStackEntry ->
                val clinicId = backStackEntry.arguments?.getString("clinicId") ?: ""
                BookingScreen(
                    clinicId = clinicId,
                    onBookingComplete = {
                        navController.popBackStack("home", inclusive = false)
                    }
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController, currentRoute: String?) {
    val items = listOf(
        BottomNavItem.Notifications,
        BottomNavItem.Home,
        BottomNavItem.Records,
        BottomNavItem.Profile
    )

    NavigationBar(
        containerColor = PrimaryBlue,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                selected = currentRoute == item.route,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryBlue,
                    unselectedIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    indicatorColor = MaterialTheme.colorScheme.onPrimary
                ),
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}