package com.grama.wastetracker.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grama.wastetracker.R
import com.grama.wastetracker.ui.theme.*
import com.grama.wastetracker.viewmodel.AuthState
import com.grama.wastetracker.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlin.random.Random

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
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
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
                    colors = listOf(LoginBgTop, LoginBgBottom)
                )
            )
    ) {
        BackgroundEffects()

        AnimatedContent(
            targetState = authState is AuthState.OtpSent,
            label = "auth_state_transition",
            transitionSpec = {
                if (targetState) {
                    slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                } else {
                    slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                }
            }
        ) { isOtpSent ->
            if (isOtpSent) {
                OtpVerificationSection(
                    phoneNumber = "+91 $phoneNumber",
                    onBack = { viewModel.resetToIdle() },
                    onVerify = { viewModel.verifyOtp(it) },
                    onResend = { viewModel.sendOtp("+91$phoneNumber", activity) },
                    isLoading = authState is AuthState.Loading
                )
            } else {
                PhoneInputSection(
                    phoneNumber = phoneNumber,
                    onPhoneNumberChange = { if (it.length <= 10) phoneNumber = it },
                    onSendOtp = { viewModel.sendOtp("+91$phoneNumber", activity) },
                    onGoogleSignIn = { viewModel.signInWithGoogle(context) },
                    visible = visible,
                    isLoading = authState is AuthState.Loading,
                    errorMessage = (authState as? AuthState.Error)?.message
                )
            }
        }
    }
}

@Composable
fun BackgroundEffects() {
    val infiniteTransition = rememberInfiniteTransition(label = "bg_anim")
    val circleAnim1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 40f,
        animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing), RepeatMode.Reverse), label = ""
    )
    
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.2f)) {
            drawCircle(
                color = AccentPrimary.copy(alpha = 0.1f),
                radius = 400f,
                center = Offset(size.width * 0.1f + circleAnim1, size.height * 0.2f)
            )
            drawCircle(
                color = AccentSecondary.copy(alpha = 0.1f),
                radius = 350f,
                center = Offset(size.width * 0.8f - circleAnim1, size.height * 0.7f)
            )
        }
        
        Box(modifier = Modifier.size(250.dp).offset(x = (-80).dp, y = 120.dp).blur(80.dp).background(AccentPrimary.copy(alpha = 0.15f), CircleShape))
        Box(modifier = Modifier.size(200.dp).align(Alignment.BottomEnd).offset(x = 80.dp, y = (-120).dp).blur(70.dp).background(AccentSecondary.copy(alpha = 0.15f), CircleShape))
        
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.05f)) {
            val rows = 40
            val cols = 25
            val gap = size.width / cols
            for (i in 0..cols) {
                for (j in 0..rows) {
                    drawCircle(Color.Gray, radius = 1.2f, center = Offset(i * gap, j * gap))
                }
            }
        }
        
        FloatingParticles()
    }
}

