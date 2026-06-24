package com.perftop.android.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.perftop.android.presentation.ranking.RankingScreen

data class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
)

@Composable
fun PerfTopNavGraph() {
    val navController = rememberNavController()
    val bottomNavItems = listOf(
        BottomNavItem(Screen.Ranking, Icons.Default.Home, "排行榜"),
        BottomNavItem(Screen.Ladder, Icons.Default.BarChart, "天梯图"),
        BottomNavItem(Screen.Compare, Icons.Default.BarChart, "对比"),
        BottomNavItem(Screen.Favorites, Icons.Default.Favorite, "收藏"),
        BottomNavItem(Screen.Settings, Icons.Default.Settings, "设置")
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true,
                        onClick = {
                            navController.navigate(item.screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Ranking.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Ranking.route) {
                RankingScreen(onNavigateToDetail = { hardwareId ->
                    navController.navigate(Screen.Detail.createRoute(hardwareId))
                })
            }
            composable(Screen.Ladder.route) {
                LadderScreen(onNavigateToDetail = { hardwareId ->
                    navController.navigate(Screen.Detail.createRoute(hardwareId))
                })
            }
            composable(Screen.Compare.route) {
                CompareScreen(onNavigateToDetail = { hardwareId ->
                    navController.navigate(Screen.Detail.createRoute(hardwareId))
                })
            }
            composable(Screen.Favorites.route) {
                FavoritesScreen(onNavigateToDetail = { hardwareId ->
                    navController.navigate(Screen.Detail.createRoute(hardwareId))
                })
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
            composable(Screen.Detail.route) { backStackEntry ->
                val hardwareId = backStackEntry.arguments?.getString("hardwareId")?.toIntOrNull() ?: return@composable
                DetailScreen(
                    hardwareId = hardwareId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun LadderScreen(onNavigateToDetail: (Int) -> Unit) {
    Text("天梯图页面 - 待实现")
}

@Composable
private fun CompareScreen(onNavigateToDetail: (Int) -> Unit) {
    Text("对比页面 - 待实现")
}

@Composable
private fun FavoritesScreen(onNavigateToDetail: (Int) -> Unit) {
    Text("收藏页面 - 待实现")
}

@Composable
private fun SettingsScreen() {
    Text("设置页面 - 待实现")
}

@Composable
private fun DetailScreen(hardwareId: Int, onNavigateBack: () -> Unit) {
    Text("详情页面 - ID: $hardwareId")
}
