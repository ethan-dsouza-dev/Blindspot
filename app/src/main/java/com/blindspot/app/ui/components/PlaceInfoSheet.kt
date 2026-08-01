package com.blindspot.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.blindspot.app.R
import com.blindspot.app.data.model.Place
import com.blindspot.app.ui.components.aurora.AuroraCard
import com.blindspot.app.ui.theme.AuroraTokens
import com.blindspot.app.util.categoryLabel
import com.blindspot.app.util.priceLabel
import com.blindspot.app.util.ratingLabel

/**
 * The single shared venue detail sheet, used from every entry point (Discover, Feed, Map) so
 * the venue presentation is identical across the app.
 *
 * CTA hierarchy: "Take me there" ([onViewOnMap]) is the primary filled action; "Next"
 * ([onSkip], optional) is the tonal secondary; back is a circular tonal icon button.
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
                .navigationBarsPadding(),
        ) {
            // Hero image with gradient overlay and title.
            HeroImage(
                photos = place.imageUrl.orEmpty().filter { it.isNotBlank() },
                contentDescription = place.name,
                placeName = place.name,
                distanceLabel = distanceLabel,
                modifier = Modifier.fillMaxWidth(),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            ) {
                MetadataChips(
                    place = place,
                    distanceLabel = distanceLabel,
                    modifier = Modifier.padding(top = 16.dp),
                )

                if (place.description.isNotBlank()) {
                    Text(
                        text = place.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AuroraTokens.TextSecondary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (showBack) {
                        CircleIconButton(
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
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/** Hero image with gradient overlay and place name + distance. */
@Composable
private fun HeroImage(
    photos: List<String>,
    contentDescription: String,
    placeName: String,
    distanceLabel: String,
    modifier: Modifier = Modifier,
) {
    val photoShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)

    Box(
        modifier = modifier
            .height(240.dp)
            .clip(photoShape),
        contentAlignment = Alignment.BottomStart,
    ) {
        if (photos.isEmpty()) {
            Image(
                painter = painterResource(R.drawable.bar),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            AsyncImage(
                model = photos.first(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            AuroraTokens.BaseDeep.copy(alpha = 0.7f),
                            AuroraTokens.BaseDeep,
                        ),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
        ) {
            Text(
                text = placeName,
                style = MaterialTheme.typography.displayMedium,
                color = AuroraTokens.TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "$distanceLabel away",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFeatureSettings = "tnum",
                ),
                color = AuroraTokens.AccentCyan,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

/** Metadata as chips: distance (accent), rating, price, category (elevated). */
@Composable
private fun MetadataChips(
    place: Place,
    distanceLabel: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Chip(
            text = distanceLabel,
            textColor = AuroraTokens.AccentCyan,
            backgroundColor = AuroraTokens.AccentCyan.copy(alpha = 0.10f),
            icon = null,
        )
        place.ratingLabel?.let { rating ->
            Chip(
                text = rating,
                textColor = AuroraTokens.TextPrimary,
                backgroundColor = AuroraTokens.SurfaceElevated,
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = AuroraTokens.RatingStar,
                        modifier = Modifier.size(14.dp),
                    )
                },
            )
        }
        place.priceLabel?.let { price ->
            Chip(
                text = price,
                textColor = AuroraTokens.TextSecondary,
                backgroundColor = AuroraTokens.SurfaceElevated,
                icon = null,
            )
        }
        if (place.categoryLabel.isNotBlank()) {
            Chip(
                text = place.categoryLabel,
                textColor = AuroraTokens.TextSecondary,
                backgroundColor = AuroraTokens.SurfaceElevated,
                icon = null,
            )
        }
    }
}

@Composable
private fun Chip(
    text: String,
    textColor: Color,
    backgroundColor: Color,
    icon: (@Composable () -> Unit)?,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(1.dp, AuroraTokens.SurfaceBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        icon?.invoke()
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
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
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AuroraTokens.AccentCyan,
            contentColor = AuroraTokens.OnAccent,
        ),
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
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

/** Tonal secondary button: elevated surface with an accent hairline border. */
@Composable
private fun SecondaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, AuroraTokens.AccentCyan.copy(alpha = 0.10f)),
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

/** Circular tonal icon button for back action. */
@Composable
private fun CircleIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(56.dp),
        shape = CircleShape,
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