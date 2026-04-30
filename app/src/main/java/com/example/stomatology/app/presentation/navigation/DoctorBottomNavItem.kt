package com.example.stomatology.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class DoctorBottomNavItem(
    val route: String,
    val icon: ImageVector
) {
    object Dashboard : DoctorBottomNavItem(
        DoctorRoutes.Dashboard,
        Icons.Default.Home
    )

    object Appointments : DoctorBottomNavItem(
        DoctorRoutes.Appointments,
        Icons.AutoMirrored.Filled.List
    )

    object Profile : DoctorBottomNavItem(
        DoctorRoutes.Profile,
        Icons.Default.Person
    )
}
