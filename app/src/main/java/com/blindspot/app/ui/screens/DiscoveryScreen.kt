package com.blindspot.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.blindspot.app.ui.components.CompassView
import com.blindspot.app.ui.components.PermissionGate
import com.blindspot.app.ui.components.PlaceBanner
import com.blindspot.app.ui.components.PlaceInfoSheet
import com.blindspot.app.ui.discovery.DiscoveryUiState
import com.blindspot.app.ui.discovery.DiscoveryViewModel
import com.blindspot.app.ui.theme.GeminiBlue
import org.koin.androidx.compose.koinViewModel

/**
 * Discovery landing screen: a permission-gated compass that points toward the nearest place,
 * with a tappable banner that opens a detail sheet (with skip-to-next).
 *
 * The Gemini-style gradient background is provided by the hosting scaffold (BlindspotApp).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryScreen(
    modifier: Modifier = Modifier,
    viewModel: DiscoveryViewModel = koinViewModel(),
) {
    PermissionGate(
        modifier = modifier,
        onReady = { viewModel.start() },
    ) {
        LaunchedEffect(Unit) { viewModel.start() }

        val state by viewModel.uiState.collectAsStateWithLifecycle()
        var sheetVisible by remember { mutableStateOf(false) }
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        DiscoveryContent(
            state = state,
            onBannerClick = { sheetVisible = true },
            onRetry = viewModel::retry,
            modifier = Modifier.fillMaxSize(),
        )

        val place = state.currentPlace
        if (sheetVisible && place != null) {
            PlaceInfoSheet(
                place = place,
                distanceLabel = state.distanceLabel,
                sheetState = sheetState,
                onDismiss = { sheetVisible = false },
                onSkip = { viewModel.skipToNext() },
                onBack = { viewModel.skipToPrevious() },
                showBack = state.hasPrevious,
            )
        }
    }
}

@Composable
private fun DiscoveryContent(
    state: DiscoveryUiState,
    onBannerClick: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .statusBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Discover",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            text = "Pointing you to the nearest spot",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 4.dp),
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            when (state.status) {
                DiscoveryUiState.Status.Loading -> CircularProgressIndicator(color = GeminiBlue)
                DiscoveryUiState.Status.Empty -> CenterMessage(
                    title = "No places nearby",
                    body = "We couldn't find anywhere to point to right now.",
                )
                DiscoveryUiState.Status.Error -> CenterMessage(
                    title = "Something went wrong",
                    body = state.errorMessage ?: "Please try again.",
                    actionLabel = "Retry",
                    onAction = onRetry,
                )
                DiscoveryUiState.Status.Content -> CompassView(
                    rotationDegrees = state.needleRotation,
                    distanceLabel = state.distanceLabel.ifEmpty { null },
                    targetLabel = state.currentPlace?.name,
                )
            }
        }

        state.currentPlace?.let { place ->
            if (state.status == DiscoveryUiState.Status.Content) {
                PlaceBanner(
                    place = place,
                    distanceLabel = state.distanceLabel,
                    onClick = onBannerClick,
                    modifier = Modifier.padding(bottom = 24.dp),
                )
            }
        }
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
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(actionLabel, color = GeminiBlue, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
