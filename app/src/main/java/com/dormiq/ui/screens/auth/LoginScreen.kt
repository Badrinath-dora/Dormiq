package com.dormiq.ui.screens.auth

import androidx.compose.animation.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.dormiq.ui.components.*
import com.dormiq.ui.theme.*

import com.dormiq.ui.viewmodel.AuthState

@Composable
fun LoginScreen(
    authState: AuthState,
    onNavigateToOtp: (String, String) -> Unit,
    onTenantLogin: (String, String) -> Unit,
    onNavigateToOwnerRegistration: () -> Unit
) {
    var selectedRole by remember { mutableStateOf("owner") } // "owner" or "tenant"
    
    // Phone state (used for both)
    var phone by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf("") }
    
    // Legacy tenant state (removed password)
    var loginError by remember { mutableStateOf("") }
    
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        if (authState is AuthState.Error) {
            isLoading = false
            if (selectedRole == "owner") phoneError = authState.message else loginError = authState.message
        } else if (authState is AuthState.Initial || authState is AuthState.Authenticated) {
            isLoading = false
        }
    }

    fun validateAndProceed() {
        if (phone.length < 10) {
            phoneError = "Enter a valid 10-digit mobile number"
            return
        }
        phoneError = ""
        isLoading = true
        onNavigateToOtp(phone, selectedRole.uppercase())
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DormiqNavy)
    ) {
        // Top gradient decoration
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(GradientBlueStart.copy(alpha = 0.8f), DormiqNavy),
                        start = Offset(0f, 0f),
                        end = Offset(0f, Float.POSITIVE_INFINITY)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            // Logo
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        Brush.linearGradient(listOf(GradientBlueStart, GradientBlueEnd)),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Apartment, null, tint = Color.White, modifier = Modifier.size(36.dp))
            }

            Spacer(Modifier.height(12.dp))

            Text(
                "Sign in or Register",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = DormiqText
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Enter your details to access your account",
                style = MaterialTheme.typography.bodyMedium,
                color = DormiqTextSecondary
            )

            Spacer(Modifier.height(24.dp))

            // Role toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DormiqSurface, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                listOf("owner" to "Hostel Owner", "tenant" to "Tenant Login").forEach { (role, label) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (selectedRole == role) DormiqBlue else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedRole = role }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selectedRole == role) Color.White else DormiqTextSecondary,
                            fontWeight = if (selectedRole == role) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // Dynamic Fields
            DormiqTextField(
                value = phone,
                onValueChange = { if (it.length <= 10) phone = it },
                label = "Mobile Number",
                leadingIcon = Icons.Default.Phone,
                isError = phoneError.isNotEmpty(),
                errorText = phoneError,
                keyboardType = KeyboardType.Phone,
                placeholder = "Enter registered phone"
            )
            
            Spacer(Modifier.height(12.dp))
            Text(
                if (selectedRole == "owner") "We'll send a 6-digit OTP to verify your ownership"
                else "We'll send a 6-digit OTP to log in to your tenant dashboard",
                style = MaterialTheme.typography.bodySmall,
                color = DormiqTextSecondary,
                textAlign = TextAlign.Center
            )
            
            if (loginError.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(loginError, color = DormiqRed, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(28.dp))

            DormiqPrimaryButton(
                text = if (selectedRole == "owner") "Send OTP" else "Login as Tenant",
                onClick = ::validateAndProceed,
                isLoading = isLoading,
                modifier = Modifier.fillMaxWidth(),
                icon = if (selectedRole == "owner") Icons.Default.Send else Icons.Default.Login
            )

            Spacer(Modifier.height(32.dp))

            // Divider
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = DormiqBorder)
                Text("  New to Dormiq?  ", style = MaterialTheme.typography.bodySmall, color = DormiqTextSecondary)
                HorizontalDivider(modifier = Modifier.weight(1f), color = DormiqBorder)
            }

            Spacer(Modifier.height(16.dp))

            DormiqSecondaryButton(
                text = "Register as Hostel Owner",
                onClick = onNavigateToOwnerRegistration,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.AppRegistration
            )

            Spacer(Modifier.height(24.dp))
            
            // Helpful hint
            Text(
                if (selectedRole == "owner") 
                    "Owners: Use your phone for OTP. New owners will be guided to setup automatically." 
                else 
                    "Tenants: Use your phone number provided to the owner to log in.",
                style = MaterialTheme.typography.labelSmall,
                color = DormiqTextMuted,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Preview
@Composable
fun LoginScreenPreview() {
    DormiqTheme {
        LoginScreen(AuthState.Initial, { _, _ -> }, { _, _ -> }, {})
    }
}
