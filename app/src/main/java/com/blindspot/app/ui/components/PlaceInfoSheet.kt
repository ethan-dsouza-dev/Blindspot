package com.blindspot.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.blindspot.app.R
import com.blindspot.app.data.model.Place
import com.blindspot.app.ui.theme.AuroraTokens
import com.blindspot.app.util.categoryLabel
import com.blindspot.app.util.priceLabel
import com.blindspot.app.util.ratingLabel

/**
 * The single shared venue detail sheet, used from every entry point (Discover, Feed, Map) so
 * the venue presentation is identical across the app.
 *
 * CTA hierarchy: "Take me there" ([onViewOnMap]) is the primary filled action; "Next"
 * ([onSkip], optional) is the tonal secondary; back is a tonal icon button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceInfoSheet(
    place: Place,
    distanceLabel: String,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onSkip: (() -> Unit)? = null,
    showBack: Boolean = true,
    onViewOnMap: (() -> Unit)? = null,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AuroraTokens.BaseSlate,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
        ) {
            Text(
                text = place.name,
                style = MaterialTheme.typography.headlineSmall,
                color = AuroraTokens.TextPrimary,
            )

            MetadataRow(
                place = place,
                distanceLabel = distanceLabel,
                modifier = Modifier.padding(top = 8.dp),
            )

            if (place.description.isNotBlank()) {
                Text(
                    text = place.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AuroraTokens.TextSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            PlacePhotos(
                photos = place.imageUrl.orEmpty().filter { it.isNotBlank() },
                contentDescription = place.name,
                modifier = Modifier.padding(top = 16.dp),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (showBack) {
                    SecondaryIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Point to the previous place",
                        onClick = onBack,
                    )
                }
                if (onSkip != null) {
                    SecondaryButton(
                        label = "Next",
                        onClick = onSkip,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (onViewOnMap != null) {
                    PrimaryButton(
                        label = "Take me there",
                        icon = Icons.Filled.NearMe,
                        onClick = { onViewOnMap(); onDismiss() },
                        modifier = Modifier.weight(1.4f),
                    )
                } else if (onSkip == null) {
                    // No actions besides back — shouldn't happen, but keep the row balanced.
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Single unified metadata line: `386 m · ★ 4.5 · $$ · Fine Dining`. Distance carries the only
 * accent; everything else stays quiet so the row scans as one unit.
 */
@Composable
private fun MetadataRow(
    place: Place,
    distanceLabel: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = distanceLabel,
            style = MaterialTheme.typography.labelLarge,
            color = AuroraTokens.AccentCyan,
        )
        place.ratingLabel?.let { rating ->
            MetadataDot()
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = AuroraTokens.RatingStar,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = rating,
                style = MaterialTheme.typography.labelLarge,
                color = AuroraTokens.TextPrimary,
            )
        }
        place.priceLabel?.let { price ->
            MetadataDot()
            Text(
                text = price,
                style = MaterialTheme.typography.labelLarge,
                color = AuroraTokens.TextSecondary,
            )
        }
        if (place.categoryLabel.isNotBlank()) {
            MetadataDot()
            Text(
                text = place.categoryLabel,
                style = MaterialTheme.typography.labelLarge,
                color = AuroraTokens.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun MetadataDot() {
    Text(
        text = "·",
        style = MaterialTheme.typography.labelLarge,
        color = AuroraTokens.TextSecondary,
    )
}

/** Filled accent CTA — the one and only primary button style. */
@Composable
private fun PrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AuroraTokens.AccentCyan,
            contentColor = AuroraTokens.OnAccent,
        ),
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
        )
    }
}

/** Tonal secondary button: elevated surface with a hairline border. */
@Composable
private fun SecondaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(52.dp)
            .border(1.dp, AuroraTokens.SurfaceBorder, CircleShape),
        colors = ButtonDefaults.buttonColors(
            containerColor = AuroraTokens.SurfaceElevated,
            contentColor = AuroraTokens.TextPrimary,
        ),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
        )
    }
}

@Composable
private fun SecondaryIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .size(52.dp)
            .border(1.dp, AuroraTokens.SurfaceBorder, CircleShape),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AuroraTokens.SurfaceElevated,
            contentColor = AuroraTokens.TextPrimary,
        ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun PlacePhotos(
    photos: List<String>,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val photoShape = RoundedCornerShape(16.dp)
    val photoModifier = Modifier.border(1.dp, AuroraTokens.SurfaceBorder, photoShape)

    if (photos.isEmpty()) {
        Card(
            shape = photoShape,
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp)
                .then(photoModifier),
        ) {
            Image(
                painter = painterResource(R.drawable.bar),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    } else if (photos.size == 1) {
        Card(
            shape = photoShape,
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp)
                .then(photoModifier),
        ) {
            AsyncImage(
                model = photos.first(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    } else {
        LazyRow(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(photos) { url ->
                Card(
                    shape = photoShape,
                    modifier = Modifier
                        .width(180.dp)
                        .height(200.dp)
                        .border(1.dp, AuroraTokens.SurfaceBorder, photoShape),
                ) {
                    AsyncImage(
                        model = url,
                        contentDescription = contentDescription,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}
