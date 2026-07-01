package com.blindspot.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.blindspot.app.ui.components.GlassSurface
import com.blindspot.app.ui.components.PermissionGate
import com.blindspot.app.ui.theme.GeminiBlue
import kotlinx.coroutines.launch
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.location.LocationPuck
import org.maplibre.compose.location.mostAccurateBearing
import org.maplibre.compose.location.rememberDefaultLocationProvider
import org.maplibre.compose.location.rememberDefaultOrientationProvider
import org.maplibre.compose.location.rememberUserLocationState
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Position

private const val DARK_STYLE_ASSET = "asset://dark_map_style.json"
private const val USER_ZOOM = 16.0
private const val MAP_PITCH = 60.0

private suspend fun centerOnUser(cameraState: CameraState, position: Position) {
    cameraState.animateTo(
        CameraPosition(target = position, zoom = USER_ZOOM, tilt = MAP_PITCH),
    )
}

@Composable
fun MapsScreen(
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
) {
    PermissionGate(modifier = modifier) {
        val cameraState = rememberCameraState()
        val scope = rememberCoroutineScope()
        var hasCenteredOnUser by remember { mutableStateOf(false) }

        // Only subscribe to location/orientation while the Maps tab is visible. The MaplibreMap
        // below stays composed (the native instance is never torn down), so switching tabs no
        // longer reloads the map; toggling this just pauses GPS/tracking work.
        val locationState = if (isActive) {
            val locationProvider = rememberDefaultLocationProvider()
            val orientationProvider = rememberDefaultOrientationProvider()
            rememberUserLocationState(locationProvider, orientationProvider)
        } else {
            null
        }

        // Read the latest user position during composition so it stays current on every sampled
        // update; both the initial auto-center and the recenter button rely on this value.
        val userPosition: Position? = locationState?.location?.position?.value

        // Center automatically once, as soon as the first location fix is available.
        LaunchedEffect(userPosition) {
            if (userPosition != null && !hasCenteredOnUser) {
                hasCenteredOnUser = true
                centerOnUser(cameraState, userPosition)
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            MaplibreMap(
                modifier = Modifier.fillMaxSize(),
                baseStyle = BaseStyle.Uri(DARK_STYLE_ASSET),
                cameraState = cameraState,
                options = MapOptions(ornamentOptions = OrnamentOptions.AllDisabled),
            ) {
                if (locationState != null) {
                    LocationPuck(
                        idPrefix = "user",
                        location = locationState.location,
                        bearing = locationState.mostAccurateBearing(),
                        cameraState = cameraState,
                    )
                }
            }

            if (isActive) {
                GlassSurface(
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(24.dp)
                        .size(56.dp)
                        .clip(CircleShape)
                        .clickable(enabled = userPosition != null) {
                            userPosition?.let { scope.launch { centerOnUser(cameraState, it) } }
                        },
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MyLocation,
                            contentDescription = "Recenter map on my location",
                            tint = GeminiBlue,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
        }
    }
}
