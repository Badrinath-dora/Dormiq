package com.dormiq.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dormiq.domain.model.UserProfile
import com.dormiq.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tenantRepository: com.dormiq.domain.repository.TenantRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
 
    init {
        observeSession()
    }

    private fun observeSession() {
        viewModelScope.launch {
            authRepository.sessionStatus.collect { status ->
                when (status) {
                    is io.github.jan.supabase.auth.status.SessionStatus.Authenticated -> {
                        val user = authRepository.getCurrentUser()
                        if (user != null) {
                            _authState.value = AuthState.Authenticated(user)
                        }
                    }
                    is io.github.jan.supabase.auth.status.SessionStatus.NotAuthenticated -> {
                        _authState.value = AuthState.NotAuthenticated
                    }
                    else -> {
                        _authState.value = AuthState.Loading
                    }
                }
            }
        }
    }

    fun sendOtp(phone: String, role: com.dormiq.domain.model.UserRole? = null) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.sendOtp(phone, role)
                .onSuccess { _authState.value = AuthState.OtpSent(phone) }
                .onFailure { _authState.value = AuthState.Error(it.message ?: "Failed to send OTP") }
        }
    }

    fun verifyOtp(phone: String, otp: String, name: String? = null, email: String? = null, role: com.dormiq.domain.model.UserRole? = null) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.verifyOtp(phone, otp)
                .onSuccess { user -> 
                    // If logging in as Tenant, verify if number is registered
                    if (role == com.dormiq.domain.model.UserRole.TENANT) {
                        try {
                            val tenant = tenantRepository.getTenantByPhone(phone)
                            if (tenant == null) {
                                // Only block if we successfully searched and found NOTHING
                                authRepository.logout()
                                _authState.value = AuthState.Error("This mobile number is not registered as a tenant.")
                                return@onSuccess
                            } else {
                                // Automatically link the user_id if it's NULL
                                if (tenant.userId == null) {
                                    tenantRepository.updateTenantUserId(tenant.id, user.id)
                                }
                            }
                        } catch (e: Exception) {
                            // If there's a database error (Unauthorized), DO NOT block the user.
                            // We will handle linking later in the Dashboard.
                            println("Tenant verification deferred due to RLS: ${e.message}")
                        }
                    }

                    if ((name != null && name.isNotBlank()) || role != null) {
                        authRepository.updateProfile(user.id, name ?: user.name, email ?: user.email, role)
                    }
                    val refreshedUser = authRepository.getCurrentUser() ?: user
                    val updatedUser = if (role != null) refreshedUser.copy(role = role) else refreshedUser
                    _authState.value = AuthState.Authenticated(updatedUser) 
                }
                .onFailure { _authState.value = AuthState.Error(it.message ?: "Invalid OTP") }
        }
    }

    fun updateProfile(userId: String, name: String, email: String? = null, role: com.dormiq.domain.model.UserRole? = null, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            authRepository.updateProfile(userId, name, email, role)
                .onSuccess {
                    val user = authRepository.getCurrentUser()
                    if (user != null) {
                        _authState.value = AuthState.Authenticated(user)
                    }
                    onResult(true, null)
                }
                .onFailure {
                    onResult(false, it.message ?: "Update failed")
                }
        }
    }

    fun loginWithPassword(userId: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.loginWithPassword(userId, password)
                .onSuccess { user -> _authState.value = AuthState.Authenticated(user) }
                .onFailure { _authState.value = AuthState.Error(it.message ?: "Invalid Credentials") }
        }
    }

    fun completeRegistration(hostelId: String) {
        viewModelScope.launch {
            authRepository.completeRegistration(hostelId)
        }
    }

    fun logout() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.logout()
            _authState.value = AuthState.NotAuthenticated
        }
    }

    fun refreshUser() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            if (user != null) {
                _authState.value = AuthState.Authenticated(user)
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Initial
    }
}

sealed class AuthState {
    object Initial : AuthState()
    object NotAuthenticated : AuthState()
    object Loading : AuthState()
    data class OtpSent(val phone: String) : AuthState()
    data class Authenticated(val user: UserProfile) : AuthState()
    data class Error(val message: String) : AuthState()
}
