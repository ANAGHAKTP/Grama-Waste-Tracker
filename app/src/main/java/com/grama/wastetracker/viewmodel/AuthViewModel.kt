package com.grama.wastetracker.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.grama.wastetracker.data.model.UserProfile
import com.grama.wastetracker.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AuthStep {
    PHONE_INPUT,   // User enters phone number
    OTP_SENT,      // OTP sent, waiting for code input
    VERIFYING,     // Verifying OTP
    AUTHENTICATED  // Signed in
}

data class AuthState(
    val user: FirebaseUser? = null,
    val profile: UserProfile? = null,
    val loading: Boolean = true,
    val error: String? = null,
    val step: AuthStep = AuthStep.PHONE_INPUT,
    val phoneNumber: String = "",
    val otp: String = "",
    val verificationId: String? = null
)

class AuthViewModel(
    private val authRepo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        observeAuth()
    }

    private fun observeAuth() {
        viewModelScope.launch {
            authRepo.observeAuthState().collect { firebaseUser ->
                if (firebaseUser != null) {
                    try {
                        val profile = authRepo.getOrCreateProfile(firebaseUser)
                        _state.value = _state.value.copy(
                            user = firebaseUser,
                            profile = profile,
                            loading = false,
                            step = AuthStep.AUTHENTICATED
                        )
                    } catch (e: Exception) {
                        _state.value = _state.value.copy(
                            user = firebaseUser,
                            loading = false,
                            error = e.message
                        )
                    }
                } else {
                    _state.value = AuthState(loading = false)
                }
            }
        }
    }

    fun updatePhoneNumber(phone: String) {
        _state.value = _state.value.copy(phoneNumber = phone, error = null)
    }

    fun updateOtp(code: String) {
        _state.value = _state.value.copy(otp = code, error = null)
    }

    /**
     * Send OTP to the entered phone number.
     */
    fun sendOtp(activity: Activity) {
        val phone = _state.value.phoneNumber.trim()
        if (phone.isEmpty()) {
            _state.value = _state.value.copy(error = "Please enter a phone number")
            return
        }

        // Add country code if not present
        val fullPhone = if (phone.startsWith("+")) phone else "+91$phone"

        _state.value = _state.value.copy(loading = true, error = null)

        authRepo.sendOtp(
            phoneNumber = fullPhone,
            activity = activity,
            onCodeSent = { verificationId ->
                _state.value = _state.value.copy(
                    loading = false,
                    step = AuthStep.OTP_SENT,
                    verificationId = verificationId
                )
            },
            onAutoVerified = { credential ->
                signInWithCredential(credential)
            },
            onError = { e ->
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "Failed to send OTP"
                )
            }
        )
    }

    /**
     * Verify the entered OTP code.
     */
    fun verifyOtp() {
        val verificationId = _state.value.verificationId
        val code = _state.value.otp.trim()

        if (verificationId == null) {
            _state.value = _state.value.copy(error = "Please request OTP first")
            return
        }
        if (code.length != 6) {
            _state.value = _state.value.copy(error = "Please enter a 6-digit OTP")
            return
        }

        _state.value = _state.value.copy(loading = true, step = AuthStep.VERIFYING, error = null)

        viewModelScope.launch {
            try {
                val profile = authRepo.verifyOtp(verificationId, code)
                _state.value = _state.value.copy(
                    user = authRepo.currentUser(),
                    profile = profile,
                    loading = false,
                    step = AuthStep.AUTHENTICATED
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    loading = false,
                    step = AuthStep.OTP_SENT,
                    error = e.message ?: "Invalid OTP"
                )
            }
        }
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, step = AuthStep.VERIFYING)
            try {
                val profile = authRepo.signInWithCredential(credential)
                _state.value = _state.value.copy(
                    user = authRepo.currentUser(),
                    profile = profile,
                    loading = false,
                    step = AuthStep.AUTHENTICATED
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    loading = false,
                    step = AuthStep.PHONE_INPUT,
                    error = e.message
                )
            }
        }
    }

    /**
     * Go back to phone input step.
     */
    fun goBackToPhoneInput() {
        _state.value = _state.value.copy(
            step = AuthStep.PHONE_INPUT,
            otp = "",
            verificationId = null,
            error = null
        )
    }

    fun signOut() {
        authRepo.signOut()
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
