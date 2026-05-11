package com.example.stomatology.app.presentation.navigation

import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.stomatology.app.R
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
import com.example.stomatology.app.presentation.theme.SecondaryBlue
import com.example.stomatology.app.presentation.tracking.TrackingScreen

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    @StringRes val labelRes: Int
) {
    object Notifications : BottomNavItem("notifications", Icons.Default.Notifications, R.string.nav_notifications)
    object Dashboard : BottomNavItem("dashboard", Icons.Default.Menu, R.string.nav_dashboard)
    object Home : BottomNavItem("home", Icons.Default.Home, R.string.nav_home)
    object Records : BottomNavItem("records", Icons.AutoMirrored.Filled.List, R.string.nav_records)
    object Profile : BottomNavItem("profile", Icons.Default.Person, R.string.nav_profile)
}

private data class NavBarTab(
    val route: String,
    val icon: ImageVector,
    @StringRes val labelRes: Int
)

@Composable
private fun StyledBottomNavigationBar(
    currentRoute: String?,
    tabs: List<NavBarTab>,
    onNavigateTo: (String) -> Unit
) {
    val shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        tonalElevation = 2.dp
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            tabs.forEach { tab ->
                val selected = currentRoute == tab.route
                val label = stringResource(tab.labelRes)
                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        if (currentRoute != tab.route) {
                            onNavigateTo(tab.route)
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = label
                        )
                    },
                    label = { },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryBlue,
                        selectedTextColor = PrimaryBlue,
                        indicatorColor = SecondaryBlue,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
                    ),
                    alwaysShowLabel = false
                )
            }
        }
    }
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
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF121212))
                ) {
                    val circleSide = minOf(maxWidth, maxHeight) * 0.38f
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(y = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(circleSide)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_launcher_foreground_logo),
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    // Жоғарыдағы жұлдызшалар шеңбер шетіне тимесін — біркелкі кішірейту
                                    .padding(
                                        start = circleSide * 0.24f,
                                        top = circleSide * 0.30f,
                                        end = circleSide * 0.24f,
                                        bottom = circleSide * 0.22f
                                    )
                            )
                        }
                        Spacer(modifier = Modifier.height(28.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(30.dp),
                            color = PrimaryBlue,
                            strokeWidth = 2.dp
                        )
                    }
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
                    },
                    onQuickRebook = { clinicId, service ->
                        navController.navigate("booking/${Uri.encode(clinicId)}/${Uri.encode(service)}")
                    },
                    onOpenClinic = { clinicId, service ->
                        navController.navigate("clinic_detail/${Uri.encode(clinicId)}/${Uri.encode(service)}")
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
                    },
                    onDismiss = { navController.popBackStack() }
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
    val tabs = items.map { NavBarTab(it.route, it.icon, it.labelRes) }
    StyledBottomNavigationBar(
        currentRoute = currentRoute,
        tabs = tabs,
        onNavigateTo = { route ->
            navController.navigate(route) {
                popUpTo(navController.graph.startDestinationId)
                launchSingleTop = true
            }
        }
    )
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
