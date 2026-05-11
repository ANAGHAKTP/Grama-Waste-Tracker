package com.grama.wastetracker.ui.screens

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grama.wastetracker.R
import com.grama.wastetracker.ui.theme.AccentError
import com.grama.wastetracker.ui.theme.AccentPrimary
import com.grama.wastetracker.ui.theme.GramaTheme
import com.grama.wastetracker.viewmodel.AuthState
import com.grama.wastetracker.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity
    val viewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.factory(context)
    )
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
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
                        Color(0xFF0F1419),
                        Color(0xFF1A2332),
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
            verticalArrangement = Arrangement.Center
        ) {
            // Branding Section
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(1200)) + slideInVertically(tween(1200)) { -40 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(R.drawable.ic_logo),
                        contentDescription = "Grama Logo",
                        modifier = Modifier
                            .size(120.dp)
                            .padding(bottom = 16.dp)
                    )
                    
                    Text(
                        text = "Grama Waste Tracker",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Text(
                        text = "Clean Village. Green Future.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GramaTheme.colors.accentSecondary,
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Phone Auth UI based on current AuthState
            AnimatedContent(
                targetState = authState,
                label = "auth_state_transition"
            ) { state ->
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (state) {
                        is AuthState.Loading -> {
                            CircularProgressIndicator(color = AccentPrimary)
                        }
                        
                        is AuthState.OtpSent -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Enter the code sent to +91$phoneNumber",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(16.dp))
                                OutlinedTextField(
                                    value = otpCode,
                                    onValueChange = { if (it.length <= 6) otpCode = it },
                                    label = { Text("OTP Code", color = Color.Gray) },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = AccentPrimary,
                                        unfocusedBorderColor = Color.Gray
                                    )
                                )
                                Spacer(Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.verifyOtp(otpCode) },
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Verify & Login")
                                }
                                TextButton(onClick = { viewModel.resetToIdle() }) {
                                    Text("Change Number", color = Color.White.copy(alpha = 0.7f))
                                }
                            }
                        }

                        else -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (state is AuthState.Error) {
                                    Text(
                                        state.message,
                                        color = AccentError,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )
                                }

                                // Phone Login
                                OutlinedTextField(
                                    value = phoneNumber,
                                    onValueChange = { phoneNumber = it },
                                    label = { Text("Phone Number", color = Color.Gray) },
                                    prefix = { Text("+91 ", color = Color.White) },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = AccentPrimary,
                                        unfocusedBorderColor = Color.Gray
                                    )
                                )
                                Spacer(Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.sendOtp("+91$phoneNumber", activity) },
                                    enabled = phoneNumber.length >= 10,
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Send OTP")
                                }

                                Spacer(Modifier.height(24.dp))
                                Text("OR", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                                Spacer(Modifier.height(24.dp))

                                GoogleSignInButton(onClick = { viewModel.signInWithGoogle(context) })
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))
            
            Text(
                text = "© 2026 GRAMA-WASTE SYSTEMS",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
fun GoogleSignInButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_google),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "Continue with Google",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
