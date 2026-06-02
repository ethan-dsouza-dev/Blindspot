package com.blindspot.app.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun FeedScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = "Feed",
        icon = Icons.Filled.DynamicFeed,
        modifier = modifier,
    )
}
