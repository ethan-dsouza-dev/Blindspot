package com.blindspot.app.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.blindspot.app.ui.components.PermissionGate
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.location.LocationPuck
import org.maplibre.compose.location.LocationTrackingEffect
import org.maplibre.compose.location.mostAccurateBearing
import org.maplibre.compose.location.rememberDefaultLocationProvider
import org.maplibre.compose.location.rememberDefaultOrientationProvider
import org.maplibre.compose.location.rememberUserLocationState
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.style.BaseStyle

private const val OPENFREEMAP_STYLE_URL = "https://tiles.openfreemap.org/styles/fiord"

@Composable
fun MapsScreen(modifier: Modifier = Modifier) {
    PermissionGate(modifier = modifier) {
        val cameraState = rememberCameraState()
        val locationProvider = rememberDefaultLocationProvider()
        val orientationProvider = rememberDefaultOrientationProvider()
        val locationState = rememberUserLocationState(locationProvider, orientationProvider)
        var hasCenteredOnUser by remember { mutableStateOf(false) }

        MaplibreMap(
            modifier = Modifier.fillMaxSize(),
            baseStyle = BaseStyle.Uri(OPENFREEMAP_STYLE_URL),
            cameraState = cameraState,
            options = MapOptions(ornamentOptions = OrnamentOptions.AllDisabled),
        ) {
            LocationPuck(
                idPrefix = "user",
                location = locationState.location,
                bearing = locationState.mostAccurateBearing(),
                cameraState = cameraState,
            )

            LocationTrackingEffect(locationState = locationState) {
                if (!hasCenteredOnUser) {
                    val position = currentLocation.location?.position?.value
                    if (position != null) {
                        hasCenteredOnUser = true
                        cameraState.animateTo(CameraPosition(target = position, zoom = 15.0))
                    }
                }
            }
        }
    }
}
