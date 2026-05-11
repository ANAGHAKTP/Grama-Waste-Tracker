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
import com.grama.wastetracker.ui.theme.AccentSecondary
import com.grama.wastetracker.ui.theme.GramaTheme
import com.grama.wastetracker.viewmodel.AuthState
import com.grama.wastetracker.viewmodel.AuthViewModel
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun LoginScreen(
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
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
            .background(GramaTheme.colors.bgPrimary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Branding Section
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(1000)) + expandVertically(tween(1000))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 48.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_logo),
                        contentDescription = "Grama Logo",
                        modifier = Modifier
                            .size(100.dp)
                            .padding(bottom = 16.dp)
                    )
                    
                    Text(
                        text = "Grama Waste Tracker",
                        style = MaterialTheme.typography.displaySmall,
                        color = GramaTheme.colors.textPrimary,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Text(
                        text = "Clean Village • Green Future",
                        style = MaterialTheme.typography.labelLarge,
                        color = GramaTheme.colors.textTertiary,
                        textAlign = TextAlign.Center,
                        letterSpacing = 2.sp
                    )
                }
            }

            // Authentication Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = GramaTheme.colors.bgSecondary,
                border = BorderStroke(1.dp, GramaTheme.colors.borderDim),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedContent(
                        targetState = authState,
                        label = "auth_state_transition"
                    ) { state ->
                        when (state) {
                            is AuthState.Loading -> {
                                Box(
                                    modifier = Modifier.height(200.dp).fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = AccentPrimary)
                                }
                            }
                            
                            is AuthState.OtpSent -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "Verification",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = GramaTheme.colors.textPrimary
                                    )
                                    Text(
                                        "Enter the code sent to +91 $phoneNumber",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = GramaTheme.colors.textSecondary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                                    )
                                    
                                    OutlinedTextField(
                                        value = otpCode,
                                        onValueChange = { if (it.length <= 6) otpCode = it },
                                        placeholder = { Text("000000") },
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = GramaTheme.colors.textPrimary,
                                            unfocusedTextColor = GramaTheme.colors.textPrimary,
                                            focusedBorderColor = AccentPrimary,
                                            unfocusedBorderColor = GramaTheme.colors.borderDim
                                        ),
                                        singleLine = true
                                    )
                                    
                                    Spacer(Modifier.height(24.dp))
                                    
                                    Button(
                                        onClick = { viewModel.verifyOtp(otpCode) },
                                        modifier = Modifier.fillMaxWidth().height(56.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Verify OTP", fontWeight = FontWeight.Bold)
                                    }
                                    
                                    TextButton(onClick = { viewModel.resetToIdle() }) {
                                        Text("Change Number", color = AccentSecondary)
                                    }
                                }
                            }

                            else -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "Welcome",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = GramaTheme.colors.textPrimary
                                    )
                                    Text(
                                        "Sign in to access your dashboard",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = GramaTheme.colors.textSecondary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                                    )

                                    if (state is AuthState.Error) {
                                        Text(
                                            state.message,
                                            color = AccentError,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(bottom = 16.dp)
                                        )
                                    }

                                    OutlinedTextField(
                                        value = phoneNumber,
                                        onValueChange = { if (it.length <= 10) phoneNumber = it },
                                        label = { Text("Phone Number") },
                                        prefix = { Text("+91 ") },
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = GramaTheme.colors.textPrimary,
                                            unfocusedTextColor = GramaTheme.colors.textPrimary,
                                            focusedBorderColor = AccentPrimary,
                                            unfocusedBorderColor = GramaTheme.colors.borderDim
                                        ),
                                        singleLine = true
                                    )
                                    
                                    Spacer(Modifier.height(16.dp))
                                    
                                    Button(
                                        onClick = { 
                                            activity?.let { viewModel.sendOtp("+91$phoneNumber", it) }
                                        },
                                        enabled = phoneNumber.length == 10,
                                        modifier = Modifier.fillMaxWidth().height(56.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Get OTP", fontWeight = FontWeight.Bold)
                                    }

                                    Box(
                                        modifier = Modifier.padding(vertical = 24.dp).fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        HorizontalDivider(color = GramaTheme.colors.borderDim)
                                        Surface(
                                            color = GramaTheme.colors.bgSecondary,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        ) {
                                            Text(
                                                "OR",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = GramaTheme.colors.textTertiary
                                            )
                                        }
                                    }

                                    GoogleSignInButton(onClick = { viewModel.signInWithGoogle(context) })
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Text(
            text = "© 2026 GRAMA-WASTE",
            style = MaterialTheme.typography.labelSmall,
            color = GramaTheme.colors.textTertiary,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}

@Composable
fun GoogleSignInButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, GramaTheme.colors.borderDim),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
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
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "Sign in with Google",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
