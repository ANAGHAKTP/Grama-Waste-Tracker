package com.grama.wastetracker.ui.screens

import android.app.Activity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grama.wastetracker.R
import com.grama.wastetracker.ui.theme.AccentPrimary
import com.grama.wastetracker.ui.theme.LightTextPrimary
import com.grama.wastetracker.ui.theme.LightTextSecondary
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

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) onAuthSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF4FBF4),
                        Color(0xFFF7FAF7)
                    )
                )
            )
    ) {
        // Decorative Background Blobs
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color(0xFF6DBB75),
                radius = 350.dp.toPx(),
                center = Offset(-100.dp.toPx(), 100.dp.toPx()),
                alpha = 0.03f
            )
            drawCircle(
                color = Color(0xFF6DBB75),
                radius = 450.dp.toPx(),
                center = Offset(size.width + 50.dp.toPx(), size.height / 2),
                alpha = 0.03f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Logo Image
            Image(
                painter = painterResource(R.drawable.ic_logo),
                contentDescription = "Grama Logo",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(95.dp)
                    .padding(bottom = 16.dp)
            )

            // App Title
            Text(
                text = "Grama Waste Tracker",
                fontSize = 28.sp,
                color = Color(0xFF111111),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            
            // Tagline
            Text(
                text = "Clean Village. Green Future.",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp,
                color = LightTextSecondary,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .align(Alignment.CenterHorizontally),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(56.dp))

            // Authentication Interface
            when (val state = authState) {
                is AuthState.Loading -> {
                    CircularProgressIndicator(color = AccentPrimary)
                }
                is AuthState.OtpSent -> {
                    OtpInputSection(
                        otpCode = otpCode,
                        onOtpChange = { otpCode = it },
                        onVerify = { viewModel.verifyOtp(otpCode) },
                        onBack = { viewModel.resetToIdle() }
                    )
                }
                else -> {
                    LoginInputSection(
                        phoneNumber = phoneNumber,
                        onPhoneChange = { phoneNumber = it },
                        onSendOtp = { viewModel.sendOtp("+91$phoneNumber", activity) },
                        onGoogleSignIn = { viewModel.signInWithGoogle(context) },
                        error = (state as? AuthState.Error)?.message
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Security Footer
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_lock_idle_lock),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = LightTextSecondary.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Your data is secure and private",
                    fontSize = 12.sp,
                    color = LightTextSecondary.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Bottom Illustration PNG
        Image(
            painter = painterResource(R.drawable.ill_bottom_village),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .alpha(0.22f),
            contentScale = ContentScale.FillWidth
        )
    }
}

@Composable
fun OtpInputField(
    otpCode: String,
    onOtpChange: (String) -> Unit
) {
    Box(contentAlignment = Alignment.Center) {
        // Hidden TextField for input capture
        BasicTextField(
            value = otpCode,
            onValueChange = { if (it.length <= 6) onOtpChange(it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.alpha(0f).fillMaxWidth(),
            singleLine = true
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            repeat(6) { index ->
                val char = otpCode.getOrNull(index)?.toString() ?: ""
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(62.dp)
                        .shadow(
                            6.dp,
                            RoundedCornerShape(16.dp),
                            ambientColor = Color.Black.copy(alpha = 0.03f)
                        )
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .border(
                            1.dp,
                            if (char.isNotEmpty()) Color(0xFF6DBB75) 
                            else Color.LightGray.copy(alpha = 0.15f),
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = char,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightTextPrimary
                    )
                }
            }
        }
    }
}
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    prefix: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .shadow(
                6.dp,
                RoundedCornerShape(18.dp),
                ambientColor = Color(0xFF6DBB75).copy(alpha = 0.08f)
            )
            .border(
                1.dp,
                Color.White.copy(alpha = 0.7f),
                RoundedCornerShape(18.dp)
            )
            .background(
                Color.White,
                RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (prefix != null) {
                Text(
                    text = prefix,
                    color = LightTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    color = LightTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                ),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = Color.Gray.copy(alpha = 0.5f),
                            fontSize = 16.sp
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .shadow(
                12.dp,
                RoundedCornerShape(18.dp),
                ambientColor = Color(0xFF6DBB75).copy(alpha = 0.25f)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (enabled) {
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF6DBB75),
                            Color(0xFF8EDC95)
                        )
                    )
                } else {
                    Color.LightGray.copy(alpha = 0.3f)
                }
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            if (enabled) {
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun LoginInputSection(
    phoneNumber: String,
    onPhoneChange: (String) -> Unit,
    onSendOtp: () -> Unit,
    onGoogleSignIn: () -> Unit,
    error: String?
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Custom Phone Number Field
        CustomTextField(
            value = phoneNumber,
            onValueChange = { if (it.length <= 10) onPhoneChange(it) },
            placeholder = "Phone Number",
            prefix = "+91 ",
            keyboardType = KeyboardType.Phone
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Custom Gradient Send OTP Button
        GradientButton(
            text = "Send OTP",
            onClick = onSendOtp,
            enabled = phoneNumber.length >= 10
        )

        Spacer(modifier = Modifier.height(24.dp))

        // OR Divider
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray.copy(alpha = 0.3f))
            Text(
                text = "OR",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray.copy(alpha = 0.6f)
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray.copy(alpha = 0.3f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Custom Google Sign In Button (Box-based)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(18.dp))
                .border(1.dp, Color.LightGray.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                .clickable { onGoogleSignIn() },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_google),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Continue with Google",
                    color = LightTextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun OtpInputSection(
    otpCode: String,
    onOtpChange: (String) -> Unit,
    onVerify: () -> Unit,
    onBack: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Verify OTP",
            style = MaterialTheme.typography.titleMedium,
            color = LightTextPrimary
        )
        Text(
            text = "Enter the code sent to your phone",
            style = MaterialTheme.typography.bodySmall,
            color = LightTextSecondary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Custom 6-Box OTP Field
        OtpInputField(
            otpCode = otpCode,
            onOtpChange = onOtpChange
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Custom Gradient Verify Button
        GradientButton(
            text = "Verify & Continue",
            onClick = onVerify,
            enabled = otpCode.length == 6
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onBack() }
                .padding(8.dp)
        ) {
            Text(
                text = "Change Phone Number",
                color = AccentPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
