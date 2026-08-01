package com.blindspot.app.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import com.blindspot.app.ui.components.aurora.AuroraFloating
import com.blindspot.app.ui.components.aurora.AuroraPlaceBanner
import com.blindspot.app.ui.components.focusEffect
import com.blindspot.app.ui.components.navItemPress
import com.blindspot.app.ui.theme.AuroraTokens
import com.blindspot.app.util.GeoUtils
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.value.LineCap
import org.maplibre.compose.expressions.value.LineJoin
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.location.LocationPuck
import org.maplibre.compose.location.LocationPuckColors
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

// Dark Matter fork keeps the map legible against the app's dark-only "Midnight Aurora" theme.
private const val OPENFREEMAP_STYLE_URL = "https://tiles.openfreemap.org/styles/dark"
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
        // True while a route fetch is in flight; distinguishes "still loading" from "fetch
        // failed", both of which leave routePositions null, so the guide line only pulses
        // during an active fetch and reads as static once it has settled with a failure.
        var isFetchingRoute by remember { mutableStateOf(false) }
        var sheetVisible by remember { mutableStateOf(false) }
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        // Shared press/focus sources so navItemPress/focusEffect observe the same interactions
        // that clickable emits (each modifier needs the exact same instance).
        val recenterSource = remember { MutableInteractionSource() }
        val clearSource = remember { MutableInteractionSource() }

        // User puck recolored to match the aurora accent (cyan dot, dark ring like the
        // destination pin) instead of the default blue.
        val puckColors = remember {
            LocationPuckColors(
                dotFillColorCurrentLocation = AuroraTokens.AccentCyan,
                dotFillColorOldLocation = AuroraTokens.TextTertiary,
                dotStrokeColor = AuroraTokens.BaseDeep,
                shadowColor = AuroraTokens.ShadowTint,
                accuracyStrokeColor = AuroraTokens.AccentCyan,
                accuracyFillColor = AuroraTokens.AccentCyan.copy(alpha = 0.08f),
                bearingColor = AuroraTokens.AccentCyan,
            )
        }

        // Pulses the straight guide line's opacity while the real route is still loading, so the
        // static line reads as a loading state rather than a finished result.
        val routeLinePulse = rememberInfiniteTransition(label = "routeLinePulse")
        val routeLinePulseAlpha by routeLinePulse.animateFloat(
            initialValue = 0.25f,
            targetValue = 0.8f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 700, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "routeLinePulseAlpha",
        )

        // The Maps tab only exists in the composition while it's the selected destination, so we
        // can subscribe to location/orientation unconditionally; leaving the tab unmounts this and
        // tears down the GPS/tracking work.
        val locationProvider = rememberDefaultLocationProvider()
        val orientationProvider = rememberDefaultOrientationProvider()
        val locationState = rememberUserLocationState(locationProvider, orientationProvider)

        // Read the latest user position during composition so it stays current on every sampled
        // update; both the initial auto-center and the recenter button rely on this value.
        val userPosition: Position? = locationState.location?.position?.value

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
            isFetchingRoute = true
            routePositions = routeRepository.getRoute(
                fromLatitude = user.latitude,
                fromLongitude = user.longitude,
                toLatitude = target.latitude,
                toLongitude = target.longitude,
            ).getOrNull()?.points?.map { Position(it.longitude, it.latitude) }
            isFetchingRoute = false
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
                    // straight guide line that pulses while the route resolves (or if the fetch
                    // fails, in which case it stays static).
                    if (userPosition != null) {
                        val isRouteLoading = isFetchingRoute && routePositions == null
                        val linePositions = routePositions?.takeIf { it.size >= 2 }
                            ?: listOf(userPosition, targetPosition)
                        val routeSource = rememberGeoJsonSource(
                            data = GeoJsonData.Features(
                                LineString(linePositions),
                            ),
                        )
                        val lineAlpha = if (isRouteLoading) routeLinePulseAlpha else 0.8f
                        // Dark casing under the accent line keeps it legible against map tiles.
                        LineLayer(
                            id = "route-line-casing",
                            source = routeSource,
                            color = const(AuroraTokens.BaseDeep.copy(alpha = 0.6f)),
                            width = const(8.dp),
                            cap = const(LineCap.Round),
                            join = const(LineJoin.Round),
                        )
                        LineLayer(
                            id = "route-line",
                            source = routeSource,
                            color = const(AuroraTokens.AccentCyan.copy(alpha = lineAlpha)),
                            width = const(3.dp),
                            cap = const(LineCap.Round),
                            join = const(LineJoin.Round),
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

                LocationPuck(
                    idPrefix = "user",
                    location = locationState.location,
                    bearing = locationState.mostAccurateBearing(),
                    cameraState = cameraState,
                    colors = puckColors,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 96.dp),
            ) {
                AuroraFloating(
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(end = 24.dp, bottom = 12.dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = recenterSource,
                            indication = null,
                            enabled = userPosition != null,
                        ) {
                            userPosition?.let { scope.launch { centerOnUser(cameraState, it) } }
                        }
                        .navItemPress(interactionSource = recenterSource)
                        .focusEffect(interactionSource = recenterSource),
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
                            .padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AuroraPlaceBanner(
                            place = targetPlace,
                            distanceLabel = liveDistanceLabel,
                            onClick = { sheetVisible = true },
                            modifier = Modifier.weight(1f),
                        )
                        AuroraFloating(
                            shape = CircleShape,
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = clearSource,
                                    indication = null,
                                    onClick = onClearTarget,
                                )
                                .navItemPress(interactionSource = clearSource)
                                .focusEffect(interactionSource = clearSource),
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