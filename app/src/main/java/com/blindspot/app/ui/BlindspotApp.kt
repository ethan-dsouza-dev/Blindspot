package com.blindspot.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.blindspot.app.data.model.Place
import com.blindspot.app.navigation.Destination
import com.blindspot.app.ui.components.FloatingNavPill
import com.blindspot.app.ui.components.aurora.AuroraBackground
import com.blindspot.app.ui.screens.DiscoveryScreen
import com.blindspot.app.ui.screens.FeedScreen
import com.blindspot.app.ui.screens.MapsScreen
import com.blindspot.app.ui.screens.ProfileScreen

@Composable
fun BlindspotApp() {
    val navController = rememberNavController()

    // The venue the map should guide the user to; set by "Take me there" from any detail sheet.
    // Hoisted above the NavHost so it survives tab switches even though MapsScreen is unmounted
    // while another tab is on screen.
    var mapTarget by remember { mutableStateOf<Place?>(null) }
    val navigateToPlace: (Place) -> Unit = remember(navController) { { place ->
        mapTarget = place
        navController.navigateToTab(Destination.Maps)
    } }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val selected = Destination.entries.firstOrNull { destination ->
        currentDestination?.hierarchy?.any { it.route == destination.route } == true
    } ?: Destination.Discovery

    AuroraBackground(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Only the selected tab is composed, so taps can't bleed through to a hidden screen.
            NavHost(
                navController = navController,
                startDestination = Destination.Discovery.route,
                modifier = Modifier.fillMaxSize(),
            ) {
                composable(Destination.Maps.route) {
                    MapsScreen(
                        targetPlace = mapTarget,
                        onClearTarget = { mapTarget = null },
                    )
                }
                composable(Destination.Discovery.route) {
                    DiscoveryScreen(onNavigateToMaps = navigateToPlace)
                }
                composable(Destination.Feed.route) {
                    FeedScreen(onNavigateToMaps = navigateToPlace)
                }
                composable(Destination.Profile.route) {
                    ProfileScreen()
                }
            }

            // Floating navigation pill overlaid at the bottom
            FloatingNavPill(
                selected = selected,
                onSelect = navController::navigateToTab,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

/**
 * Standard bottom-navigation switch: single top-level entry per tab, saving and restoring each
 * tab's state so switching back and forth doesn't stack duplicate destinations.
 */
private fun NavHostController.navigateToTab(destination: Destination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
