package com.blindspot.app.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.blindspot.app.ui.theme.GeminiBlue
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority

/**
 * Wraps [content], only rendering it once location permission is granted *and* device location
 * services are turned on. Otherwise shows the relevant rationale: a button that requests
 * fine/coarse permission, or one that opens the inline Play Services dialog to turn location on.
 *
 * Both states are re-checked on resume so the gate clears automatically when the user returns
 * from system settings or flips the location toggle elsewhere.
 */
@Composable
fun PermissionGate(
    modifier: Modifier = Modifier,
    onReady: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(context.hasLocationPermission()) }
    var locationEnabled by remember { mutableStateOf(context.isLocationServicesEnabled()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        hasPermission = results.values.any { it }
    }

    val resolutionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) {
        locationEnabled = context.isLocationServicesEnabled()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPermission = context.hasLocationPermission()
                locationEnabled = context.isLocationServicesEnabled()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val ready = hasPermission && locationEnabled
    LaunchedEffect(ready) { if (ready) onReady() }

    when {
        !hasPermission -> LocationRationale(
            modifier = modifier,
            icon = Icons.Filled.LocationOn,
            title = "Location needed",
            body = "Blindspot uses your location to point the compass toward nearby places.",
            buttonLabel = "Enable location",
            onRequest = {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    ),
                )
            },
        )

        !locationEnabled -> LocationRationale(
            modifier = modifier,
            icon = Icons.Filled.LocationOff,
            title = "Location is turned off",
            body = "Turn on location services so Blindspot can point the compass toward nearby places.",
            buttonLabel = "Turn on location",
            onRequest = {
                context.requestLocationServices(
                    onEnabled = { locationEnabled = true },
                    onResolutionRequired = { resolution ->
                        resolutionLauncher.launch(
                            IntentSenderRequest.Builder(resolution).build(),
                        )
                    },
                )
            },
        )

        else -> content()
    }
}

@Composable
private fun LocationRationale(
    icon: ImageVector,
    title: String,
    body: String,
    buttonLabel: String,
    onRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GeminiBlue,
            modifier = Modifier.size(64.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(top = 20.dp),
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 28.dp),
        )
        Button(
            onClick = onRequest,
            colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue),
        ) {
            Text(buttonLabel, fontWeight = FontWeight.SemiBold)
        }
    }
}

fun Context.hasLocationPermission(): Boolean {
    val fine = androidx.core.content.ContextCompat.checkSelfPermission(
        this, Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED
    val coarse = androidx.core.content.ContextCompat.checkSelfPermission(
        this, Manifest.permission.ACCESS_COARSE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED
    return fine || coarse
}

fun Context.isLocationServicesEnabled(): Boolean {
    val manager = getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
    return LocationManagerCompat.isLocationEnabled(manager)
}

/**
 * Checks device location settings via Play Services. If already satisfied, [onEnabled] runs;
 * otherwise [onResolutionRequired] is invoked with the resolution so the caller can launch the
 * inline "turn on location" dialog without leaving the app.
 */
private fun Context.requestLocationServices(
    onEnabled: () -> Unit,
    onResolutionRequired: (android.content.IntentSender) -> Unit,
) {
    val request = LocationSettingsRequest.Builder()
        .addLocationRequest(
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5_000L).build(),
        )
        .build()

    LocationServices.getSettingsClient(this)
        .checkLocationSettings(request)
        .addOnSuccessListener { onEnabled() }
        .addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                onResolutionRequired(exception.resolution.intentSender)
            }
        }
}
