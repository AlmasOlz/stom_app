package com.example.stomatology.app.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
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
import com.example.stomatology.app.presentation.auth.RegistrationScreen
import com.example.stomatology.app.presentation.profile.ProfileScreen
import com.example.stomatology.app.presentation.recovery.RecoveryScreen
import com.example.stomatology.app.presentation.tracking.TrackingScreen
import com.example.stomatology.app.presentation.reminders.RemindersScreen
import com.example.stomatology.app.presentation.education.LessonScreen
import com.example.stomatology.app.presentation.recovery.OtherServicesScreen
import com.example.stomatology.app.presentation.profile.ProfileEditScreen
import com.example.stomatology.app.presentation.profile.NotificationSettingsScreen
import com.example.stomatology.app.presentation.notifications.NotificationHistoryScreen
import com.example.stomatology.app.presentation.clinics.PromoDetailScreen
import com.example.stomatology.app.presentation.doctors.DoctorListScreen
import com.example.stomatology.app.presentation.doctors.DoctorProfileScreen
import com.example.stomatology.app.presentation.booking.DateTimePickerScreen
import com.example.stomatology.app.presentation.booking.BookingFormScreen
import com.example.stomatology.app.presentation.records.MyRecordsScreen
// 1. ADDED ALL 5 ICONS TO MATCH MOCKUP
sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Notifications : BottomNavItem("notifications", Icons.Default.Notifications, "Уведомления")
    object Dashboard : BottomNavItem("dashboard", Icons.Default.Menu, "Прогресс")
    object Home : BottomNavItem("home", Icons.Default.Home, "Главная")
    object Records : BottomNavItem("records", Icons.Default.List, "Записи")
    object Profile : BottomNavItem("profile", Icons.Default.Person, "Профиль")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // 2. REGISTERED ALL 5 ROUTES FOR THE BOTTOM BAR
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
                BottomNavigationBar(navController = navController, currentRoute = currentRoute)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // --- BOTTOM NAV BAR SCREENS ---

            composable(BottomNavItem.Notifications.route) {
                NotificationHistoryScreen(onBack = { navController.popBackStack() })
            }

            composable(BottomNavItem.Dashboard.route) {
                TrackingScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToReminders = { navController.navigate("reminders") },
                    onNavigateToLesson = { topic -> navController.navigate("lesson/$topic") }
                )
            }

            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    onNavigateToClinics = { navController.navigate("clinics") },
                    onNavigateToAi = { navController.navigate("ai_analysis") },
                    onNavigateToOtherServices = { navController.navigate("other_services") }
                )
            }

            composable(BottomNavItem.Records.route) {
                RecoveryScreen(onBack = { navController.popBackStack() })
            }

            composable(BottomNavItem.Profile.route) {
                ProfileScreen()
            }
            composable("promo_detail") {
                PromoDetailScreen(
                    onBack = { navController.popBackStack() },
                    onBookClick = { navController.navigate("doctor_list") }
                )
            }

            // 2. Doctor List (Choosing a doctor)
            composable("doctor_list") {
                DoctorListScreen(
                    onDoctorClick = { doctorName ->
                        navController.navigate("doctor_profile/$doctorName")
                    }
                )
            }

            // 3. Doctor Profile
            composable("doctor_profile/{doctorName}") { backStackEntry ->
                val doctorName = backStackEntry.arguments?.getString("doctorName") ?: "Doctor"
                DoctorProfileScreen(
                    doctorName = doctorName,
                    onBookClick = { navController.navigate("date_time_picker") }
                )
            }

            // 4. Date & Time Picker (24-hour format)
            composable("date_time_picker") {
                DateTimePickerScreen(
                    onConfirm = { date, time ->
                        // In a real app, you'd pass date and time as arguments to the next screen
                        navController.navigate("booking_form")
                    }
                )
            }

            // 5. Final Booking Form Confirmation
            composable("booking_form") {
                BookingFormScreen(
                    onContinue = {
                        // Once booking is complete, return to Home
                        navController.popBackStack(BottomNavItem.Home.route, inclusive = false)
                    }
                )
            }
            composable(BottomNavItem.Records.route) {
                RecoveryScreen(onBack = { navController.popBackStack() })
            }
            composable(BottomNavItem.Records.route) {
                MyRecordsScreen(onBack = { navController.popBackStack() })
            }

            // --- OTHER SECONDARY SCREENS ---

            composable("profile_edit") {
                ProfileEditScreen(onBack = { navController.popBackStack() })
            }

            composable("notification_settings") {
                NotificationSettingsScreen(onBack = { navController.popBackStack() })
            }

            composable("other_services") {
                OtherServicesScreen(
                    onBack = { navController.popBackStack() },
                    onServiceSelected = { serviceName -> navController.popBackStack() }
                )
            }

            composable("reminders") {
                RemindersScreen(onBack = { navController.popBackStack() })
            }

            composable("lesson/{topic}") { backStackEntry ->
                val topic = backStackEntry.arguments?.getString("topic") ?: "default"
                LessonScreen(onBack = { navController.popBackStack() })
            }

            composable("ai_analysis") {
                AiAnalysisScreen(onBack = { navController.popBackStack() })
            }

            composable("login") {
                LoginScreen(
                    onLoginSuccess = { navController.navigate(BottomNavItem.Home.route) { popUpTo("login") { inclusive = true } } },
                    onNavigateToRegister = { navController.navigate("register") }
                )
            }

            composable("register") {
                RegistrationScreen(
                    onRegisterSuccess = { navController.navigate(BottomNavItem.Home.route) { popUpTo("login") { inclusive = true } } },
                    onNavigateToLogin = { navController.popBackStack() }
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
                    onBookingComplete = { navController.popBackStack(BottomNavItem.Home.route, inclusive = false) }
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController, currentRoute: String?) {
    // 3. SHOW ALL 5 ICONS
    val items = listOf(
        BottomNavItem.Notifications,
        BottomNavItem.Dashboard,
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