@Composable
fun PhoneInputSection(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    onSendOtp: () -> Unit,
    onGoogleSignIn: () -> Unit,
    visible: Boolean,
    isLoading: Boolean,
    errorMessage: String?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(70.dp))

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(1000)) + slideInVertically(tween(1000)) { -20 }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(R.drawable.ic_logo),
                    contentDescription = "Grama Logo",
                    modifier = Modifier.width(120.dp),
                    contentScale = ContentScale.Fit
                )
                Text(
                    text = "Grama Waste Tracker",
                    style = TextStyle(
                        fontFamily = PoppinsFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp,
                        color = TextHeading
                    ),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Clean Village. Green Future.",
                    style = TextStyle(
                        fontFamily = PoppinsFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = AccentPrimary,
                        letterSpacing = 0.5.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(60.dp))

        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(tween(800, delayMillis = 200)) { 50 } + fadeIn(tween(800, delayMillis = 200))
        ) {
            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Welcome!",
                    style = TextStyle(
                        fontFamily = PoppinsFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.Black
                    )
                )
                Text(
                    text = "Enter your mobile number to continue",
                    style = TextStyle(
                        fontFamily = PoppinsFamily,
                        fontSize = 16.sp,
                        color = Color.Gray
                    ),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .shadow(elevation = 10.dp, shape = RoundedCornerShape(22.dp), ambientColor = Color.LightGray.copy(alpha = 0.1f), spotColor = Color.LightGray.copy(alpha = 0.3f))
                        .background(Color.White, RoundedCornerShape(22.dp))
                        .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(22.dp))
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🇮🇳 +91", color = Color(0xFF1B1B1B), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Spacer(Modifier.width(16.dp))
                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFFE0E0E0)))
                        Spacer(Modifier.width(16.dp))
                        BasicTextField(
                            value = phoneNumber,
                            onValueChange = onPhoneNumberChange,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(color = Color(0xFF111111), fontSize = 17.sp, fontFamily = PoppinsFamily, fontWeight = FontWeight.Medium),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            decorationBox = { innerTextField ->
                                if (phoneNumber.isEmpty()) {
                                    Text("Enter mobile number", color = Color.LightGray, fontSize = 16.sp, fontFamily = PoppinsFamily)
                                }
                                innerTextField()
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        PremiumGradientButton(
            text = "Send OTP",
            onClick = onSendOtp,
            isLoading = isLoading,
            enabled = phoneNumber.length == 10
        )

        Spacer(Modifier.height(40.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
            Text("OR", modifier = Modifier.padding(horizontal = 16.dp), style = TextStyle(fontFamily = PoppinsFamily, color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Medium))
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
        }

        Spacer(Modifier.height(24.dp))

        PremiumGoogleButton(onClick = onGoogleSignIn)

        Spacer(Modifier.height(40.dp))

        SecurityFooter()

        Spacer(Modifier.height(40.dp))

        BottomIllustration()
    }
}

@Composable
fun PremiumGradientButton(
    text: String,
    onClick: () -> Unit,
    isLoading: Boolean,
    enabled: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "button_scale")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .shadow(
                elevation = if (enabled) 15.dp else 0.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = AccentPrimary.copy(alpha = 0.6f)
            )
            .background(
                brush = Brush.horizontalGradient(listOf(ButtonGradientStart, ButtonGradientEnd)),
                shape = RoundedCornerShape(24.dp)
            )
            .alpha(if (enabled) 1f else 0.6f)
            .clickable(enabled = enabled && !isLoading, interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text,
                    style = TextStyle(
                        fontFamily = PoppinsFamily,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp
                    )
                )
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.ArrowForward, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun PremiumGoogleButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(22.dp), spotColor = Color.LightGray.copy(alpha = 0.2f))
            .background(Color.White, RoundedCornerShape(22.dp))
            .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(22.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.ic_google),
                contentDescription = null,
                modifier = Modifier.size(26.dp)
            )
            Spacer(Modifier.width(16.dp))
            Text(
                "Continue with Google",
                style = TextStyle(
                    fontFamily = PoppinsFamily,
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
            )
        }
    }
}

@Composable
fun SecurityFooter() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
        Icon(Icons.Default.Shield, null, tint = Color.Gray.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            "Your data is secure and private",
            style = TextStyle(
                fontFamily = PoppinsFamily,
                fontSize = 13.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
fun BottomIllustration() {
    val infiniteTransition = rememberInfiniteTransition(label = "windmill_anim")
    val windmillRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Restart), label = ""
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .alpha(0.35f)
    ) {
        val width = size.width
        val height = size.height

        // Draw Hills
        val hillPath1 = Path().apply {
            moveTo(0f, height)
            quadraticTo(width * 0.25f, height * 0.4f, width * 0.5f, height * 0.8f)
            lineTo(width * 0.5f, height)
            close()
        }
        drawPath(hillPath1, Color(0xFF81C784))

        val hillPath2 = Path().apply {
            moveTo(width * 0.3f, height)
            quadraticTo(width * 0.65f, height * 0.2f, width, height * 0.7f)
            lineTo(width, height)
            close()
        }
        drawPath(hillPath2, Color(0xFF66BB6A))

        // Simple House
        val houseX = width * 0.1f
        val houseY = height * 0.75f
        drawRect(Color(0xFFFFF9C4), Offset(houseX, houseY), Size(40f, 30f))
        val roofPath = Path().apply {
            moveTo(houseX - 5f, houseY)
            lineTo(houseX + 20f, houseY - 15f)
            lineTo(houseX + 45f, houseY)
            close()
        }
        drawPath(roofPath, Color(0xFFE57373))

        // Windmill
        val wmX = width * 0.85f
        val wmY = height * 0.65f
        drawRect(Color.White, Offset(wmX - 3f, wmY), Size(6f, 60f))
        
        // Blades
        rotate(windmillRotation, Offset(wmX, wmY)) {
            for (i in 0..2) {
                rotate(i * 120f, Offset(wmX, wmY)) {
                    drawRect(Color.White, Offset(wmX - 2f, wmY - 35f), Size(4f, 35f))
                }
            }
        }

        // Simple trees
        fun drawTree(x: Float, y: Float, scale: Float = 1f) {
            drawRect(Color(0xFF795548), Offset(x - 5f * scale, y - 20f * scale), Size(10f * scale, 20f * scale))
            drawCircle(Color(0xFF388E3C), radius = 25f * scale, center = Offset(x, y - 35f * scale))
        }

        drawTree(width * 0.25f, height * 0.75f, 0.8f)
        drawTree(width * 0.7f, height * 0.78f, 1.1f)
        
        // Simple truck
        val truckX = width * 0.4f
        val truckY = height * 0.85f
        drawRoundRect(Color(0xFF4CAF50), Offset(truckX, truckY), Size(60f, 30f), CornerRadius(4f))
        drawRoundRect(Color(0xFF81C784), Offset(truckX + 45f, truckY + 5f), Size(20f, 20f), CornerRadius(4f))
        drawCircle(Color.Black, 8f, Offset(truckX + 15f, truckY + 30f))
        drawCircle(Color.Black, 8f, Offset(truckX + 50f, truckY + 30f))
    }
}

