package com.blindspot.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.blindspot.app.auth.AuthViewModel
import com.blindspot.app.auth.TokenStore
import com.blindspot.app.data.model.Place
import com.blindspot.app.navigation.AuthDestinations
import com.blindspot.app.navigation.Destination
import com.blindspot.app.ui.components.FloatingNavPill
import com.blindspot.app.ui.components.aurora.AuroraBackground
import com.blindspot.app.ui.discovery.PlacesViewModel
import com.blindspot.app.ui.screens.DiscoveryScreen
import com.blindspot.app.ui.screens.FeedScreen
import com.blindspot.app.ui.screens.MapsScreen
import com.blindspot.app.ui.screens.ProfileScreen
import com.blindspot.app.ui.screens.SignInScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import com.blindspot.app.data.repository.FavoritesRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.coroutines.cancellation.CancellationException
import com.blindspot.app.data.repository.ThemeRepository
import com.blindspot.app.ui.theme.AppTheme
import com.blindspot.app.ui.theme.AuroraTokens
import com.blindspot.app.ui.theme.AuroraPalette
import com.blindspot.app.ui.theme.DuskPalette

@Composable
fun BlindspotApp() {
    val navController = rememberNavController()
    val tokenStore: TokenStore = koinInject()
    val isAuthenticated by tokenStore.isAuthenticatedFlow.collectAsStateWithLifecycle()
    val authViewModel: AuthViewModel = koinViewModel()
    val favoritesRepository: FavoritesRepository = koinInject()
    val themeRepository: ThemeRepository = koinInject()
    val currentTheme by themeRepository.theme.collectAsStateWithLifecycle()

    // Applies the persisted theme choice to AuroraTokens on launch and whenever it changes
    // (e.g. toggled in Profile), so every screen reading AuroraTokens.X recomposes with the
    // new palette without needing any changes at those call sites.
    LaunchedEffect(currentTheme) {
        AuroraTokens.setPalette(if (currentTheme == AppTheme.DUSK) DuskPalette else AuroraPalette)
    }

    LaunchedEffect(isAuthenticated) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route

        if (!isAuthenticated) {
            if (currentRoute != null && currentRoute != AuthDestinations.SIGN_IN) {
                navController.navigate(AuthDestinations.SIGN_IN) {
                    popUpTo(AuthDestinations.MAIN) { inclusive = true }
                    launchSingleTop = true
                }
            }
            favoritesRepository.clear()
        } else {
            var attempt = 0
            while (isActive) {
                try {
                    favoritesRepository.refresh()
                    break
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    attempt++
                    delay(minOf(30_000L, 1_000L * (1 shl attempt.coerceAtMost(5))))
                }
            }
        }
    }

    val onSignOut: () -> Unit = {
        authViewModel.signOut()
    }

    // The venue the map should guide the user to; set by "Take me there" from any detail sheet.
    // Hoisted above the NavHost so it survives tab switches even though MapsScreen is unmounted
    // while another tab is on screen.
    var mapTarget by remember { mutableStateOf<Place?>(null) }
    var selectedTab by remember { mutableStateOf(Destination.Discovery) }
    val navigateToPlace: (Place) -> Unit = {
        mapTarget = it
        selectedTab = Destination.Maps
    }

    AuroraBackground(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = if (isAuthenticated) AuthDestinations.MAIN else AuthDestinations.SIGN_IN,
                modifier = Modifier.fillMaxSize(),
            ) {
                composable(AuthDestinations.SIGN_IN) {
                    SignInScreen(
                        onSignedIn = {
                            navController.navigate(AuthDestinations.MAIN) {
                                popUpTo(AuthDestinations.SIGN_IN) { inclusive = true }
                            }
                        },
                    )
                }
                composable(AuthDestinations.MAIN) {
                    MainContent(
                        navController = navController,
                        selected = selectedTab,
                        mapTarget = mapTarget,
                        onClearTarget = { mapTarget = null },
                        onNavigateToMaps = navigateToPlace,
                        onSignOut = onSignOut,
                        onTabSelected = { selectedTab = it },
                    )
                }
            }
        }
    }
}

@Composable
private fun MainContent(
    navController: NavController,
    selected: Destination,
    mapTarget: Place?,
    onClearTarget: () -> Unit,
    onNavigateToMaps: (Place) -> Unit,
    onSignOut: () -> Unit,
    onTabSelected: (Destination) -> Unit,
) {
    val mainEntry: NavBackStackEntry = remember(navController) {
        navController.getBackStackEntry(AuthDestinations.MAIN)
    }
    val sharedPlacesViewModel: PlacesViewModel = koinViewModel(viewModelStoreOwner = mainEntry)

    Box(modifier = Modifier.fillMaxSize()) {
        when (selected) {
            Destination.Maps -> MapsScreen(
                viewModel = sharedPlacesViewModel,
                targetPlace = mapTarget,
                onClearTarget = onClearTarget,
            )
            Destination.Discovery -> DiscoveryScreen(
                onNavigateToMaps = onNavigateToMaps,
                viewModel = sharedPlacesViewModel,
            )
            Destination.Feed -> FeedScreen(
                onNavigateToMaps = onNavigateToMaps,
                viewModel = sharedPlacesViewModel,
            )
            Destination.Profile -> ProfileScreen(onSignOut = onSignOut)
        }

        FloatingNavPill(
            selected = selected,
            onSelect = onTabSelected,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}