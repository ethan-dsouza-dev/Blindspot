package com.blindspot.app.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun MapsScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = "Maps",
        icon = Icons.Filled.Map,
        modifier = modifier,
    )
}
