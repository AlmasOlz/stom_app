package com.example.stomatology.app.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.stomatology.app.R

sealed class AdminBottomNavItem(
    val route: String,
    val icon: ImageVector,
    @StringRes val labelRes: Int
) {
    object Dashboard : AdminBottomNavItem(
        AdminRoutes.Dashboard,
        Icons.Default.AdminPanelSettings,
        R.string.nav_admin
    )

    object Profile : AdminBottomNavItem(
        AdminRoutes.Profile,
        Icons.Default.Person,
        R.string.nav_profile
    )
}
