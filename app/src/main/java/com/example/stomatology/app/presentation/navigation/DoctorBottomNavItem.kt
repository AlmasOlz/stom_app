package com.example.stomatology.app.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.stomatology.app.R

sealed class DoctorBottomNavItem(
    val route: String,
    val icon: ImageVector,
    @StringRes val labelRes: Int
) {
    object Dashboard : DoctorBottomNavItem(
        DoctorRoutes.Dashboard,
        Icons.Default.Home,
        R.string.nav_home
    )

    object Appointments : DoctorBottomNavItem(
        DoctorRoutes.Appointments,
        Icons.AutoMirrored.Filled.List,
        R.string.nav_appointments
    )

    object Profile : DoctorBottomNavItem(
        DoctorRoutes.Profile,
        Icons.Default.Person,
        R.string.nav_profile
    )
}
