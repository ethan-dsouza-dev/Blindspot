package com.blindspot.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blindspot.app.data.model.Place
import com.blindspot.app.ui.components.NearbyPlaceRow
import com.blindspot.app.ui.components.PlaceInfoSheet
import com.blindspot.app.ui.components.aurora.AuroraSurface
import com.blindspot.app.ui.feed.TrendingPlaceItem
import com.blindspot.app.ui.profile.ProfileUiState
import com.blindspot.app.ui.profile.ProfileViewModel
import com.blindspot.app.ui.theme.AuroraTokens
import org.koin.androidx.compose.koinViewModel

private const val SAVED_PLACE_LABEL = "Saved"

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    ProfileScreenContent(
        uiState = uiState,
        onToggleUnits = viewModel::onToggleUnits,
        onToggleNotifications = viewModel::onToggleNotifications,
        onSignOut = viewModel::onSignOut,
        onRetry = viewModel::loadProfile,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreenContent(
    uiState: ProfileUiState,
    onToggleUnits: (Boolean) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onSignOut: () -> Unit,
    onRetry: () -> Unit,
) {
    var selectedPlace by remember { mutableStateOf<Place?>(null) }
    val sheetState = rememberModalBottomSheetState()

    if (uiState.isLoading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator(color = AuroraTokens.AccentCyan)
        }
        return
    }

    if (uiState.error != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = uiState.error,
                color = AuroraTokens.TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 16.dp),
            )
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = "Profile",
                fontSize = 28.sp,
                color = AuroraTokens.TextPrimary,
                modifier = Modifier.padding(top = 24.dp),
            )
        }

        item {
            AuroraSurface {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape),
                        tint = AuroraTokens.AccentCyan,
                    )
                    Column {
                        Text(
                            text = uiState.name,
                            fontSize = 18.sp,
                            color = AuroraTokens.TextPrimary,
                        )
                        Text(
                            text = uiState.email,
                            fontSize = 13.sp,
                            color = AuroraTokens.TextSecondary,
                        )
                    }
                }
            }
        }

        item {
            AuroraSurface {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingRow(
                        label = "Distance in kilometers",
                        checked = uiState.unitsInKilometers,
                        onCheckedChange = onToggleUnits,
                    )
                    SettingRow(
                        label = "Notifications",
                        checked = uiState.notificationsEnabled,
                        onCheckedChange = onToggleNotifications,
                    )
                }
            }
        }

        if (uiState.savedPlaces.isNotEmpty()) {
            item {
                Text(
                    text = "Saved places",
                    fontSize = 16.sp,
                    color = AuroraTokens.TextPrimary,
                )
            }
            items(uiState.savedPlaces) { place ->
                NearbyPlaceRow(
                    item = TrendingPlaceItem(place = place, distanceLabel = SAVED_PLACE_LABEL),
                    onClick = { selectedPlace = place },
                )
            }
        }
    }

    selectedPlace?.let { place ->
        PlaceInfoSheet(
            place = place,
            distanceLabel = SAVED_PLACE_LABEL,
            sheetState = sheetState,
            onDismiss = { selectedPlace = null },
            onBack = { selectedPlace = null },
            showBack = false,
        )
    }
}

@Composable
private fun SettingRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, color = AuroraTokens.TextSecondary, fontSize = 14.sp)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}