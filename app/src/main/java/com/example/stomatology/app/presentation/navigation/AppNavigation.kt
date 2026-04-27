package com.example.stomatology.app.presentation.navigation

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.stomatology.app.presentation.ai_analysis.AiAnalysisScreen
import com.example.stomatology.app.presentation.auth.LoginScreen
import com.example.stomatology.app.presentation.auth.RegistrationScreen
import com.example.stomatology.app.presentation.booking.BookingScreen
import com.example.stomatology.app.presentation.clinics.ClinicDetailScreen
import com.example.stomatology.app.presentation.clinics.ClinicListScreen
import com.example.stomatology.app.presentation.home.HomeScreen
import com.example.stomatology.app.presentation.education.LessonScreen
import com.example.stomatology.app.presentation.education.InstructionsScreen // ЖАҢА ИМПОРТ
import com.example.stomatology.app.presentation.notifications.NotificationHistoryScreen
import com.example.stomatology.app.presentation.profile.ProfileScreen
import com.example.stomatology.app.presentation.records.MyRecordsScreen
import com.example.stomatology.app.presentation.theme.PrimaryBlue
import com.example.stomatology.app.presentation.tracking.TrackingScreen
import com.example.stomatology.app.presentation.reminders.RemindersScreen

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Notifications : BottomNavItem("notifications", Icons.Default.Notifications, "Уведомления")
    object Dashboard : BottomNavItem("dashboard", Icons.Default.Menu, "Прогресс")
    object Home : BottomNavItem("home", Icons.Default.Home, "Главная")
    object Records : BottomNavItem("records", Icons.AutoMirrored.Filled.List, "Записи")
    object Profile : BottomNavItem("profile", Icons.Default.Person, "Профиль")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val bottomBarRoutes = listOf(
        BottomNavItem.Notifications.route,
        BottomNavItem.Dashboard.route,
        BottomNavItem.Home.route,
        BottomNavItem.Records.route,
        BottomNavItem.Profile.route
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomBarRoutes) {
                BottomNavigationBar(navController, currentRoute)
            }
        }
    ) { padding ->

        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(padding)
        ) {

            // --- HOME ---
            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    onNavigateToClinics = { service ->
                        navController.navigate("clinics/${Uri.encode(service)}")
                    },
                    onNavigateToAi = { navController.navigate("ai_analysis") },
                    onNavigateToOtherServices = { }
                )
            }

            // --- DASHBOARD / TRACKING ---
            composable(BottomNavItem.Dashboard.route) {
                TrackingScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToReminders = {
                        navController.navigate("daily_reminders")
                    },
                    onNavigateToInstructions = { // ЖАҢА ПАРАМЕТР
                        navController.navigate("instructions")
                    },
                    onNavigateToLesson = { lessonType ->
                        navController.navigate("lesson/$lessonType")
                    }
                )
            }

            // --- INSTRUCTIONS SCREEN (СОВЕТЫ) ---
            composable("instructions") {
                InstructionsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // --- DAILY REMINDERS SCREEN ---
            composable("daily_reminders") {
                RemindersScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // --- LESSON SCREEN ---
            composable("lesson/{lessonType}") { backStack ->
                val lessonType = backStack.arguments?.getString("lessonType") ?: "brushing"
                LessonScreen(
                    topic = lessonType,
                    onBack = { navController.popBackStack() }
                )
            }

            // --- ҚАЛҒАН БЕТТЕР (Өзгеріссіз қалды) ---
            composable("clinics/{serviceName}") { backStack ->
                val service = backStack.arguments?.getString("serviceName") ?: ""
                ClinicListScreen(
                    serviceName = service,
                    onBack = { navController.popBackStack() },
                    onClinicClick = { id ->
                        navController.navigate("clinic_detail/$id/${Uri.encode(service)}")
                    }
                )
            }

            composable("clinic_detail/{clinicId}/{serviceName}") { backStack ->
                val id = backStack.arguments?.getString("clinicId") ?: ""
                val service = backStack.arguments?.getString("serviceName") ?: ""
                ClinicDetailScreen(
                    clinicId = id,
                    serviceName = service,
                    onBack = { navController.popBackStack() },
                    onBookClick = { clinicId, serviceName ->
                        navController.navigate("booking/$clinicId/${Uri.encode(serviceName)}")
                    }
                )
            }

            composable("booking/{clinicId}/{serviceName}") { backStack ->
                val id = backStack.arguments?.getString("clinicId") ?: ""
                val service = backStack.arguments?.getString("serviceName") ?: ""
                BookingScreen(
                    clinicId = id,
                    serviceName = service,
                    onBookingComplete = {
                        navController.popBackStack(BottomNavItem.Home.route, false)
                    }
                )
            }

            composable("ai_analysis") {
                AiAnalysisScreen(onBack = { navController.popBackStack() })
            }

            composable(BottomNavItem.Notifications.route) {
                NotificationHistoryScreen { navController.popBackStack() }
            }

            composable(BottomNavItem.Records.route) {
                MyRecordsScreen { navController.popBackStack() }
            }

            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    onEditProfile = {},
                    onNotifications = {}
                )
            }

            composable("login") {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate("register") }
                )
            }

            composable("register") {
                RegistrationScreen(
                    onRegisterSuccess = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    currentRoute: String?
) {
    val items = listOf(
        BottomNavItem.Notifications,
        BottomNavItem.Dashboard,
        BottomNavItem.Home,
        BottomNavItem.Records,
        BottomNavItem.Profile
    )

    NavigationBar(containerColor = PrimaryBlue) {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}