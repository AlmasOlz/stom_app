package com.example.stomatology.app.presentation.navigation

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.stomatology.app.presentation.ai_analysis.AiAnalysisScreen
import com.example.stomatology.app.presentation.auth.AuthViewModel
import com.example.stomatology.app.presentation.auth.LoginScreen
import com.example.stomatology.app.presentation.auth.RegistrationScreen
import com.example.stomatology.app.presentation.booking.BookingScreen
import com.example.stomatology.app.presentation.clinics.ClinicDetailScreen
import com.example.stomatology.app.presentation.clinics.ClinicListScreen
import com.example.stomatology.app.presentation.doctor_dashboard.DoctorAppointmentDetailScreen
import com.example.stomatology.app.presentation.doctor_dashboard.DoctorAppointmentListScreen
import com.example.stomatology.app.presentation.doctor_dashboard.DoctorAppointmentViewModel
import com.example.stomatology.app.presentation.doctor_dashboard.DoctorDashboardScreen
import com.example.stomatology.app.presentation.doctor_dashboard.DoctorDashboardViewModel
import com.example.stomatology.app.presentation.home.HomeScreen
import com.example.stomatology.app.presentation.notifications.NotificationHistoryScreen
import com.example.stomatology.app.presentation.profile.ProfileScreen
import com.example.stomatology.app.presentation.records.MyRecordsScreen
import com.example.stomatology.app.presentation.theme.PrimaryBlue
import com.example.stomatology.app.presentation.tracking.TrackingScreen

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
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.state.collectAsState()

    val patientBottomBarRoutes = listOf(
        BottomNavItem.Notifications.route,
        BottomNavItem.Dashboard.route,
        BottomNavItem.Home.route,
        BottomNavItem.Records.route,
        BottomNavItem.Profile.route
    )

    val doctorBottomBarRoutes = listOf(
        DoctorRoutes.Dashboard,
        DoctorRoutes.Appointments,
        DoctorRoutes.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(authState.isSuccess, authState.role) {
        if (authState.isSuccess) {
            when (authState.role) {
                "doctor" -> {
                    navController.navigate(DoctorRoutes.Dashboard) {
                        popUpTo("login") { inclusive = true }
                        launchSingleTop = true
                    }
                    authViewModel.clearSuccess()
                }

                "patient" -> {
                    navController.navigate(BottomNavItem.Home.route) {
                        popUpTo("login") { inclusive = true }
                        launchSingleTop = true
                    }
                    authViewModel.clearSuccess()
                }
            }
        }
    }

    Scaffold(
        bottomBar = {
            when {
                currentRoute in patientBottomBarRoutes -> {
                    BottomNavigationBar(navController, currentRoute)
                }

                currentRoute in doctorBottomBarRoutes -> {
                    DoctorBottomNavigationBar(navController, currentRoute)
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(padding)
        ) {
            composable("login") {
                LoginScreen(
                    onLoginSuccess = {},
                    onNavigateToRegister = {
                        navController.navigate("register")
                    },
                    viewModel = authViewModel
                )
            }

            composable("register") {
                RegistrationScreen(
                    onRegisterSuccess = {
                        navController.navigate(BottomNavItem.Home.route) {
                            popUpTo("login") { inclusive = true }
                            launchSingleTop = true
                        }
                        authViewModel.clearSuccess()
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    },
                    viewModel = authViewModel
                )
            }

            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    onNavigateToClinics = { service ->
                        navController.navigate("clinics/${Uri.encode(service)}")
                    },
                    onNavigateToAi = {
                        navController.navigate("ai_analysis")
                    },
                    onNavigateToOtherServices = {}
                )
            }

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
                        navController.popBackStack()
                    }
                )
            }

            composable("ai_analysis") {
                AiAnalysisScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(BottomNavItem.Notifications.route) {
                NotificationHistoryScreen {
                    navController.popBackStack()
                }
            }

            composable(BottomNavItem.Dashboard.route) {
                TrackingScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToReminders = {},
                    onNavigateToLesson = {}
                )
            }

            composable(BottomNavItem.Records.route) {
                MyRecordsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    onEditProfile = {},
                    onNotifications = {}
                )
            }

            composable(DoctorRoutes.Dashboard) {
                val doctorDashboardViewModel = hiltViewModel<DoctorDashboardViewModel>()

                DoctorDashboardScreen(
                    viewModel = doctorDashboardViewModel,
                    onOpenAppointments = {
                        navController.navigate(DoctorRoutes.Appointments)
                    },
                    onOpenAppointmentDetail = { appointmentId ->
                        navController.navigate(DoctorRoutes.appointmentDetail(appointmentId))
                    }
                )
            }

            composable(DoctorRoutes.Appointments) {
                val doctorAppointmentViewModel = hiltViewModel<DoctorAppointmentViewModel>()

                DoctorAppointmentListScreen(
                    viewModel = doctorAppointmentViewModel,
                    onBack = { navController.popBackStack() },
                    onOpenAppointmentDetail = { appointmentId ->
                        navController.navigate(DoctorRoutes.appointmentDetail(appointmentId))
                    }
                )
            }

            composable(DoctorRoutes.AppointmentDetail) { backStack ->
                val appointmentId = backStack.arguments?.getString("appointmentId") ?: ""
                val doctorAppointmentViewModel = hiltViewModel<DoctorAppointmentViewModel>()

                DoctorAppointmentDetailScreen(
                    appointmentId = appointmentId,
                    viewModel = doctorAppointmentViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(DoctorRoutes.Profile) {
                ProfileScreen(
                    onEditProfile = {},
                    onNotifications = {}
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
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
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

@Composable
fun DoctorBottomNavigationBar(
    navController: NavHostController,
    currentRoute: String?
) {
    val items = listOf(
        DoctorBottomNavItem.Dashboard,
        DoctorBottomNavItem.Appointments,
        DoctorBottomNavItem.Profile
    )

    NavigationBar(containerColor = PrimaryBlue) {
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
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