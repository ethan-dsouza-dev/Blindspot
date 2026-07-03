package com.blindspot.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.blindspot.app.R
import com.blindspot.app.ui.feed.TrendingPlaceItem
import com.blindspot.app.ui.components.aurora.AuroraSurface
import com.blindspot.app.ui.theme.AuroraTokens

/**
 * Portrait frosted-glass card for the Trending Now section. Shows a place photo, name, distance
 * and category. Tapping the card invokes [onClick] (opens the detail sheet in the Feed screen).
 */
@Composable
fun TrendingPlaceCard(
    item: TrendingPlaceItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val place = item.place

    AuroraSurface(
        modifier = modifier
            .width(160.dp)
            .height(220.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick),
        ) {
            // TODO: Load place.imageUrl remotely (e.g. via Coil's AsyncImage) once the image
            // loading dependency is available. Dummy data uses a local placeholder for now.
            Image(
                painter = painterResource(R.drawable.bar),
                contentDescription = place.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AuroraTokens.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.distanceLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = AuroraTokens.AccentCyan,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(
                    text = place.category.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = AuroraTokens.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}
