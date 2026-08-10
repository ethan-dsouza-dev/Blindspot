package com.blindspot.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.blindspot.app.data.model.Place
import com.blindspot.app.ui.components.CompassView
import com.blindspot.app.ui.components.PageDots
import com.blindspot.app.ui.components.PermissionGate
import com.blindspot.app.ui.components.PlaceInfoSheet
import com.blindspot.app.ui.components.PriceFilterDropdown
import com.blindspot.app.ui.components.RadiusSlider
import com.blindspot.app.ui.components.aurora.AuroraPlaceBanner
import com.blindspot.app.ui.discovery.DiscoveryUiState
import com.blindspot.app.ui.discovery.PlacesViewModel
import com.blindspot.app.ui.theme.AuroraTokens
import org.koin.androidx.compose.koinViewModel
import kotlin.math.min
import androidx.compose.runtime.rememberCoroutineScope
import com.blindspot.app.data.repository.FavoritesRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/** Radius presets used by the empty-state "Widen search" action. */
private val WIDEN_PRESETS = listOf(500, 1_000, 2_000, 5_000)

/** Next larger radius preset, or the largest preset if already there. */
private fun nextWiderRadius(current: Int): Int =
    WIDEN_PRESETS.firstOrNull { it > current } ?: WIDEN_PRESETS.last()

/** Horizontal drag distance (density-scaled) that counts as a swipe to the next/previous place. */
private val SWIPE_MIN_DISTANCE_DP = 90.dp

/** Horizontal fling velocity (px/s) that counts as a skip even if the drag was short. */
private const val SWIPE_MIN_VELOCITY_PX_S = 500f

/**
 * Turns a horizontal drag/fling on the attached node into a skip to the previous or next place.
 * The content itself stays in place (there is exactly one compass, distance label, and banner);
 * the ViewModel index change drives their crossfade/spring update. [hasPrevious]/[hasNext] gate
 * the direction so the ends of the list simply do nothing. A skip fires on a drag past
 * [SWIPE_MIN_DISTANCE_DP] or on a fling faster than [SWIPE_MIN_VELOCITY_PX_S].
 */
