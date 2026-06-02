package com.blindspot.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Map
import androidx.compose.ui.graphics.vector.ImageVector

enum class Destination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    Maps("maps", "Maps", Icons.Filled.Map),
    Discovery("discovery", "Discovery", Icons.Filled.Explore),
    Feed("feed", "Feed", Icons.Filled.DynamicFeed),
}
