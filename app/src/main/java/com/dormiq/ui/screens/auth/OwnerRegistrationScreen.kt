package com.dormiq.ui.screens.auth

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dormiq.ui.components.*
import com.dormiq.ui.theme.*
import com.dormiq.ui.viewmodel.AuthViewModel
import com.dormiq.ui.viewmodel.AuthState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState

@Composable
fun OwnerRegistrationScreen(
    onNavigateToOtp: (String, String, String) -> Unit, // phone, name, email
    onRegistrationComplete: () -> Unit,
    onBack: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val currentUser = (authState as? AuthState.Authenticated)?.user

    var fullName by remember { mutableStateOf(currentUser?.name?.takeIf { it != "User" } ?: "") }
    var phone by remember { mutableStateOf(currentUser?.phone ?: "") }
    var email by remember { mutableStateOf(currentUser?.email ?: "") }
    
    var nameError by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf("") }
    
    var isLoading by remember { mutableStateOf(false) }

    fun validateAndProceed() {
        nameError = if (fullName.isBlank()) "Name is required" else ""
        phoneError = if (phone.length < 10) "Enter a valid 10-digit mobile number" else ""
        
        if (nameError.isEmpty() && phoneError.isEmpty()) {
            isLoading = true
            if (currentUser != null) {
                // Already authenticated, just update profile
                authViewModel.updateProfile(currentUser.id, fullName, email, com.dormiq.domain.model.UserRole.OWNER) { success, message ->
                    isLoading = false
                    if (success) {
                        // Profile updated, continue to next step (handled by LaunchedEffect observing authState)
                    }
                }
            } else {
                // Not authenticated, start OTP flow
                onNavigateToOtp(phone, fullName, email)
            }
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.Error) {
            isLoading = false
        }
        if (currentUser != null && currentUser.name != "User" && currentUser.name.isNotBlank()) {
            isLoading = false
            onRegistrationComplete()
        }
    }

    Scaffold(
        containerColor = DormiqNavy,
        topBar = { DormiqTopBar(title = "Owner Registration", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            
            Text(
                "Register as Owner",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = DormiqText
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Enter your details to create a new hostel owner account.",
                style = MaterialTheme.typography.bodyMedium,
                color = DormiqTextSecondary,
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(32.dp))
            
            DormiqTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = "Full Name *",
                leadingIcon = Icons.Default.Person,
                isError = nameError.isNotEmpty(),
                errorText = nameError,
                placeholder = "Enter your full name"
            )
            
            Spacer(Modifier.height(16.dp))
            
            DormiqTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email Address",
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
                placeholder = "Optional"
            )
            
            Spacer(Modifier.height(16.dp))
            
            DormiqTextField(
                value = phone,
                onValueChange = { if (it.length <= 10) phone = it },
                label = "Contact Number *",
                leadingIcon = Icons.Default.Phone,
                isError = phoneError.isNotEmpty(),
                errorText = phoneError,
                keyboardType = KeyboardType.Phone,
                placeholder = "Enter 10-digit number",
                enabled = currentUser == null // Disable if already authenticated
            )
            
            Spacer(Modifier.height(12.dp))
            if (currentUser == null) {
                Text(
                    "We'll send a 6-digit OTP to verify your contact number.",
                    style = MaterialTheme.typography.bodySmall,
                    color = DormiqTextSecondary,
                    textAlign = TextAlign.Center
                )
            }
            
            if (authState is AuthState.Error) {
                Spacer(Modifier.height(16.dp))
                Text(
                    (authState as AuthState.Error).message,
                    color = DormiqRed,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(Modifier.height(32.dp))
            
            DormiqPrimaryButton(
                text = if (currentUser != null) "Complete Registration" else "Verify through OTP",
                onClick = ::validateAndProceed,
                isLoading = isLoading || authState is AuthState.Loading,
                modifier = Modifier.fillMaxWidth(),
                icon = if (currentUser != null) Icons.Default.CheckCircle else Icons.Default.Send
            )
            
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Preview
@Composable
fun OwnerRegistrationScreenPreview() {
    DormiqTheme {
        OwnerRegistrationScreen({ _, _, _ -> }, {}, {})
    }
}