@Composable
fun OtpVerificationSection(
    phoneNumber: String,
    onBack: () -> Unit,
    onVerify: (String) -> Unit,
    onResend: () -> Unit,
    isLoading: Boolean
) {
    val otpValues = remember { mutableStateListOf("", "", "", "", "", "") }
    val focusRequesters = remember { List(6) { FocusRequester() } }
    var timer by remember { mutableIntStateOf(25) }

    LaunchedEffect(Unit) {
        while (timer > 0) {
            delay(1000)
            timer--
        }
    }

    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
            }
            Image(
                painter = painterResource(R.drawable.ic_logo),
                contentDescription = "Grama Logo",
                modifier = Modifier.size(40.dp).align(Alignment.Center)
            )
        }

        Spacer(Modifier.height(60.dp))

        Text(
            text = "Verification",
            style = TextStyle(fontFamily = PoppinsFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color.Black)
        )

        Text(
            text = buildAnnotatedString {
                append("Enter the 6-digit code sent to\n")
                withStyle(style = SpanStyle(color = AccentPrimary, fontWeight = FontWeight.Bold)) {
                    append(phoneNumber)
                }
            },
            style = TextStyle(fontFamily = PoppinsFamily, fontSize = 16.sp, color = Color.Gray),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp, bottom = 48.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            otpValues.forEachIndexed { index, value ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(0.9f)
                        .shadow(6.dp, RoundedCornerShape(16.dp), spotColor = Color.LightGray.copy(alpha = 0.2f))
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .border(1.5.dp, if (value.isNotEmpty()) AccentPrimary else Color(0xFFF0F0F0), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = value,
                        onValueChange = { newValue ->
                            if (newValue.length <= 1) {
                                otpValues[index] = newValue
                                if (newValue.isNotEmpty() && index < 5) {
                                    focusRequesters[index + 1].requestFocus()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentHeight()
                            .focusRequester(focusRequesters[index])
                            .onKeyEvent { keyEvent ->
                                if (keyEvent.key == Key.Backspace && otpValues[index].isEmpty() && index > 0) {
                                    focusRequesters[index - 1].requestFocus()
                                    true
                                } else false
                            },
                        textStyle = TextStyle(
                            fontFamily = PoppinsFamily,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = Color.Black
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }
        }

        Spacer(Modifier.height(40.dp))

        if (timer > 0) {
            Text(
                "Resend code in 00:${timer.toString().padStart(2, '0')}",
                style = TextStyle(fontFamily = PoppinsFamily, color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            )
        } else {
            TextButton(onClick = {
                timer = 25
                onResend()
            }) {
                Text("Resend OTP", color = AccentPrimary, fontWeight = FontWeight.Bold, fontFamily = PoppinsFamily, fontSize = 15.sp)
            }
        }

        Spacer(Modifier.height(56.dp))

        PremiumGradientButton(
            text = "Verify & Continue",
            onClick = { onVerify(otpValues.joinToString("")) },
            isLoading = isLoading,
            enabled = otpValues.all { it.isNotEmpty() }
        )

        Spacer(Modifier.weight(1f))
        BottomIllustration()
    }
}

@Composable
fun FloatingParticles() {
    val particles = remember { List(15) { Particle() } }
    val infiniteTransition = rememberInfiniteTransition(label = "particles")

    particles.forEach { particle ->
        val xOffset by infiniteTransition.animateFloat(
            initialValue = particle.initialX,
            targetValue = particle.initialX + particle.drift,
            animationSpec = infiniteRepeatable(
                animation = tween(particle.duration, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "x"
        )
        val yOffset by infiniteTransition.animateFloat(
            initialValue = particle.initialY,
            targetValue = particle.initialY - 1200f,
            animationSpec = infiniteRepeatable(
                animation = tween(particle.duration, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "y"
        )

        Box(
            modifier = Modifier
                .offset(x = xOffset.dp, y = yOffset.dp)
                .size(particle.size.dp)
                .clip(CircleShape)
                .background(AccentPrimary.copy(alpha = 0.08f))
        )
    }
}

data class Particle(
    val initialX: Float = Random.nextFloat() * 400,
    val initialY: Float = 1200f + Random.nextFloat() * 500,
    val size: Float = 4f + Random.nextFloat() * 12f,
    val duration: Int = 12000 + Random.nextInt(18000),
    val drift: Float = -120f + Random.nextFloat() * 240f
)
