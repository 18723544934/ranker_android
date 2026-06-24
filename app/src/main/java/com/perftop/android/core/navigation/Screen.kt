package com.perftop.android.core.navigation

sealed class Screen(val route: String) {
    object Ranking : Screen("ranking")
    object Ladder : Screen("ladder")
    object Compare : Screen("compare")
    object Favorites : Screen("favorites")
    object Settings : Screen("settings")
    object Detail : Screen("detail/{hardwareId}") {
        fun createRoute(hardwareId: Int) = "detail/$hardwareId"
    }
}
