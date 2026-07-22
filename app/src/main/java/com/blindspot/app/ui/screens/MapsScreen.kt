package com.blindspot.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.blindspot.app.data.model.Place
import com.blindspot.app.data.repository.RouteRepository
import com.blindspot.app.ui.components.PermissionGate
import com.blindspot.app.ui.components.PlaceInfoSheet
import com.blindspot.app.ui.components.aurora.AuroraPlaceBanner
import com.blindspot.app.ui.components.aurora.AuroraSurface
import com.blindspot.app.ui.theme.AuroraTokens
import com.blindspot.app.util.GeoUtils
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.location.LocationPuck
import org.maplibre.compose.location.mostAccurateBearing
import org.maplibre.compose.location.rememberDefaultLocationProvider
import org.maplibre.compose.location.rememberDefaultOrientationProvider
import org.maplibre.compose.location.rememberUserLocationState
import org.maplibre.compose.map.GestureOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import kotlin.math.ln

private const val OPENFREEMAP_STYLE_URL = "https://tiles.openfreemap.org/styles/fiord"
private const val USER_ZOOM = 16.0
private const val MAP_PITCH = 0.0

private suspend fun centerOnUser(cameraState: CameraState, position: Position) {
    cameraState.animateTo(
        CameraPosition(target = position, zoom = USER_ZOOM, tilt = MAP_PITCH),
    )
}

/**
 * Frames both the user and the destination: camera targets the midpoint at a zoom chosen so the
 * full route fits comfortably on a phone viewport.
 */
private suspend fun frameRoute(cameraState: CameraState, user: Position, target: Position) {
    val midpoint = Position(
        longitude = (user.longitude + target.longitude) / 2.0,
        latitude = (user.latitude + target.latitude) / 2.0,
    )
    val meters = GeoUtils.distanceMeters(
        user.latitude, user.longitude,
        target.latitude, target.longitude,
    )
    val zoom = (ln(20_000_000.0 / meters.coerceAtLeast(50.0)) / ln(2.0)).coerceIn(11.0, 17.0)
    cameraState.animateTo(
        CameraPosition(target = midpoint, zoom = zoom, tilt = MAP_PITCH),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsScreen(
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    targetPlace: Place? = null,
    onClearTarget: () -> Unit = {},
) {
    PermissionGate(modifier = modifier) {
        val cameraState = rememberCameraState()
        val scope = rememberCoroutineScope()
        val routeRepository = koinInject<RouteRepository>()
        var hasCenteredOnUser by remember { mutableStateOf(false) }
        var framedTargetId by remember { mutableStateOf<String?>(null) }
        // Decoded route geometry for the current destination; null until it resolves (or when the
        // fetch fails), in which case the map falls back to a straight guide line.
        var routePositions by remember { mutableStateOf<List<Position>?>(null) }
        var sheetVisible by remember { mutableStateOf(false) }
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                if (targetPlace == null) centerOnUser(cameraState, userPosition)
            }
        }

        // When a destination is set (or changes), frame user + destination together — never
        // open on the user with the destination off-screen.
        LaunchedEffect(targetPlace?.id, userPosition != null) {
            val target = targetPlace ?: return@LaunchedEffect
            val user = userPosition ?: return@LaunchedEffect
            if (framedTargetId != target.id) {
                framedTargetId = target.id
                frameRoute(cameraState, user, Position(target.longitude, target.latitude))
            }
        }

        // Fetch and decode the route for the current destination. Keyed on the target id and the
        // first available fix so it runs once per destination (not on every tab switch). On
        // failure routePositions stays null and the map falls back to a straight guide line.
        LaunchedEffect(targetPlace?.id, userPosition != null) {
            routePositions = null
            val target = targetPlace ?: return@LaunchedEffect
            val user = userPosition ?: return@LaunchedEffect
            routePositions = routeRepository.getRoute(
                fromLatitude = user.latitude,
                fromLongitude = user.longitude,
                toLatitude = target.latitude,
                toLongitude = target.longitude,
            ).getOrNull()?.points?.map { Position(it.longitude, it.latitude) }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            MaplibreMap(
                modifier = Modifier.fillMaxSize(),
                baseStyle = BaseStyle.Uri(OPENFREEMAP_STYLE_URL),
                cameraState = cameraState,
                options = MapOptions(
                    ornamentOptions = OrnamentOptions.AllDisabled,
                    gestureOptions = GestureOptions(isTiltEnabled = false),
                ),
            ) {
                if (targetPlace != null) {
                    val targetPosition = Position(targetPlace.longitude, targetPlace.latitude)

                    // Decoded route line from the user to the destination; falls back to a
                    // straight guide line until the route resolves (or if the fetch fails).
                    if (userPosition != null) {
                        val linePositions = routePositions ?: listOf(userPosition, targetPosition)
                        val routeSource = rememberGeoJsonSource(
                            data = GeoJsonData.Features(
                                LineString(linePositions),
                            ),
                        )
                        LineLayer(
                            id = "route-line",
                            source = routeSource,
                            color = const(AuroraTokens.AccentCyan.copy(alpha = 0.8f)),
                            width = const(3.dp),
                        )
                    }

                    // Destination pin: accent dot with a dark ring so it reads on any tile.
                    val destinationSource = rememberGeoJsonSource(
                        data = GeoJsonData.Features(Point(targetPosition)),
                    )
                    CircleLayer(
                        id = "destination-ring",
                        source = destinationSource,
                        color = const(AuroraTokens.BaseDeep),
                        radius = const(11.dp),
                    )
                    CircleLayer(
                        id = "destination-pin",
                        source = destinationSource,
                        color = const(AuroraTokens.AccentCyan),
                        radius = const(7.dp),
                    )
                }

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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 96.dp),
                ) {
                    AuroraSurface(
                        shape = CircleShape,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(end = 20.dp, bottom = 12.dp)
                            .size(48.dp)
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
                                tint = AuroraTokens.AccentCyan,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }

                    if (targetPlace != null) {
                        val liveDistanceLabel = userPosition?.let { user ->
                            GeoUtils.formatDistance(
                                GeoUtils.distanceMeters(
                                    user.latitude, user.longitude,
                                    targetPlace.latitude, targetPlace.longitude,
                                ),
                            )
                        } ?: targetPlace.distanceMeters?.let(GeoUtils::formatDistance).orEmpty()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AuroraPlaceBanner(
                                place = targetPlace,
                                distanceLabel = liveDistanceLabel,
                                onClick = { sheetVisible = true },
                                modifier = Modifier.weight(1f),
                            )
                            AuroraSurface(
                                shape = CircleShape,
                                modifier = Modifier
                                    .padding(start = 12.dp)
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .clickable(onClick = onClearTarget),
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Clear destination",
                                        tint = AuroraTokens.TextSecondary,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (sheetVisible && targetPlace != null) {
            val sheetDistanceLabel = userPosition?.let { user ->
                GeoUtils.formatDistance(
                    GeoUtils.distanceMeters(
                        user.latitude, user.longitude,
                        targetPlace.latitude, targetPlace.longitude,
                    ),
                )
            } ?: targetPlace.distanceMeters?.let(GeoUtils::formatDistance).orEmpty()

            PlaceInfoSheet(
                place = targetPlace,
                distanceLabel = sheetDistanceLabel,
                sheetState = sheetState,
                onDismiss = { sheetVisible = false },
                onBack = { sheetVisible = false },
                showBack = false,
            )
        }
    }
}