@Composable
private fun Modifier.swipeToChangePlace(
    hasPrevious: Boolean,
    hasNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
): Modifier {
    val minDistancePx = with(LocalDensity.current) { SWIPE_MIN_DISTANCE_DP.toPx() }
    return pointerInput(hasPrevious, hasNext) {
        var totalDrag = 0f
        var velocity = 0f
        var lastX = 0f
        var lastTime = 0L

        detectHorizontalDragGestures(
            onDragStart = { start ->
                totalDrag = 0f
                velocity = 0f
                lastX = start.x
                lastTime = 0L
            },
            onDragEnd = {
                when {
                    (totalDrag <= -minDistancePx || velocity <= -SWIPE_MIN_VELOCITY_PX_S) && hasNext ->
                        onNext()
                    (totalDrag >= minDistancePx || velocity >= SWIPE_MIN_VELOCITY_PX_S) && hasPrevious ->
                        onPrevious()
                }
            },
            onDragCancel = { totalDrag = 0f },
        ) { change, dragAmount ->
            totalDrag += dragAmount
            val now = change.uptimeMillis
            if (lastTime != 0L && now > lastTime) {
                velocity = (change.position.x - lastX) / (now - lastTime) * 1000f
            }
            lastX = change.position.x
            lastTime = now
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryScreen(
    onNavigateToMaps: (Place) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlacesViewModel = koinViewModel(),
) {
    PermissionGate(
        modifier = modifier,
        onReady = { viewModel.start() },
    ) {
        LaunchedEffect(Unit) { viewModel.start() }

        val state by viewModel.uiState.collectAsStateWithLifecycle()
        var sheetVisible by remember { mutableStateOf(false) }
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val haptic = LocalHapticFeedback.current

        val favoritesRepository: FavoritesRepository = koinInject()
        val favoriteIds by favoritesRepository.favoritePlaceIds.collectAsStateWithLifecycle()
        val coroutineScope = rememberCoroutineScope()

        DiscoveryContent(
            state = state,
            onBannerClick = { sheetVisible = true },
            onRetry = viewModel::retry,
            onRadiusChange = viewModel::setRadius,
            onPriceChange = viewModel::setPriceLevel,
            onRefresh = viewModel::refresh,
            onCompassLock = { haptic.performHapticFeedback(HapticFeedbackType.VirtualKey) },
            onWidenSearch = { viewModel.setRadius(nextWiderRadius(state.radiusMeters)) },
            onSkipNext = viewModel::skipToNext,
            onSkipPrevious = viewModel::skipToPrevious,
            modifier = Modifier.fillMaxSize(),
        )

        val place = state.currentPlace
        if (sheetVisible && place != null) {
            PlaceInfoSheet(
                place = place,
                distanceLabel = state.distanceLabel,
                sheetState = sheetState,
                isFavorite = place.id in favoriteIds,
                onToggleFavorite = {
                    coroutineScope.launch { favoritesRepository.toggleFavorite(place.id) }
                },
                onDismiss = { sheetVisible = false },
                onSkip = { viewModel.skipToNext() },
                onBack = { viewModel.skipToPrevious() },
                showBack = state.hasPrevious,
                onViewOnMap = { onNavigateToMaps(place) },
            )
        }
    }
}

@Composable
private fun DiscoveryContent(
    state: DiscoveryUiState,
    onBannerClick: () -> Unit,
    onRetry: () -> Unit,
    onRadiusChange: (Int) -> Unit,
    onPriceChange: (Int?) -> Unit,
    onRefresh: () -> Unit,
    onCompassLock: () -> Unit,
    onWidenSearch: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
            .navigationBarsPadding()
            .padding(bottom = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Discover",
                    style = MaterialTheme.typography.headlineLarge,
                    color = AuroraTokens.TextPrimary,
                )
                Text(
                    text = "Pointing you to the nearest spot",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AuroraTokens.TextSecondary,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            if (state.status == DiscoveryUiState.Status.Content ||
                state.status == DiscoveryUiState.Status.Empty
            ) {
                IconButton(
                    onClick = onRefresh,
                    enabled = !state.isRefreshing,
                ) {
                    if (state.isRefreshing) {
                        CircularProgressIndicator(
                            color = AuroraTokens.AccentCyan,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp),
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh results",
                            tint = AuroraTokens.TextPrimary,
                        )
                    }
                }
            }
        }

        when (state.status) {
            DiscoveryUiState.Status.Loading -> Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = AuroraTokens.AccentCyan)
            }
            DiscoveryUiState.Status.Empty -> Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                EmptyDiscoveryState(onWidenSearch = onWidenSearch)
            }
            DiscoveryUiState.Status.Error -> Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                CenterMessage(
                    title = "Something went wrong",
                    body = state.errorMessage ?: "Please try again.",
                    actionLabel = "Retry",
                    onAction = onRetry,
                )
            }
            DiscoveryUiState.Status.Content -> Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .swipeToChangePlace(
                            hasPrevious = state.hasPrevious,
                            hasNext = state.hasNext,
                            onPrevious = onSkipPrevious,
                            onNext = onSkipNext,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CompassView(
                            rotationDegrees = state.needleRotation,
                            onLockOn = onCompassLock,
                            size = 240.dp,
                        )
                        // Distance label crossfades + rises whenever the target place changes
                        // (walking updates the text in place without re-animating).
                        AnimatedContent(
                            targetState = state.currentPlace?.id,
                            transitionSpec = {
                                (fadeIn(tween(durationMillis = 350)) +
                                    slideInVertically(tween(durationMillis = 350)) { it / 3 })
                                    .togetherWith(fadeOut(tween(durationMillis = 150)))
                            },
                            label = "distanceReveal",
                        ) { placeId ->
                            if (placeId != null && state.distanceLabel.isNotEmpty()) {
                                Text(
                                    text = state.distanceLabel,
                                    style = MaterialTheme.typography.displayMedium,
                                    color = AuroraTokens.TextPrimary,
                                    modifier = Modifier.padding(top = 24.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        if (state.status == DiscoveryUiState.Status.Content ||
            state.status == DiscoveryUiState.Status.Empty
        ) {
            PriceFilterDropdown(
                priceLevel = state.priceLevel,
                onPriceChange = onPriceChange,
                modifier = Modifier.padding(top = 8.dp),
            )
            RadiusSlider(
                radiusMeters = state.radiusMeters,
                onRadiusChange = onRadiusChange,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        // Position indicator at the bottom, above the banner: how far into the place list we are.
        if (state.status == DiscoveryUiState.Status.Content && state.places.size > 1) {
            PageDots(
                pageCount = state.places.size,
                currentPage = state.currentIndex,
                modifier = Modifier.padding(top = 12.dp, bottom = 12.dp),
            )
        }

        // Banner + skip action reveal together with a spring rise on each new place.
        if (state.status == DiscoveryUiState.Status.Content) {
            AnimatedContent(
                targetState = state.currentPlace,
                transitionSpec = {
                    (fadeIn(tween(durationMillis = 300, delayMillis = 80)) +
                        slideInVertically(
                            spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow,
                            ),
                        ) { it })
                        .togetherWith(fadeOut(tween(durationMillis = 150)))
                },
                label = "placeBannerReveal",
            ) { place ->
                if (place != null) {
                    AuroraPlaceBanner(
                        place = place,
                        distanceLabel = state.distanceLabel,
                        onClick = onBannerClick,
                    )
                }
            }
        }
    }
}

/** Branded empty state: a quiet compass dial (no needle — nothing to point at) + widen CTA. */
@Composable
private fun EmptyDiscoveryState(
    onWidenSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EmptyCompassIllustration()
        Text(
            text = "No spots in range",
            style = MaterialTheme.typography.titleLarge,
            color = AuroraTokens.TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 24.dp),
        )
        Text(
            text = "Nothing to point at right now.\nTry widening your search radius.",
            style = MaterialTheme.typography.bodyMedium,
            color = AuroraTokens.TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
        Button(
            onClick = onWidenSearch,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AuroraTokens.AccentCyan,
                contentColor = AuroraTokens.OnAccent,
            ),
            modifier = Modifier
                .padding(top = 24.dp)
                .height(48.dp),
        ) {
            Text(
                text = "Widen search",
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

/** Simplified compass dial with tick marks but no needle — reads as "no target found". */
@Composable
private fun EmptyCompassIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(120.dp)) {
        val radius = min(size.width, size.height) / 2f
        val center = this.center

        drawCircle(color = AuroraTokens.CompassDialFill, radius = radius, center = center)
        drawCircle(
            color = AuroraTokens.CompassDialStroke,
            radius = radius,
            center = center,
            style = Stroke(width = 2f),
        )
        drawCircle(
            color = AuroraTokens.CompassDialInnerStroke,
            radius = radius * 0.72f,
            center = center,
            style = Stroke(width = 1f),
        )

        val tickCount = 12
        for (i in 0 until tickCount) {
            val angle = Math.toRadians((i * 360.0 / tickCount))
            val isMajor = i % 3 == 0
            val outer = radius
            val inner = radius - if (isMajor) 16f else 9f
            val startX = center.x + (outer * Math.sin(angle)).toFloat()
            val startY = center.y - (outer * Math.cos(angle)).toFloat()
            val endX = center.x + (inner * Math.sin(angle)).toFloat()
            val endY = center.y - (inner * Math.cos(angle)).toFloat()
            drawLine(
                color = if (isMajor) {
                    AuroraTokens.CompassTickMajor.copy(alpha = 0.5f)
                } else {
                    AuroraTokens.CompassTickMinor.copy(alpha = 0.5f)
                },
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = if (isMajor) 2.5f else 1.5f,
            )
        }

        // Muted center dot where the needle hub would be.
        drawCircle(
            color = AuroraTokens.TextTertiary,
            radius = radius * 0.05f,
            center = center,
        )
    }
}

@Composable
private fun CenterMessage(
    title: String,
    body: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = AuroraTokens.TextPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = AuroraTokens.TextSecondary,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(actionLabel, color = AuroraTokens.AccentCyan, fontWeight = FontWeight.Bold)
            }
        }
    }
}
