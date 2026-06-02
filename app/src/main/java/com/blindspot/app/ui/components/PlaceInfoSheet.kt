package com.blindspot.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SkipNext
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.blindspot.app.data.model.Place
import com.blindspot.app.ui.theme.BackgroundMid
import com.blindspot.app.ui.theme.GeminiBlue

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
    modifier: Modifier = Modifier,
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

            Button(
                onClick = onSkip,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue),
            ) {
                Icon(imageVector = Icons.Filled.SkipNext, contentDescription = null)
                Text(
                    text = "  Point to another place",
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
