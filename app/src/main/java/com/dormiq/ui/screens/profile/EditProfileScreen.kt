package com.dormiq.ui.screens.profile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dormiq.ui.components.*
import com.dormiq.ui.theme.*
import com.dormiq.ui.viewmodel.AuthViewModel
import com.dormiq.ui.viewmodel.AuthState

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val user = (authState as? AuthState.Authenticated)?.user

    var name by remember { mutableStateOf(user?.name ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var phone by remember { mutableStateOf(user?.phone ?: "") }
    var isUpdating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = DormiqNavy,
        topBar = { DormiqTopBar(title = "Edit Profile", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Box(Modifier.fillMaxWidth(), Alignment.Center) {
                AvatarCircle(name.ifEmpty { "User" }, size = 80.dp)
            }
            
            Spacer(Modifier.height(32.dp))
            
            SectionLabel("Basic Information")
            Spacer(Modifier.height(12.dp))
            
            DormiqTextField(
                value = name,
                onValueChange = { name = it },
                label = "Full Name",
                leadingIcon = Icons.Default.Person,
                placeholder = "Enter your full name"
            )
            
            Spacer(Modifier.height(16.dp))
            
            DormiqTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email Address",
                leadingIcon = Icons.Default.Email,
                placeholder = "Enter your email"
            )
            
            Spacer(Modifier.height(16.dp))
            
            DormiqTextField(
                value = phone,
                onValueChange = { }, // Phone should typically be read-only as it's the auth ID
                label = "Phone Number",
                leadingIcon = Icons.Default.Phone,
                readOnly = true,
                enabled = false
            )
            
            Spacer(Modifier.height(40.dp))
            
            if (errorMessage != null) {
                Text(errorMessage!!, color = DormiqRed, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp))
            }
            
            DormiqPrimaryButton(
                text = if (isUpdating) "Saving..." else "Save Changes",
                onClick = {
                    if (user != null) {
                        isUpdating = true
                        authViewModel.updateProfile(user.id, name, email) { success, message ->
                            isUpdating = false
                            if (success) {
                                onSuccess()
                            } else {
                                errorMessage = message
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isUpdating && name.isNotBlank()
            )
        }
    }
}
