package com.blindspot.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.blindspot.app.R
import com.blindspot.app.data.model.Place
import com.blindspot.app.ui.theme.BackgroundMid
import com.blindspot.app.ui.theme.GeminiBlue
import com.blindspot.app.ui.theme.GeminiTeal

/**
 * Material 3 [ModalBottomSheet] showing details for [place]. Kept as a bottom sheet so the
 * compass stays visible in the background. The "Point to another place" action triggers
 * [onSkip] (skip logic lives in the ViewModel).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceInfoSheet(
    place: Place,
    distanceLabel: String,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSkip: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    showBack: Boolean = true,
    onViewOnMap: (() -> Unit)? = null,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BackgroundMid.copy(alpha = 0.96f),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = place.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "$distanceLabel away",
                    style = MaterialTheme.typography.labelLarge,
                    color = GeminiBlue,
                )
                place.rating?.let { rating ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC93C),
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = " $rating",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.85f),
                        )
                    }
                }
                place.priceLevel?.let { level ->
                    Text(
                        text = "·  ${"$".repeat(level.coerceIn(1, 4))}",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.7f),
                    )
                }
            }

            Text(
                text = place.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
            )

            PlacePhotos(
                photos = place.imageUrl.orEmpty().filter { it.isNotBlank() },
                contentDescription = place.name,
                modifier = Modifier.padding(top = 4.dp),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (showBack) {
                    OutlinedButton(
                        onClick = onBack,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GeminiBlue),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipPrevious,
                            contentDescription = "Point to the previous place",
                        )
                    }
                }
                if (onViewOnMap != null) {
                    OutlinedButton(
                        onClick = { onViewOnMap(); onDismiss() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GeminiTeal),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Map,
                            contentDescription = "View on Map",
                        )
                        Text(
                            text = "  Map",
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                Button(
                    onClick = onSkip,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue),
                ) {
                    Icon(imageVector = Icons.Filled.SkipNext, contentDescription = null)
                    Text(
                        text = "Next",
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun PlacePhotos(
    photos: List<String>,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    if (photos.isEmpty()) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
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
            shape = RoundedCornerShape(16.dp),
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
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
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .width(160.dp)
                        .height(180.dp),
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
