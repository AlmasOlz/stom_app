package com.example.stomatology.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.stomatology.app.R

sealed class AdminBottomNavItem(
    val route: String,
    val icon: ImageVector,
    val labelRes: Int
) {
    object Dashboard : AdminBottomNavItem(
        AdminRoutes.Dashboard,
        Icons.Default.AdminPanelSettings,
        R.string.nav_admin
    )

    object Users : AdminBottomNavItem(
        AdminRoutes.Users,
        Icons.Default.SupervisorAccount,
        R.string.nav_users
    )

    object Clinics : AdminBottomNavItem(
        AdminRoutes.Clinics,
        Icons.Default.LocalHospital,
        R.string.nav_clinics
    )

    object Profile : AdminBottomNavItem(
        AdminRoutes.Profile,
        Icons.Default.Person,
        R.string.nav_profile
    )
}
