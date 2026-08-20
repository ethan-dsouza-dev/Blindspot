package com.blindspot.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.blindspot.app.ui.theme.AuroraTokens
import com.blindspot.app.util.categoryLabel
import com.blindspot.app.util.priceLabel
import com.blindspot.app.util.ratingLabel
import com.blindspot.app.util.reviewCountLabel

/** Roughly how many characters a 3-line body description holds at this width; beyond this the
 * description collapses behind a "Read more" toggle. */
private const val DESCRIPTION_FOLD_CHARS = 130

/**
 * The single shared venue detail sheet, used from every entry point (Discover, Feed, Map) so
 * the venue presentation is identical across the app.
 *
 * The hero is a swipeable photo pager with a gradient scrim, page indicators, and a favorite
 * toggle in the top-right corner; the body (chips + description) scrolls while the CTA row stays
 * pinned at the bottom. CTA hierarchy: "Take me there" ([onViewOnMap]) is the primary filled
 * action; "Next" ([onSkip], optional) is the tonal secondary; back is a circular tonal icon
 * button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceInfoSheet(
    place: Place,
    distanceLabel: String,
    sheetState: SheetState,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
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
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(AuroraTokens.SurfaceBorder),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
            ) {
                // Hero image with gradient overlay, title, and favorite toggle.
                HeroImage(
                    photos = place.imageUrl.orEmpty().filter { it.isNotBlank() },
                    contentDescription = place.name,
                    placeName = place.name,
                    distanceLabel = distanceLabel,
                    isFavorite = isFavorite,
                    onToggleFavorite = onToggleFavorite,
                    modifier = Modifier.fillMaxWidth(),
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp),
                ) {
                    MetadataChips(
                        place = place,
                        modifier = Modifier.padding(top = 16.dp),
                    )

                    DescriptionSection(
                        text = place.description,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }
            }

            val hasActions = showBack || onSkip != null || onViewOnMap != null
            if (hasActions) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp),
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
                    }
                }
            }
        }
    }
}

/** Hero image: swipeable photo pager with a gradient scrim, page indicators, favorite toggle,
 * and place name + distance overlaid on the scrim. Falls back to a single placeholder when
 * there are no photos. */
@Composable
private fun HeroImage(
    photos: List<String>,
    contentDescription: String,
    placeName: String,
    distanceLabel: String,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val photoShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    val photoCount = photos.size

    Box(
        modifier = modifier
            .height(240.dp)
            .clip(photoShape),
        contentAlignment = Alignment.BottomStart,
    ) {
        if (photoCount == 0) {
            Image(
                painter = painterResource(R.drawable.bar),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            val pagerState = rememberPagerState(pageCount = { photoCount })
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                AsyncImage(
                    model = photos[page],
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            if (photoCount > 1) {
                PageDots(
                    pageCount = photoCount,
                    currentPage = pagerState.currentPage,
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            }
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

        FavoriteToggleButton(
            isFavorite = isFavorite,
            onClick = onToggleFavorite,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
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

/** Circular scrim button that toggles favorite status: outline heart when not favorited,
 * filled accent heart when favorited. */
@Composable
private fun FavoriteToggleButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(AuroraTokens.BaseDeep.copy(alpha = 0.5f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
            tint = if (isFavorite) AuroraTokens.AccentCyan else AuroraTokens.TextPrimary,
            modifier = Modifier.size(22.dp),
        )
    }
}

/** Body description, collapsed to 3 lines with a "Read more"/"Show less" toggle. */
@Composable
private fun DescriptionSection(
    text: String,
    modifier: Modifier = Modifier,
) {
    if (text.isBlank()) return

    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = AuroraTokens.TextSecondary,
            maxLines = if (expanded) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis,
        )
        if (text.length > DESCRIPTION_FOLD_CHARS) {
            Text(
                text = if (expanded) "Show less" else "Read more",
                style = MaterialTheme.typography.bodyMedium,
                color = AuroraTokens.AccentCyan,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clickable(onClick = { expanded = !expanded })
                    .padding(vertical = 4.dp),
            )
        }
    }
}

/** Metadata as chips: rating, price, category (elevated). Distance is shown in the hero scrim. */
@Composable
private fun MetadataChips(
    place: Place,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        place.ratingLabel?.let { rating ->
            Chip(
                text = buildString {
                    append(rating)
                    place.reviewCountLabel?.let { append(" ($it)") }
                },
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