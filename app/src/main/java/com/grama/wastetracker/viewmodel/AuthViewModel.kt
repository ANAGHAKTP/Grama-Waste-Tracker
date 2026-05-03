package com.grama.wastetracker.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.grama.wastetracker.BuildConfig
import com.grama.wastetracker.data.model.UserProfile
import com.grama.wastetracker.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: FirebaseUser, val profile: UserProfile) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkExistingSession()
    }

    fun checkExistingSession() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val user = authRepository.getCurrentUser()
            if (user != null) {
                val profile = authRepository.getCurrentProfile()
                if (profile != null) {
                    _authState.value = AuthState.Success(user, profile)
                } else {
                    _authState.value = AuthState.Idle
                }
            } else {
                _authState.value = AuthState.Idle
            }
        }
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.signInWithGoogle(BuildConfig.WEB_CLIENT_ID)
                .onSuccess { profile ->
                    val user = authRepository.getCurrentUser()
                    if (user != null) {
                        _authState.value = AuthState.Success(user, profile)
                    } else {
                        _authState.value = AuthState.Error("Failed to retrieve current user")
                    }
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "Sign-in failed")
                }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _authState.value = AuthState.Idle
    }

    // Factory
    companion object {
        fun factory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return AuthViewModel(AuthRepository(context)) as T
                }
            }
        }
    }
}
