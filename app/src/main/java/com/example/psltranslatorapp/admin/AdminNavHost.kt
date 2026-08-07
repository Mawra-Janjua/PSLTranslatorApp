package com.example.psltranslatorapp.admin

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.psltranslatorapp.ui.SettingsScreen

@Composable
fun AdminNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AdminRoutes.LOGIN
    ) {
        composable(AdminRoutes.LOGIN) { AdminLoginScreen(navController) }
        composable(AdminRoutes.DASHBOARD) { AdminDashboardScreen(navController) }
        composable(AdminRoutes.ADD_SIGN) { AddSignScreen(navController) }
        composable(AdminRoutes.EDIT_DELETE) { EditDeleteScreen(navController) }
        composable(AdminRoutes.STATS) { StatsScreen(navController) }
        composable(AdminRoutes.TEST_AI) { TestAIScreen(navController) }
        composable(AdminRoutes.UPDATE_ML) { UpdateMLScreen(navController) }

        composable(AdminRoutes.SETTINGS) {
            SettingsScreen(isAdmin = true)
        }
    }
}