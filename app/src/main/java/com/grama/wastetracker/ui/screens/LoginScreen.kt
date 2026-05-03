package com.grama.wastetracker.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grama.wastetracker.R
import com.grama.wastetracker.ui.theme.AccentError
import com.grama.wastetracker.ui.theme.AccentPrimary
import com.grama.wastetracker.ui.theme.GramaTheme
import com.grama.wastetracker.ui.theme.SpaceGroteskFamily
import com.grama.wastetracker.viewmodel.AuthState
import com.grama.wastetracker.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.factory(context)
    )
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) onAuthSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GramaTheme.colors.bgPrimary)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Branding — match your existing SpaceGrotesk style
        Text(
            "GRAMA",
            style = MaterialTheme.typography.displayMedium.copy(
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold
            ),
            color = GramaTheme.colors.textPrimary
        )
        Text(
            "WASTE TRACKER",
            style = MaterialTheme.typography.titleMedium.copy(
                letterSpacing = 4.sp
            ),
            color = AccentPrimary
        )

        Spacer(Modifier.height(64.dp))

        when (val state = authState) {
            is AuthState.Loading -> {
                CircularProgressIndicator(color = AccentPrimary)
            }
            is AuthState.Error -> {
                Surface(
                    color = AccentError.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, AccentError.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        state.message,
                        color = AccentError,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
                GoogleSignInButton(onClick = { viewModel.signInWithGoogle(context) })
            }
            else -> GoogleSignInButton(onClick = { viewModel.signInWithGoogle(context) })
        }
    }
}

@Composable
fun GoogleSignInButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, GramaTheme.colors.borderDim)
    ) {
        // Add res/drawable/ic_google.xml (standard Google G SVG)
        Icon(
            painter = painterResource(R.drawable.ic_google),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            "Continue with Google",
            style = MaterialTheme.typography.labelLarge,
            color = GramaTheme.colors.textPrimary
        )
    }
}
