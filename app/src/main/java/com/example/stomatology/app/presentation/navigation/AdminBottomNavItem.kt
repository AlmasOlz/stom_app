package com.example.stomatology.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AdminBottomNavItem(
    val route: String,
    val icon: ImageVector
) {
    object Dashboard : AdminBottomNavItem(
        AdminRoutes.Dashboard,
        Icons.Default.AdminPanelSettings
    )

    object Profile : AdminBottomNavItem(
        AdminRoutes.Profile,
        Icons.Default.Person
    )
}
