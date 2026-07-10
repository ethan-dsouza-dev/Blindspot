package com.blindspot.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.blindspot.app.R
import com.blindspot.app.ui.feed.TrendingPlaceItem
import com.blindspot.app.ui.theme.AuroraTokens
import com.blindspot.app.util.categoryLabel
import com.blindspot.app.util.ratingLabel

/**
 * Compact vertical-list row for feed sections ("Near you"): 56dp thumbnail, name, single
 * metadata line, and a trailing chevron. Tapping the row invokes [onClick].
 */
@Composable
fun NearbyPlaceRow(
    item: TrendingPlaceItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val place = item.place

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // TODO: Load place.imageUrl remotely via AsyncImage once real photos are wired up.
        Image(
            painter = painterResource(R.drawable.bar),
            contentDescription = place.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp)),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = place.name,
                style = MaterialTheme.typography.titleSmall,
                color = AuroraTokens.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp),
            ) {
                Text(
                    text = "${item.distanceLabel} · ${place.categoryLabel}",
                    style = MaterialTheme.typography.labelMedium,
                    color = AuroraTokens.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                place.ratingLabel?.let { rating ->
                    Text(
                        text = " · ",
                        style = MaterialTheme.typography.labelMedium,
                        color = AuroraTokens.TextSecondary,
                    )
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = AuroraTokens.RatingStar,
                        modifier = Modifier.size(12.dp),
                    )
                    Text(
                        text = " $rating",
                        style = MaterialTheme.typography.labelMedium,
                        color = AuroraTokens.TextSecondary,
                    )
                }
            }
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = AuroraTokens.TextSecondary,
        )
    }
}
