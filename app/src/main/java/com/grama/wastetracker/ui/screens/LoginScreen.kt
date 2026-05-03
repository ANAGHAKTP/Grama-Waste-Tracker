package com.grama.wastetracker.ui.screens

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grama.wastetracker.ui.theme.*
import com.grama.wastetracker.viewmodel.AuthStep
import com.grama.wastetracker.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = viewModel(),
    onLoginSuccess: () -> Unit
) {
    val state by authViewModel.state.collectAsState()
    val context = LocalContext.current
    val activity = context as Activity

    // Navigate on successful login
    LaunchedEffect(state.step) {
        if (state.step == AuthStep.AUTHENTICATED && state.profile != null) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GramaTheme.colors.bgPrimary)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Feature implementation follows...
        Spacer(modifier = Modifier.weight(1f))

        // ── Rotating Diamond Icon ──
        Box(
            modifier = Modifier
                .size(96.dp)
                .rotate(45f)
                .clip(RoundedCornerShape(4.dp))
                .background(GramaTheme.colors.bgTertiary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Grama Waste",
                tint = AccentPrimary,
                modifier = Modifier
                    .size(40.dp)
                    .rotate(-45f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ── Title ──
        Text(
            text = "GRAMA.WASTE",
            style = MaterialTheme.typography.displaySmall.copy(
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-2).sp
            ),
            color = GramaTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ── Subtitle ──
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(40.dp).height(1.dp).background(GramaTheme.colors.borderDim))
            Text(
                text = "SMART VILLAGE HUB",
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 9.sp, letterSpacing = 3.sp),
                color = GramaTheme.colors.textTertiary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Box(Modifier.width(40.dp).height(1.dp).background(GramaTheme.colors.borderDim))
        }

        Spacer(modifier = Modifier.height(48.dp))

        // ── Feature Grid ──
        Row(modifier = Modifier.width(280.dp), horizontalArrangement = Arrangement.spacedBy(1.dp)) {
            Surface(
                Modifier.weight(1f), color = GramaTheme.colors.bgSecondary,
                border = BorderStroke(1.dp, GramaTheme.colors.borderDim),
                shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
            ) {
                Column(Modifier.padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Phone, null, tint = AccentPrimary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("PHONE AUTH", style = MaterialTheme.typography.labelLarge.copy(fontSize = 9.sp, letterSpacing = 2.sp), color = GramaTheme.colors.textPrimary)
                }
            }
            Surface(
                Modifier.weight(1f), color = GramaTheme.colors.bgSecondary,
                border = BorderStroke(1.dp, GramaTheme.colors.borderDim),
                shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
            ) {
                Column(Modifier.padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Eco, null, tint = AccentPrimary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("SUSTAINABILITY", style = MaterialTheme.typography.labelLarge.copy(fontSize = 9.sp, letterSpacing = 2.sp), color = GramaTheme.colors.textPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ── Error Message ──
        AnimatedVisibility(visible = state.error != null, enter = fadeIn() + scaleIn()) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                color = AccentError.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, AccentError.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(4.dp)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.ErrorOutline, null, tint = AccentError, modifier = Modifier.size(16.dp))
                    Text(state.error ?: "", style = MaterialTheme.typography.bodySmall, color = AccentError)
                }
            }
        }

        // ── Auth Form ──
        AnimatedContent(
            targetState = state.step,
            transitionSpec = {
                slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
            },
            label = "authStep"
        ) { step ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (step) {
                    AuthStep.PHONE_INPUT -> PhoneInputStep(state.phoneNumber, state.loading, authViewModel, activity)
                    AuthStep.OTP_SENT, AuthStep.VERIFYING -> OtpInputStep(state.otp, state.phoneNumber, state.loading, authViewModel)
                    AuthStep.AUTHENTICATED -> { /* Navigating... */ }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "© 2026 GRAMA-WASTE SYSTEMS • V1.0",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, letterSpacing = 2.sp),
            color = GramaTheme.colors.textTertiary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PhoneInputStep(
    phoneNumber: String,
    loading: Boolean,
    viewModel: AuthViewModel,
    activity: Activity
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Label
        Text(
            text = "PHONE NUMBER",
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 10.sp, letterSpacing = 2.sp),
            color = GramaTheme.colors.textTertiary
        )

        // Phone input with +91 prefix
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = viewModel::updatePhoneNumber,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("Enter 10-digit mobile number", style = MaterialTheme.typography.bodyMedium)
            },
            prefix = {
                Text(
                    "+91  ",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = GramaTheme.colors.textPrimary
                )
            },
            leadingIcon = {
                Icon(Icons.Default.Phone, null, tint = AccentPrimary, modifier = Modifier.size(20.dp))
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Go
            ),
            keyboardActions = KeyboardActions(onGo = { viewModel.sendOtp(activity) }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentPrimary,
                unfocusedBorderColor = GramaTheme.colors.borderDim,
                focusedContainerColor = GramaTheme.colors.bgSecondary,
                unfocusedContainerColor = GramaTheme.colors.bgSecondary,
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // Send OTP button
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable(enabled = !loading && phoneNumber.length >= 10) {
                    viewModel.sendOtp(activity)
                },
            color = if (!loading && phoneNumber.length >= 10) AccentPrimary else GramaTheme.colors.bgTertiary,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (loading) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                    Spacer(Modifier.width(12.dp))
                    Text("SENDING...", style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp, letterSpacing = 2.sp), color = Color.White)
                } else {
                    Icon(Icons.Default.Send, null, tint = if (phoneNumber.length >= 10) Color.White else GramaTheme.colors.textTertiary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "SEND OTP",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp, letterSpacing = 2.sp),
                        color = if (phoneNumber.length >= 10) Color.White else GramaTheme.colors.textTertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun OtpInputStep(
    otp: String,
    phoneNumber: String,
    loading: Boolean,
    viewModel: AuthViewModel
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Info
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Sms, null, tint = AccentPrimary, modifier = Modifier.size(16.dp))
            Text(
                "OTP sent to +91 $phoneNumber",
                style = MaterialTheme.typography.bodySmall,
                color = GramaTheme.colors.textSecondary
            )
        }

        // Label
        Text(
            "ENTER 6-DIGIT OTP",
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 10.sp, letterSpacing = 2.sp),
            color = GramaTheme.colors.textTertiary
        )

        // OTP input
        OutlinedTextField(
            value = otp,
            onValueChange = { if (it.length <= 6) viewModel.updateOtp(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("• • • • • •", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
            singleLine = true,
            textStyle = MaterialTheme.typography.headlineMedium.copy(
                textAlign = TextAlign.Center,
                letterSpacing = 8.sp,
                fontWeight = FontWeight.Bold
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { viewModel.verifyOtp() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentPrimary,
                unfocusedBorderColor = GramaTheme.colors.borderDim,
                focusedContainerColor = GramaTheme.colors.bgSecondary,
                unfocusedContainerColor = GramaTheme.colors.bgSecondary,
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // Verify button
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable(enabled = !loading && otp.length == 6) {
                    viewModel.verifyOtp()
                },
            color = if (!loading && otp.length == 6) AccentPrimary else GramaTheme.colors.bgTertiary,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (loading) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                    Spacer(Modifier.width(12.dp))
                    Text("VERIFYING...", style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp, letterSpacing = 2.sp), color = Color.White)
                } else {
                    Icon(Icons.Default.Lock, null, tint = if (otp.length == 6) Color.White else GramaTheme.colors.textTertiary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "VERIFY & ACCESS",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp, letterSpacing = 2.sp),
                        color = if (otp.length == 6) Color.White else GramaTheme.colors.textTertiary
                    )
                }
            }
        }

        // Change number link
        Text(
            text = "← CHANGE NUMBER",
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 10.sp, letterSpacing = 1.sp),
            color = AccentPrimary,
            modifier = Modifier
                .clickable { viewModel.goBackToPhoneInput() }
                .padding(vertical = 8.dp)
        )
    }
}
