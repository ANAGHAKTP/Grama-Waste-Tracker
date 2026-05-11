package com.grama.wastetracker.ui.screens

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grama.wastetracker.ui.theme.AccentPrimary
import com.grama.wastetracker.ui.theme.GramaTheme
import com.grama.wastetracker.viewmodel.AuthState
import com.grama.wastetracker.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onRegistrationSuccess: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val viewModel: AuthViewModel = viewModel(factory = AuthViewModel.factory(context))
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) onRegistrationSuccess()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Account", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = GramaTheme.colors.bgPrimary
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            
            Text(
                "Join the Grama community and help us keep our village green.",
                style = MaterialTheme.typography.bodyLarge,
                color = GramaTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = GramaTheme.colors.bgSecondary,
                border = BorderStroke(1.dp, GramaTheme.colors.borderDim),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    AnimatedContent(
                        targetState = authState,
                        label = "registration_state"
                    ) { state ->
                        when (state) {
                            is AuthState.OtpSent -> {
                                OtpSection(
                                    otpCode = otpCode,
                                    onOtpChange = { otpCode = it },
                                    onVerify = { viewModel.verifyOtp(otpCode) },
                                    phoneNumber = phoneNumber
                                )
                            }
                            is AuthState.Loading -> {
                                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = AccentPrimary)
                                }
                            }
                            else -> {
                                RegistrationForm(
                                    name = name,
                                    onNameChange = { name = it },
                                    phone = phoneNumber,
                                    onPhoneChange = { phoneNumber = it },
                                    address = address,
                                    onAddressChange = { address = it },
                                    onRegister = {
                                        activity?.let {
                                            viewModel.registerCitizen(
                                                name = name,
                                                phone = "+91$phoneNumber",
                                                address = address,
                                                activity = it
                                            )
                                        }
                                    },
                                    errorMessage = (state as? AuthState.Error)?.message
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RegistrationForm(
    name: String, onNameChange: (String) -> Unit,
    phone: String, onPhoneChange: (String) -> Unit,
    address: String, onAddressChange: (String) -> Unit,
    onRegister: () -> Unit,
    errorMessage: String?
) {
    Column {
        if (errorMessage != null) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 16.dp))
        }

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { if (it.length <= 10) onPhoneChange(it) },
            label = { Text("Phone Number") },
            prefix = { Text("+91 ") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = address,
            onValueChange = onAddressChange,
            label = { Text("Home Address / Ward No.") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onRegister,
            enabled = name.isNotBlank() && phone.length == 10 && address.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Create Account", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun OtpSection(
    otpCode: String,
    onOtpChange: (String) -> Unit,
    onVerify: () -> Unit,
    phoneNumber: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Verify Phone", style = MaterialTheme.typography.titleLarge, color = Color.White)
        Text(
            "Enter the code sent to +91 $phoneNumber",
            style = MaterialTheme.typography.bodyMedium,
            color = GramaTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        OutlinedTextField(
            value = otpCode,
            onValueChange = { if (it.length <= 6) onOtpChange(it) },
            placeholder = { Text("000000") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, letterSpacing = 4.sp)
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onVerify,
            enabled = otpCode.length == 6,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Verify & Finish", fontWeight = FontWeight.Bold)
        }
    }
}
