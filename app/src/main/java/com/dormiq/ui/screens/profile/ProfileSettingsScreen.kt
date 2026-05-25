package com.dormiq.ui.screens.profile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import com.dormiq.domain.model.UserRole
import com.dormiq.ui.components.*
import com.dormiq.ui.theme.*

import androidx.hilt.navigation.compose.hiltViewModel
import com.dormiq.ui.viewmodel.AuthViewModel
import com.dormiq.ui.viewmodel.AuthState

@Composable
fun ProfileSettingsScreen(
    onLogout: () -> Unit,
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onNavigateToManageProperties: () -> Unit = {},
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val user = (authState as? AuthState.Authenticated)?.user
    val isOwner = user?.role == UserRole.OWNER

    var notificationsEnabled by remember { mutableStateOf(true) }
    var emailAlertsEnabled by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var activeDialog by remember { mutableStateOf<String?>(null) }
    
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    authViewModel.logout()
                    onLogout()
                }) {
                    Text("Logout", color = DormiqRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = DormiqTextSecondary)
                }
            },
            title = { Text("Logout", color = DormiqText) },
            text = { Text("Are you sure you want to logout from Dormiq?", color = DormiqTextSecondary) },
            containerColor = DormiqCard,
            titleContentColor = DormiqText
        )
    }

    if (activeDialog != null) {
        AlertDialog(
            onDismissRequest = { activeDialog = null },
            confirmButton = {
                TextButton(onClick = { activeDialog = null }) {
                    Text("Close", color = DormiqBlue)
                }
            },
            title = { Text(activeDialog!!, color = DormiqText) },
            text = {
                Text(
                    when (activeDialog) {
                        "Language" -> "Currently, only English is supported. More languages coming in future updates."
                        "Help & Support" -> "For support, please contact us at support@dormiq.com or call our 24/7 helpline at +91 1800-DORMIQ."
                        "Privacy Policy" -> "Your data is secure with Dormiq. We use industry-standard encryption to protect your personal information."
                        "About Dormiq" -> "Dormiq v1.0.0\nBuilt with ❤️ for better hostel management.\n© 2026 Dormiq Inc."
                        else -> ""
                    },
                    color = DormiqTextSecondary
                )
            },
            containerColor = DormiqCard,
            titleContentColor = DormiqText
        )
    }

    Scaffold(
        containerColor = DormiqNavy,
        topBar = { DormiqTopBar(title = "Profile & Settings", onBack = onBack) }
    ) { padding ->
        if (user == null) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = DormiqBlue)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile Header
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(GradientBlueStart.copy(0.5f), DormiqSurface),
                                    start = Offset(0f, 0f),
                                    end = Offset(Float.POSITIVE_INFINITY, 200f)
                                ),
                                RoundedCornerShape(16.dp)
                            )
                            .border(1.dp, DormiqBorder, RoundedCornerShape(16.dp))
                            .padding(20.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            AvatarCircle(user.name.ifEmpty { "User" }, size = 72.dp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                user.name.ifEmpty { "Dormiq User" },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = DormiqText
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .background(DormiqBlueGlow, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    if (isOwner) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                    null,
                                    tint = DormiqBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    if (isOwner) "Owner" else "Tenant",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = DormiqBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "+91 ${user.phone} · ${user.email ?: "no-email@dormiq.com"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = DormiqTextSecondary
                            )

                            if (!isOwner) {
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier
                                        .background(DormiqTealGlow, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Bed, null, tint = DormiqTeal, modifier = Modifier.size(14.dp))
                                    Text("Room ${user.roomNumber ?: "N/A"} · Bed ${user.bedLabel ?: "N/A"}", style = MaterialTheme.typography.labelSmall, color = DormiqTeal)
                                }
                            }
                        }
                    }
                }

                // Account Settings
                item {
                    SettingsSection(title = "Account") {
                        SettingsItem(Icons.Default.Person, "Edit Profile", onClick = onEditProfile)
                        if (isOwner) {
                            SettingsItem(Icons.Default.Apartment, "Manage Properties", onClick = onNavigateToManageProperties)
                            SettingsItem(Icons.Default.Settings, "Hostel Settings", onClick = { activeDialog = "Hostel Settings" })
                        }
                    }
                }

                // Notifications
                item {
                    SettingsSection(title = "Notifications") {
                        SettingsToggle(Icons.Default.Notifications, "Push Notifications", notificationsEnabled) { notificationsEnabled = it }
                        SettingsToggle(Icons.Outlined.Email, "Email Alerts", emailAlertsEnabled) { emailAlertsEnabled = it }
                    }
                }

                // App Settings
                item {
                    SettingsSection(title = "App Settings") {
                        SettingsItem(Icons.Default.Language, "Language — English", onClick = { activeDialog = "Language" })
                        SettingsItem(Icons.Default.HelpOutline, "Help & Support", onClick = { activeDialog = "Help & Support" })
                        SettingsItem(Icons.Default.PrivacyTip, "Privacy Policy", onClick = { activeDialog = "Privacy Policy" })
                        SettingsItem(Icons.Default.Info, "About Dormiq v1.0", onClick = { activeDialog = "About Dormiq" })
                    }
                }

                // Logout
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DormiqRedGlow, RoundedCornerShape(14.dp))
                            .border(1.dp, DormiqRed.copy(0.3f), RoundedCornerShape(14.dp))
                            .clickable { showLogoutDialog = true }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.Logout, null, tint = DormiqRed, modifier = Modifier.size(20.dp))
                            Text("Logout", style = MaterialTheme.typography.titleSmall, color = DormiqRed, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(title, style = MaterialTheme.typography.labelMedium, color = DormiqTextSecondary, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DormiqCard, RoundedCornerShape(14.dp))
                .border(1.dp, DormiqBorder, RoundedCornerShape(14.dp))
        ) {
            content()
        }
    }
}

@Composable
fun SettingsItem(icon: ImageVector, label: String, subtitle: String = "", onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = DormiqBlue, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = DormiqText)
            if (subtitle.isNotEmpty()) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = DormiqTextSecondary)
        }
        Icon(Icons.Default.ChevronRight, null, tint = DormiqTextMuted, modifier = Modifier.size(18.dp))
    }
}

@Composable
fun SettingsToggle(icon: ImageVector, label: String, value: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = DormiqBlue, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = DormiqText, modifier = Modifier.weight(1f))
        Switch(
            checked = value,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = androidx.compose.ui.graphics.Color.White,
                checkedTrackColor = DormiqBlue,
                uncheckedThumbColor = DormiqTextMuted,
                uncheckedTrackColor = DormiqBorder
            )
        )
    }
}

@Preview
@Composable
fun ProfileSettingsScreenPreview() {
    DormiqTheme {
        ProfileSettingsScreen({}, {}, {})
    }
}
