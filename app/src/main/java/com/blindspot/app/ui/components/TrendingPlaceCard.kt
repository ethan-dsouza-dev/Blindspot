package com.blindspot.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.blindspot.app.R
import com.blindspot.app.ui.components.aurora.AuroraSurface
import com.blindspot.app.ui.feed.TrendingPlaceItem
import com.blindspot.app.ui.theme.AuroraTokens
import com.blindspot.app.util.categoryLabel
import com.blindspot.app.util.ratingLabel

/**
 * Portrait card for the Trending Now rail: photo with a rating chip, one-line name, and a
 * single quiet metadata line (`400 m · Bar`). Tapping the card invokes [onClick].
 */
@Composable
fun TrendingPlaceCard(
    item: TrendingPlaceItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val place = item.place

    AuroraSurface(
        modifier = modifier.width(160.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        ) {
            Box {
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
                place.ratingLabel?.let { rating ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = AuroraTokens.RatingStar,
                            modifier = Modifier.size(12.dp),
                        )
                        Text(
                            text = " $rating",
                            style = MaterialTheme.typography.labelSmall,
                            color = AuroraTokens.TextPrimary,
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = AuroraTokens.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${item.distanceLabel} · ${place.categoryLabel}",
                    style = MaterialTheme.typography.labelSmall,
                    color = AuroraTokens.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}
