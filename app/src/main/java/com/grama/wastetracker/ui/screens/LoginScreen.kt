package com.grama.wastetracker.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) onAuthSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F1419), // DarkBgPrimary
                        Color(0xFF1A2332), // DarkBgSecondary
                        AccentPrimary.copy(alpha = 0.2f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(100.dp))

            // Branding Section
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(1200)) + slideInVertically(tween(1200)) { -40 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(R.drawable.ic_logo_leaf),
                        contentDescription = "Grama Logo",
                        modifier = Modifier
                            .size(180.dp)
                            .padding(bottom = 32.dp)
                    )
                    
                    Text(
                        text = "Grama Waste Tracker",
                        style = MaterialTheme.typography.displaySmall,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    
                    Text(
                        text = "Clean Village. Green Future.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = AccentSecondary,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Text(
                        text = "Join our community-led movement to make every village sustainable and waste-free.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }

            // Action Section
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(1200, 600)) + slideInVertically(tween(1200, 600)) { 40 }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (val state = authState) {
                        is AuthState.Loading -> {
                            CircularProgressIndicator(color = AccentPrimary)
                        }
                        is AuthState.Error -> {
                            Surface(
                                color = AccentError.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, AccentError.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    state.message,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                            Spacer(Modifier.height(20.dp))
                            GoogleSignInButton(onClick = { viewModel.signInWithGoogle(context) })
                        }
                        else -> {
                            GoogleSignInButton(onClick = { viewModel.signInWithGoogle(context) })
                        }
                    }
                    
                    Spacer(Modifier.height(32.dp))
                    
                    Text(
                        text = "By continuing, you agree to our Terms & Privacy Policy",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
fun GoogleSignInButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, GramaTheme.colors.borderDim),
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterHorizontally,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_google),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(16.dp))
            Text(
                "Continue with Google",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
