package com.blindspot.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.blindspot.app.navigation.Destination
import com.blindspot.app.ui.components.GradientBackground
import com.blindspot.app.ui.screens.DiscoveryScreen
import com.blindspot.app.ui.screens.FeedScreen
import com.blindspot.app.ui.screens.MapsScreen
import com.blindspot.app.ui.theme.GeminiBlue

@Composable
fun BlindspotApp() {
    var selected by rememberSaveable { mutableStateOf(Destination.Discovery) }

    // Each screen is wrapped in movable content so its composition (and, for Maps, the native
    // MapLibre view) is preserved when we reorder the screens to keep the active one on top.
    val mapsContent = remember { movableContentOf<Boolean> { active -> MapsScreen(isActive = active) } }
    val discoveryContent = remember { movableContentOf { DiscoveryScreen() } }
    val feedContent = remember { movableContentOf { FeedScreen(onNavigateToMaps = { selected = Destination.Maps }) } }

    GradientBackground(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White.copy(alpha = 0.06f),
                    tonalElevation = 0.dp,
                ) {
                    Destination.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = selected == destination,
                            onClick = { selected = destination },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.label,
                                )
                            },
                            label = { Text(destination.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = GeminiBlue,
                                selectedTextColor = GeminiBlue,
                                indicatorColor = Color.White.copy(alpha = 0.10f),
                                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                unselectedTextColor = Color.White.copy(alpha = 0.6f),
                            ),
                        )
                    }
                }
            },
        ) { innerPadding ->
            // Keep all screens composed so nothing (e.g. the map) reloads on tab switch. Render
            // them stacked, ordering the active screen last so it draws on top and receives input;
            // inactive screens stay alive but invisible.
            Box(modifier = Modifier.fillMaxSize()) {
                Destination.entries
                    .sortedBy { it == selected }
                    .forEach { destination ->
                        val isActive = destination == selected
                        // Maps draws edge-to-edge behind the status bar; other screens
                        // respect the full Scaffold insets.
                        val screenPadding = if (destination == Destination.Maps) {
                            PaddingValues(bottom = innerPadding.calculateBottomPadding())
                        } else {
                            innerPadding
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(screenPadding)
                                .then(if (isActive) Modifier else Modifier.alpha(0f)),
                        ) {
                            when (destination) {
                                Destination.Maps -> mapsContent(isActive)
                                Destination.Discovery -> discoveryContent()
                                Destination.Feed -> feedContent()
                            }
                        }
                    }
            }
        }
    }
}
