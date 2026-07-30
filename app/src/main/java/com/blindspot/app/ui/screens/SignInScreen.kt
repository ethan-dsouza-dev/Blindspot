package com.blindspot.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VpnLock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.blindspot.app.auth.AuthViewModel
import com.blindspot.app.auth.SignInUiState
import com.blindspot.app.ui.components.aurora.AuroraSurface
import com.blindspot.app.ui.theme.AuroraTokens
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignInScreen(
    onSignedIn: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = koinViewModel(),
) {
    val state = viewModel.uiState
    val activity = LocalActivity.current

    LaunchedEffect(state) {
        if (state is SignInUiState.SignedIn) {
            onSignedIn()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Blindspot",
                style = MaterialTheme.typography.headlineLarge,
                color = AuroraTokens.TextPrimary,
            )
            Text(
                text = "Discover your next night out",
                style = MaterialTheme.typography.bodyLarge,
                color = AuroraTokens.TextSecondary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(32.dp))

            val isLoading = state is SignInUiState.Loading
            val enabled = !isLoading && state !is SignInUiState.SignedIn

            Button(
                onClick = { activity?.let { viewModel.signInWithGoogle(it) } },
                enabled = enabled,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AuroraTokens.AccentCyan,
                    contentColor = AuroraTokens.OnAccent,
                    disabledContainerColor = AuroraTokens.AccentCyan.copy(alpha = 0.4f),
                ),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = AuroraTokens.OnAccent,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.VpnLock,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Text(
                    text = if (isLoading) "Signing in..." else "Continue with Google",
                    style = MaterialTheme.typography.labelLarge,
                )
            }

            if (state is SignInUiState.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                AuroraSurface(
                    color = AuroraTokens.Negative.copy(alpha = 0.15f),
                    borderColor = AuroraTokens.Negative.copy(alpha = 0.4f),
                ) {
                    Text(
                        text = state.message,
                        color = AuroraTokens.Negative,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
