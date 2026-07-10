package com.blindspot.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import com.blindspot.app.data.model.Place
import com.blindspot.app.navigation.Destination
import com.blindspot.app.ui.components.FloatingNavPill
import com.blindspot.app.ui.components.aurora.AuroraBackground
import com.blindspot.app.ui.screens.DiscoveryScreen
import com.blindspot.app.ui.screens.FeedScreen
import com.blindspot.app.ui.screens.MapsScreen

@Composable
fun BlindspotApp() {
    var selected by rememberSaveable { mutableStateOf(Destination.Discovery) }

    // The venue the map should guide the user to; set by "Take me there" from any detail sheet.
    var mapTarget by remember { mutableStateOf<Place?>(null) }
    val navigateToPlace: (Place) -> Unit = {
        mapTarget = it
        selected = Destination.Maps
    }

    // Each screen is wrapped in movable content so its composition (and, for Maps, the native
    // MapLibre view) is preserved when we reorder the screens to keep the active one on top.
    val mapsContent = remember {
        movableContentOf<Boolean> { active ->
            MapsScreen(isActive = active, targetPlace = mapTarget, onClearTarget = { mapTarget = null })
        }
    }
    val discoveryContent = remember { movableContentOf { DiscoveryScreen(onNavigateToMaps = navigateToPlace) } }
    val feedContent = remember { movableContentOf { FeedScreen(onNavigateToMaps = navigateToPlace) } }

    AuroraBackground(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Keep all screens composed so nothing (e.g. the map) reloads on tab switch. Render
            // them stacked, ordering the active screen last so it draws on top and receives input;
            // inactive screens stay alive but invisible.
            Box(modifier = Modifier.fillMaxSize()) {
                Destination.entries
                    .sortedBy { it == selected }
                    .forEach { destination ->
                        val isActive = destination == selected
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
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

            // Floating navigation pill overlaid at the bottom
            FloatingNavPill(
                selected = selected,
                onSelect = { selected = it },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
