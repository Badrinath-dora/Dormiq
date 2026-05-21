package com.dormiq.ui.screens.tenant_mgmt

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dormiq.domain.model.*
import com.dormiq.ui.components.*
import com.dormiq.ui.theme.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.dormiq.ui.viewmodel.TenantViewModel
import androidx.compose.runtime.collectAsState

@Composable
fun TenantProfileScreen(
    tenantId: String,
    isOwner: Boolean = true,
    onEdit: () -> Unit = {},
    onAssignBed: () -> Unit = {},
    onBack: () -> Unit,
    viewModel: TenantViewModel = hiltViewModel()
) {
    LaunchedEffect(tenantId) {
        viewModel.loadTenant(tenantId)
    }

    val tenant by viewModel.tenant.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        containerColor = DormiqNavy,
        topBar = {
            DormiqTopBar(
                title = "Tenant Profile",
                onBack = onBack,
                actions = {
                    if (isOwner && tenant?.status != "checked_out") {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, null, tint = DormiqText)
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = DormiqBlue)
            }
        } else if (tenant == null) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("Tenant not found", color = DormiqTextSecondary)
            }
        } else {
            val t = tenant!!
            val isCheckedOut = t.status == "checked_out"
            
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AvatarCircle(t.name, size = 80.dp)
                        Spacer(Modifier.height(12.dp))
                        Text(t.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = DormiqText)
                        Spacer(Modifier.height(4.dp))
                        StatusChip(t.status)
                        Spacer(Modifier.height(4.dp))
                        Text("Joined ${t.joiningDate}", style = MaterialTheme.typography.bodySmall, color = DormiqTextSecondary)
                    }
                }

                // Room Assignment Banner
                item {
                    DormiqCard(modifier = Modifier.fillMaxWidth(), glowColor = DormiqBlueGlow) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(44.dp).background(DormiqBlueGlow, RoundedCornerShape(10.dp)), Alignment.Center) {
                                Icon(Icons.Default.Bed, null, tint = DormiqBlue, modifier = Modifier.size(22.dp))
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Current Assignment", style = MaterialTheme.typography.labelMedium, color = DormiqTextSecondary)
                                val roomNo = t.assignedRoomNumber ?: "N/A"
                                val bedLabel = t.assignedBedLabel ?: "N/A"
                                Text("Room $roomNo · Bed $bedLabel", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = DormiqText)
                                Text(t.assignedFloorName ?: "Not assigned yet", style = MaterialTheme.typography.bodySmall, color = DormiqTextSecondary)
                            }
                            if (isOwner && !isCheckedOut) {
                                IconButton(onClick = onAssignBed) {
                                    Icon(Icons.Default.SwapHoriz, null, tint = DormiqBlue)
                                }
                            }
                        }
                    }
                }

                // Personal Details
                item {
                    SectionLabel("Personal Details")
                    Spacer(Modifier.height(8.dp))
                    DormiqCard(modifier = Modifier.fillMaxWidth()) {
                        InfoRow("Phone", t.phone, Icons.Default.Phone)
                        InfoRow("Email", t.email ?: "Not provided", Icons.Default.Email)
                        InfoRow("ID Type", t.idType ?: "Not provided", Icons.Default.Badge)
                        if (isOwner) InfoRow("ID Number", t.idNumber ?: "Not provided", Icons.Default.Numbers)
                        InfoRow("Joining Date", t.joiningDate ?: "Not provided", Icons.Default.DateRange)
                    }
                }

                // Emergency Contact
                if (isOwner) {
                    item {
                        SectionLabel("Emergency Contact")
                        Spacer(Modifier.height(8.dp))
                        DormiqCard(modifier = Modifier.fillMaxWidth()) {
                            InfoRow("Name", t.emergencyContactName ?: "Not provided", Icons.Default.Person)
                            InfoRow("Phone", t.emergencyContactPhone ?: "Not provided", Icons.Default.Phone)
                        }
                    }
                }

                // Issue History
                item {
                    SectionLabel("Issue History")
                    Spacer(Modifier.height(8.dp))
                    
                    val tenantIssues by viewModel.issues.collectAsState()
                    
                    if (tenantIssues.isEmpty()) {
                        DormiqCard(modifier = Modifier.fillMaxWidth()) {
                            Box(Modifier.fillMaxWidth().padding(20.dp), Alignment.Center) {
                                Text("No issues reported yet", color = DormiqTextSecondary, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    } else {
                        tenantIssues.forEach { issue ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                    .background(DormiqCard, RoundedCornerShape(10.dp))
                                    .border(1.dp, DormiqBorder, RoundedCornerShape(10.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ReportProblem, null, tint = if (issue.status == "open") DormiqRed else DormiqGreen, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(issue.description ?: issue.category, style = MaterialTheme.typography.bodyMedium, color = DormiqText)
                                    Text(issue.createdAt ?: "", style = MaterialTheme.typography.bodySmall, color = DormiqTextSecondary)
                                }
                                StatusChip(issue.status)
                            }
                        }
                    }
                }

                // Owner Actions
                if (isOwner) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (!isCheckedOut) {
                                DormiqSecondaryButton(
                                    "Checkout Tenant", 
                                    { viewModel.checkoutTenant(t.id) { onBack() } }, 
                                    modifier = Modifier.fillMaxWidth(), 
                                    icon = Icons.Default.Logout
                                )
                                DormiqSecondaryButton(
                                    "Change Room/Bed", 
                                    onAssignBed, 
                                    modifier = Modifier.fillMaxWidth(), 
                                    icon = Icons.Default.SwapHoriz
                                )
                            }
                            
                            // Delete button (Red)
                            OutlinedButton(
                                onClick = { viewModel.deleteTenant(t.id) { onBack() } },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DormiqRed),
                                border = BorderStroke(1.dp, DormiqRed.copy(0.3f))
                            ) {
                                Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Delete Tenant")
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Preview
@Composable
fun TenantProfileScreenPreview() {
    DormiqTheme {
        TenantProfileScreen("tenant-001", true, {}, {}, {})
    }
}
