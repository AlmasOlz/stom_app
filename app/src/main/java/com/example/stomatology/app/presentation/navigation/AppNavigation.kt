package com.example.stomatology.app.presentation.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.stomatology.app.core.firebase.UserRoles
import com.example.stomatology.app.presentation.admin.AdminDashboardScreen
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
import com.example.stomatology.app.presentation.education.InstructionsScreen
import com.example.stomatology.app.presentation.education.LessonScreen
import com.example.stomatology.app.presentation.home.HomeScreen
import com.example.stomatology.app.presentation.notifications.NotificationHistoryScreen
import com.example.stomatology.app.presentation.profile.NotificationSettingsScreen
import com.example.stomatology.app.presentation.profile.ProfileEditScreen
import com.example.stomatology.app.presentation.profile.ProfileScreen
import com.example.stomatology.app.presentation.records.MyRecordsScreen
import com.example.stomatology.app.presentation.recovery.OtherServicesScreen
import com.example.stomatology.app.presentation.reminders.RemindersScreen
import com.example.stomatology.app.presentation.theme.PrimaryBlue
import com.example.stomatology.app.presentation.tracking.TrackingScreen

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector
) {
    object Notifications : BottomNavItem("notifications", Icons.Default.Notifications)
    object Dashboard : BottomNavItem("dashboard", Icons.Default.Menu)
    object Home : BottomNavItem("home", Icons.Default.Home)
    object Records : BottomNavItem("records", Icons.AutoMirrored.Filled.List)
    object Profile : BottomNavItem("profile", Icons.Default.Person)
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

    val adminBottomBarRoutes = listOf(
        AdminRoutes.Dashboard,
        AdminRoutes.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(Unit) {
        authViewModel.bootstrapSession()
    }

    LaunchedEffect(authState.isSuccess, authState.role, currentRoute) {
        if (authState.isSuccess) {
            val popRoute = if (currentRoute == "bootstrap") "bootstrap" else "login"
            when (authState.role) {
                UserRoles.DOCTOR -> {
                    navController.navigate(DoctorRoutes.Dashboard) {
                        popUpTo(popRoute) { inclusive = true }
                        launchSingleTop = true
                    }
                    authViewModel.clearSuccess()
                }

                UserRoles.PATIENT -> {
                    navController.navigate(BottomNavItem.Home.route) {
                        popUpTo(popRoute) { inclusive = true }
                        launchSingleTop = true
                    }
                    authViewModel.clearSuccess()
                }

                UserRoles.ADMIN -> {
                    navController.navigate(AdminRoutes.Dashboard) {
                        popUpTo(popRoute) { inclusive = true }
                        launchSingleTop = true
                    }
                    authViewModel.clearSuccess()
                }
            }
        }
    }

    LaunchedEffect(authState.isSessionChecked, authState.isSuccess, currentRoute) {
        if (authState.isSessionChecked && !authState.isSuccess && currentRoute == "bootstrap") {
            navController.navigate("login") {
                popUpTo("bootstrap") { inclusive = true }
                launchSingleTop = true
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

                currentRoute in adminBottomBarRoutes -> {
                    AdminBottomNavigationBar(navController, currentRoute)
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "bootstrap",
            modifier = Modifier.padding(padding)
        ) {
            composable("bootstrap") {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }

            composable("login") {
                LoginScreen(
                    onNavigateToRegister = {
                        navController.navigate("register")
                    },
                    viewModel = authViewModel
                )
            }

            composable("register") {
                RegistrationScreen(
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
                    onNavigateToOtherServices = {
                        navController.navigate("other_services")
                    }
                )
            }

            composable("other_services") {
                OtherServicesScreen(
                    onBack = { navController.popBackStack() },
                    onServiceSelected = { service ->
                        navController.navigate("clinics/${Uri.encode(service)}")
                    }
                )
            }

            composable(BottomNavItem.Dashboard.route) {
                TrackingScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToReminders = {
                        navController.navigate("daily_reminders")
                    },
                    onNavigateToInstructions = {
                        navController.navigate("instructions")
                    },
                    onNavigateToLesson = { lessonType ->
                        navController.navigate("lesson/$lessonType")
                    }
                )
            }

            composable("instructions") {
                InstructionsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable("daily_reminders") {
                RemindersScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable("lesson/{lessonType}") { backStack ->
                val lessonType = backStack.arguments?.getString("lessonType") ?: "brushing"

                LessonScreen(
                    topic = lessonType,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("clinics/{serviceName}") { backStack ->
                val service = Uri.decode(backStack.arguments?.getString("serviceName") ?: "")

                ClinicListScreen(
                    serviceName = service,
                    onBack = { navController.popBackStack() },
                    onClinicClick = { id ->
                        navController.navigate("clinic_detail/${Uri.encode(id)}/${Uri.encode(service)}")
                    }
                )
            }

            composable("clinic_detail/{clinicId}/{serviceName}") { backStack ->
                val id = Uri.decode(backStack.arguments?.getString("clinicId") ?: "")
                val service = Uri.decode(backStack.arguments?.getString("serviceName") ?: "")

                ClinicDetailScreen(
                    clinicId = id,
                    serviceName = service,
                    onBack = { navController.popBackStack() },
                    onBookClick = { clinicId, serviceName ->
                        navController.navigate("booking/${Uri.encode(clinicId)}/${Uri.encode(serviceName)}")
                    }
                )
            }

            composable("booking/{clinicId}/{serviceName}") { backStack ->
                val id = Uri.decode(backStack.arguments?.getString("clinicId") ?: "")
                val service = Uri.decode(backStack.arguments?.getString("serviceName") ?: "")

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

            composable(BottomNavItem.Records.route) {
                MyRecordsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    onEditProfile = {
                        runCatching { navController.navigate("profile_edit") { launchSingleTop = true } }
                    },
                    onNotifications = {
                        runCatching {
                            navController.navigate(BottomNavItem.Notifications.route) {
                                launchSingleTop = true
                            }
                        }
                    },
                    onOpenSettings = {
                        runCatching { navController.navigate("profile_settings") { launchSingleTop = true } }
                    }
                )
            }

            composable("profile_edit") {
                ProfileEditScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }

            composable("profile_settings") {
                NotificationSettingsScreen(
                    onBack = { navController.popBackStack() },
                    onSignOut = {
                        authViewModel.signOut()
                        navController.navigate("login") {
                            popUpTo("bootstrap") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
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
                    onEditProfile = {
                        runCatching { navController.navigate("profile_edit") { launchSingleTop = true } }
                    },
                    onNotifications = {
                        runCatching {
                            navController.navigate(BottomNavItem.Notifications.route) {
                                launchSingleTop = true
                            }
                        }
                    },
                    onOpenSettings = {
                        runCatching { navController.navigate("profile_settings") { launchSingleTop = true } }
                    }
                )
            }

            composable(AdminRoutes.Dashboard) {
                AdminDashboardScreen()
            }

            composable(AdminRoutes.Profile) {
                ProfileScreen(
                    onEditProfile = {
                        runCatching { navController.navigate("profile_edit") { launchSingleTop = true } }
                    },
                    onNotifications = {
                        runCatching {
                            navController.navigate(BottomNavItem.Notifications.route) {
                                launchSingleTop = true
                            }
                        }
                    },
                    onOpenSettings = {
                        runCatching { navController.navigate("profile_settings") { launchSingleTop = true } }
                    }
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
                        contentDescription = null
                    )
                },
                selected = currentRoute == item.route,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    unselectedIconColor = Color.White.copy(alpha = 0.75f),
                    indicatorColor = Color.White.copy(alpha = 0.18f)
                ),
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
                        contentDescription = null
                    )
                },
                selected = currentRoute == item.route,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    unselectedIconColor = Color.White.copy(alpha = 0.75f),
                    indicatorColor = Color.White.copy(alpha = 0.18f)
                ),
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
fun AdminBottomNavigationBar(
    navController: NavHostController,
    currentRoute: String?
) {
    val items = listOf(
        AdminBottomNavItem.Dashboard,
        AdminBottomNavItem.Profile
    )

    NavigationBar(containerColor = PrimaryBlue) {
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null
                    )
                },
                selected = currentRoute == item.route,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    unselectedIconColor = Color.White.copy(alpha = 0.75f),
                    indicatorColor = Color.White.copy(alpha = 0.18f)
                ),
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
