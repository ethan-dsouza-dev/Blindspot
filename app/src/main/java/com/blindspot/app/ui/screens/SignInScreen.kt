package com.blindspot.app.ui.screens

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.blindspot.app.R
import com.blindspot.app.auth.AuthViewModel
import com.blindspot.app.auth.SignInUiState
import com.blindspot.app.ui.components.aurora.AuroraCard
import com.blindspot.app.ui.theme.AuroraTokens
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignInScreen(
    onSignedIn: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = koinViewModel()
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
        contentAlignment = Alignment.Center
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
                style = MaterialTheme.typography.bodyMedium,
                color = AuroraTokens.TextSecondary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(32.dp))

            val isLoading = state is SignInUiState.Loading
            val enabled = !isLoading && state !is SignInUiState.SignedIn

            Box(
                modifier = Modifier
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(
                        enabled = enabled,
                        role = Role.Button,
                        onClick = { activity?.let { viewModel.signInWithGoogle(it) } },
                    )
                    .semantics { contentDescription = "Continue with Google" },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.sign_in_with_google_dark),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = AuroraTokens.OnAccent,
                        strokeWidth = 2.dp,
                    )
                }
            }

            if (state is SignInUiState.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                AuroraCard(
                    color = AuroraTokens.Negative.copy(alpha = 0.15f)
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