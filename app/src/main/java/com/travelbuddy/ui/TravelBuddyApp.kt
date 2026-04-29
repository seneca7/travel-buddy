package com.travelbuddy.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.travelbuddy.TravelViewModel
import com.travelbuddy.navigation.AppRoute

private data class BottomTab(
    val route: AppRoute,
    val label: String,
    val icon: ImageVector,
)

private fun isTabSelected(tab: AppRoute, currentRoute: String?): Boolean {
    if (currentRoute == null) return false
    if (currentRoute == tab.route) return true
    if (tab == AppRoute.Matches) {
        return currentRoute.startsWith("profile/") ||
            currentRoute.startsWith("join_request/")
    }
    return false
}

private val tabRootRoutes = setOf(
    AppRoute.Home.route,
    AppRoute.Matches.route,
    AppRoute.Chat.route,
    AppRoute.Safety.route,
)

private val tabs = listOf(
    BottomTab(AppRoute.Home, "Home", Icons.Filled.Home),
    BottomTab(AppRoute.Matches, "Matches", Icons.Filled.Favorite),
    BottomTab(AppRoute.Chat, "Chat", Icons.Filled.MailOutline),
    BottomTab(AppRoute.Safety, "Safety", Icons.Filled.Info),
)

private val HorizontalScreenPadding = 24.dp

@Composable
fun TravelBuddyApp() {
    val navController = rememberNavController()
    val vm: TravelViewModel = viewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val colorScheme = MaterialTheme.colorScheme
    val showBottomBar = currentRoute in tabRootRoutes

    Scaffold(
        containerColor = colorScheme.background,
        contentColor = colorScheme.onBackground,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    colors = NavigationBarDefaults.colors(containerColor = colorScheme.surfaceContainer),
                ) {
                    tabs.forEach { tab ->
                        val selected = isTabSelected(tab.route, currentRoute)
                        NavigationBarItem(
                            selected = selected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = colorScheme.primary,
                                selectedTextColor = colorScheme.primary,
                                indicatorColor = colorScheme.primaryContainer,
                                unselectedIconColor = colorScheme.onSurfaceVariant,
                                unselectedTextColor = colorScheme.onSurfaceVariant,
                            ),
                            onClick = {
                                navController.navigate(tab.route.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { pad ->
        Surface(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize(),
            color = colorScheme.background,
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .then(
                        if (showBottomBar) {
                            Modifier.padding(horizontal = HorizontalScreenPadding)
                        } else {
                            Modifier
                        },
                    ),
            ) {
                NavHost(
                    navController = navController,
                    startDestination = AppRoute.Home.route,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    composable(AppRoute.Home.route) {
                        HomeScreen(
                            vm = vm,
                            onCreateTrip = { navController.navigate(AppRoute.Create.route) },
                        )
                    }
                    composable(AppRoute.Create.route) {
                        CreateTripScreen(
                            vm = vm,
                            onPublishSuccess = {
                                navController.navigate(AppRoute.Matches.route) {
                                    popUpTo(AppRoute.Home.route) { inclusive = false }
                                    launchSingleTop = true
                                }
                            },
                        )
                    }
                    composable(AppRoute.Matches.route) {
                        MatchesScreen(
                            vm = vm,
                            onOpenProfile = { id ->
                                navController.navigate(AppRoute.Nav.profile(id))
                            },
                        )
                    }
                    composable(
                        route = AppRoute.Nav.ProfilePattern,
                        arguments = listOf(
                            navArgument("matchId") { type = NavType.StringType },
                        ),
                    ) { backStackEntry ->
                        val matchId = checkNotNull(backStackEntry.arguments?.getString("matchId"))
                        ProfileScreen(
                            vm = vm,
                            matchId = matchId,
                            onJoinRequest = { id ->
                                navController.navigate(AppRoute.Nav.joinRequest(id))
                            },
                        )
                    }
                    composable(
                        route = AppRoute.Nav.JoinRequestPattern,
                        arguments = listOf(
                            navArgument("matchId") { type = NavType.StringType },
                        ),
                    ) { backStackEntry ->
                        val matchId = checkNotNull(backStackEntry.arguments?.getString("matchId"))
                        JoinRequestScreen(
                            vm = vm,
                            matchId = matchId,
                            onSend = {
                                navController.navigate(AppRoute.Chat.route) {
                                    popUpTo(AppRoute.Home.route) { saveState = true }
                                    launchSingleTop = true
                                }
                            },
                        )
                    }
                    composable(AppRoute.Chat.route) { ChatScreen(vm = vm) }
                    composable(AppRoute.Notifications.route) { NotificationsScreen() }
                    composable(AppRoute.Safety.route) {
                        SafetyScreen(
                            onOpenBlockedTravelers = {
                                navController.navigate(AppRoute.SafetyBlocks.route)
                            },
                            onOpenReporting = {
                                navController.navigate(AppRoute.SafetyReport.route)
                            },
                        )
                    }
                    composable(AppRoute.SafetyBlocks.route) {
                        SafetyToolkitPlaceholderScreen(
                            title = "Blocked travelers",
                            supporting = "Server-backed block lists and appeal flows ship with account services. " +
                                "For now, keep using in-app mute and report to stay in control.",
                            onNavigateBack = { navController.popBackStack() },
                        )
                    }
                    composable(AppRoute.SafetyReport.route) {
                        SafetyToolkitPlaceholderScreen(
                            title = "Safety reports",
                            supporting = "Guided intake with moderator review launches with production auth. " +
                                "This placeholder routes you back — never hesitate to escalate real harm offline too.",
                            onNavigateBack = { navController.popBackStack() },
                        )
                    }
                }
            }
        }
    }
}
