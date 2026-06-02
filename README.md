# Blindspot

Blindspot is an Android app that points you toward nearby places like a compass. Instead of
staring at a map, you hold up your phone and a needle spins to point at the closest spot — a bar,
café, or whatever category you're exploring. Tap the banner for details, or skip to the next place.

## Features

- **Live compass** — a needle that points to the nearest place, combining your GPS location, the
  device's magnetic heading, and the target's coordinates.
- **Distance + target readout** — real-time distance to the current place, shown in the bottom
  half of the dial.
- **Place details** — a frosted-glass bottom sheet with rating, price level, and description.
- **Skip to next** — cycle the compass to the next nearby place.
- **Gemini-style UI** — animated gradient background and glassmorphism components, dark by default.

## Tech stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3, Navigation Compose
- **DI:** Koin
- **Networking:** Retrofit + OkHttp + Gson (wired up; backed by a mock until the API is live)
- **Location/sensors:** Fused Location Provider (Play Services) + rotation-vector sensor
- **Async:** Kotlin Coroutines + Flow
- **Min SDK:** 29 · **Target/Compile SDK:** 36

## Architecture

The app follows an MVVM structure with a thin data layer:

```
com.blindspot.app
├─ BlindspotApplication.kt      # Application entry point; starts Koin
├─ MainActivity.kt              # Hosts the Compose UI
├─ data/
│  ├─ model/Place.kt            # Domain model
│  ├─ remote/PlaceApi.kt        # Retrofit API contract (+ PlaceDto)
│  └─ repository/               # PlaceRepository + MockPlaceRepository
├─ location/LocationProvider.kt # Fused location: last fix + updates flow
├─ sensor/CompassSensorManager.kt # Smoothed device heading flow
├─ util/GeoUtils.kt             # Pure bearing / distance / formatting helpers
├─ di/AppModule.kt              # Koin module
├─ navigation/Destinations.kt   # Bottom-nav destinations (Maps, Discovery, Feed)
└─ ui/
   ├─ BlindspotApp.kt           # Root scaffold + bottom navigation
   ├─ components/               # GradientBackground, GlassSurface, CompassView,
   │                            #   PlaceBanner, PlaceInfoSheet, PermissionGate
   ├─ discovery/                # DiscoveryViewModel + DiscoveryUiState
   ├─ screens/                  # DiscoveryScreen (+ Maps/Feed placeholders)
   └─ theme/                    # Gemini color palette + theme
```

The compass math (`GeoUtils`) is framework-free and unit-tested. The needle uses an "unwrapped"
target so it always rotates the shortest way and never spins across the 0°/360° boundary.

## Getting started

### Prerequisites

- Android Studio (latest stable) with Android SDK 36
- A device or emulator running Android 10 (API 29) or higher, with location enabled

### Build & run

```bash
# Build a debug APK
./gradlew assembleDebug        # Windows: .\gradlew.bat assembleDebug

# Install on a connected device/emulator
./gradlew installDebug

# Run the unit tests
./gradlew testDebugUnitTest
```

The app requests location permission on first launch; grant it so the compass can find places.

## Backend / data

`MockPlaceRepository` generates a handful of places around your current location so the compass
has realistic targets while there's no live backend. To switch to a real API:

1. Implement `PlaceRepository` against `PlaceApi`.
2. Set `BASE_URL` in `di/AppModule.kt`.
3. Change the `single<PlaceRepository>` binding to the network-backed implementation.

## Permissions

- `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` — to locate you and compute distances/bearings.
- Compass (rotation-vector) sensor is used when available and degrades gracefully when not.