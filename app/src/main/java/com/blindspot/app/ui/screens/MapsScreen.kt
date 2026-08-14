package com.blindspot.app.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.blindspot.app.data.model.Place
import com.blindspot.app.data.repository.RouteRepository
import com.blindspot.app.ui.components.PermissionGate
import com.blindspot.app.ui.components.PlaceInfoSheet
import com.blindspot.app.ui.components.aurora.AuroraPlaceBanner
import com.blindspot.app.ui.components.map.MapFab
import com.blindspot.app.ui.components.map.TrendingPinsLayer
import com.blindspot.app.ui.discovery.PlacesViewModel
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
import com.blindspot.app.data.repository.FavoritesRepository
import com.blindspot.app.data.repository.FavoritesNotReadyException


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
    viewModel: PlacesViewModel,
    targetPlace: Place? = null,
    onClearTarget: () -> Unit = {},
) {
    PermissionGate(modifier = modifier) {
        val cameraState = rememberCameraState()
        val scope = rememberCoroutineScope()
        val routeRepository = koinInject<RouteRepository>()
        val favoritesRepository = koinInject<FavoritesRepository>()
        val favoriteIds by favoritesRepository.favoritePlaceIds.collectAsStateWithLifecycle()
        var hasCenteredOnUser by remember { mutableStateOf(false) }
        var framedTargetId by remember { mutableStateOf<String?>(null) }
        // Decoded route geometry for the current destination; null until it resolves (or when the
        // fetch fails), in which case the map falls back to a straight guideline.
        var routePositions by remember { mutableStateOf<List<Position>?>(null) }
        // True while a route fetch is in flight; distinguishes "still loading" from "fetch
        // failed", both of which leave routePositions null, so the guideline only pulses
        // during an active fetch and reads as static once it has settled with a failure.
        var isFetchingRoute by remember { mutableStateOf(false) }
        // The trending place currently shown in the detail sheet; null when closed. Serves both
        // the destination banner (tap sets it to targetPlace) and trending pins.
        var sheetPlace by remember { mutableStateOf<Place?>(null) }
        // Whether the trending-now pins are visible on the map.
        var showTrendingPins by remember { mutableStateOf(false) }
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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

        // Pulses the straight guideline's opacity while the real route is still loading, so the
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

        // Trending-now places shared with the Feed tab; loaded once by the ViewModel on the
        // first location fix. Empty while loading or on failure.
        val trendingPlaces by viewModel.trendingPlaces.collectAsStateWithLifecycle()

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
        // failure routePositions stays null and the map falls back to a straightguide line.
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
                   ornamentOptions = OrnamentOptions(
                        isLogoEnabled = false,
                        isAttributionEnabled = true,
                        attributionAlignment = Alignment.BottomStart,
                        isCompassEnabled = false,
                        isScaleBarEnabled = false,
                    ),
                    gestureOptions = GestureOptions(isTiltEnabled = false),
                ),
            ) {
                // Trending pins: smaller cyan dots with dark rings, drawn below the destination
                // pin so the destination stays the visual focal point. Tapping a pin opens its
                // detail sheet.
                if (showTrendingPins && trendingPlaces.isNotEmpty()) {
                    TrendingPinsLayer(
                        places = trendingPlaces,
                        onPinClick = { sheetPlace = it },
                    )
                }

                if (targetPlace != null) {
                    val targetPosition = Position(targetPlace.longitude, targetPlace.latitude)

                    // Decoded route line from the user to the destination; falls back to a
                    // straight guideline that pulses while the route resolves (or if the fetch
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
                MapFab(
                    icon = Icons.Filled.LocationOn,
                    contentDescription = if (showTrendingPins) {
                        "Hide trending places"
                    } else {
                        "Show trending places"
                    },
                    onClick = { showTrendingPins = !showTrendingPins },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(end = 24.dp, bottom = 12.dp),
                    enabled = userPosition != null,
                    active = showTrendingPins,
                    tint = AuroraTokens.TextTertiary,
                )

                MapFab(
                    icon = Icons.Filled.MyLocation,
                    contentDescription = "Recenter map on my location",
                    onClick = { userPosition?.let { scope.launch { centerOnUser(cameraState, it) } } },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(end = 24.dp, bottom = 12.dp),
                    enabled = userPosition != null,
                    tint = AuroraTokens.AccentCyan,
                )

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
                            onClick = { sheetPlace = targetPlace },
                            modifier = Modifier.weight(1f),
                        )
                        MapFab(
                            icon = Icons.Filled.Close,
                            contentDescription = "Clear destination",
                            onClick = onClearTarget,
                            modifier = Modifier.padding(start = 12.dp),
                            tint = AuroraTokens.TextSecondary,
                            iconSize = 20.dp,
                        )
                    }
                }
            }
        }

        val sheetPlaceValue = sheetPlace
        if (sheetPlaceValue != null) {
            val sheetDistanceLabel = userPosition?.let { user ->
                GeoUtils.formatDistance(
                    GeoUtils.distanceMeters(
                        user.latitude, user.longitude,
                        sheetPlaceValue.latitude, sheetPlaceValue.longitude,
                    ),
                )
            } ?: sheetPlaceValue.distanceMeters?.let(GeoUtils::formatDistance).orEmpty()

            PlaceInfoSheet(
                place = sheetPlaceValue,
                distanceLabel = sheetDistanceLabel,
                sheetState = sheetState,
                isFavorite = sheetPlaceValue.id in favoriteIds,
                onToggleFavorite = {
                    scope.launch {
                        try {
                            favoritesRepository.toggleFavorite(sheetPlaceValue.id)
                        } catch (e: FavoritesNotReadyException) {
                            // Favorites haven't loaded yet — refuse rather than guess.
                        } catch (e: Exception) {
                            // Toggle failed (network, server error, expired token, etc.) — the heart's
                            // optimistic state was already rolled back inside FavoritesRepository.
                            // Nothing further to do; just don't crash.
                        }
                    }
                },
                onDismiss = { sheetPlace = null },
                onBack = { sheetPlace = null },
                showBack = false,
            )
        }
    }
}