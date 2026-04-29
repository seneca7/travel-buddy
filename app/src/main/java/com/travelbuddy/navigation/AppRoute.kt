package com.travelbuddy.navigation

/**
 * Single source of truth for NavHost routes (avoids string typos; aligns with Navigation Compose guidance).
 *
 * Screens with typed args use [Nav] patterns; navigate with helpers (e.g. [Nav.profile]) so IDs stay centralized.
 */
sealed class AppRoute(val route: String) {
    data object Home : AppRoute("home")
    data object Create : AppRoute("create")
    data object Matches : AppRoute("matches")
    data object Chat : AppRoute("chat")
    data object Notifications : AppRoute("notifications")
    data object Safety : AppRoute("safety")
    /** Placeholder flows — swap for Firebase-backed screens later. */
    data object SafetyBlocks : AppRoute("safety_blocks")
    data object SafetyReport : AppRoute("safety_report")

    object Nav {
        const val ProfilePattern = "profile/{matchId}"
        fun profile(matchId: String): String = "profile/$matchId"

        const val JoinRequestPattern = "join_request/{matchId}"
        fun joinRequest(matchId: String): String = "join_request/$matchId"
    }
}
