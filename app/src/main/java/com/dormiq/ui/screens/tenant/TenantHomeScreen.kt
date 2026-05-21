package com.dormiq.ui.screens.tenant

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dormiq.domain.model.*
import com.dormiq.ui.components.*
import com.dormiq.ui.screens.issues.*
import com.dormiq.ui.screens.notices.*
import com.dormiq.ui.theme.*

// ─── Tenant Home ──────────────────────────────────────────────────────────────

import androidx.hilt.navigation.compose.hiltViewModel
import com.dormiq.ui.viewmodel.TenantViewModel
import com.dormiq.ui.viewmodel.AuthViewModel
import com.dormiq.ui.viewmodel.AuthState

// ─── Tenant Home ──────────────────────────────────────────────────────────────

@Composable
fun TenantHomeScreen(
    onNavigateToMyRoom: () -> Unit,
    onNavigateToIssues: () -> Unit,
    onNavigateToNotices: () -> Unit,
    onNavigateToReportIssue: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToFoodMenu: () -> Unit,
    tenantViewModel: TenantViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val user = (authState as? AuthState.Authenticated)?.user
    val tenant by tenantViewModel.tenant.collectAsState()
    val issues by tenantViewModel.issues.collectAsState()
    val notices by tenantViewModel.notices.collectAsState()
    val isLoading by tenantViewModel.isLoading.collectAsState()

    LaunchedEffect(user?.id) {
        if (user != null) {
            tenantViewModel.loadTenantData(user.id, user.phone)
        }
    }

    LaunchedEffect(user?.id, tenant?.hostelId) {
        val uId = user?.id
        val hId = tenant?.hostelId
        if (uId != null && hId != null) {
            tenantViewModel.loadHomeData(hId, uId)
        }
    }

    Scaffold(
        containerColor = DormiqNavy,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        if (isLoading && tenant == null) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = DormiqBlue)
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())) {
                // Fixed Greeting Header & Room Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(GradientBlueStart, DormiqNavy),
                                start = Offset(0f, 0f),
                                end = Offset(0f, 600f)
                            )
                        )
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Hello, ${tenant?.name?.split(" ")?.first() ?: "User"} 👋", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Welcome back to Dormiq", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(0.8f))
                            }
                            IconButton(
                                onClick = onLogout,
                                modifier = Modifier.background(Color.White, CircleShape)
                            ) {
                                Icon(Icons.Default.Logout, null, tint = DormiqRed, modifier = Modifier.size(20.dp))
                            }
                        }
                        
                        Spacer(Modifier.height(24.dp))

                        // Room Card inline
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DormiqCard.copy(0.7f), RoundedCornerShape(14.dp))
                                .border(1.dp, DormiqBlueDark, RoundedCornerShape(14.dp))
                                .clickable(onClick = onNavigateToMyRoom)
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(44.dp).background(DormiqBlueGlow, RoundedCornerShape(10.dp)), Alignment.Center) {
                                    Icon(Icons.Default.Bed, null, tint = DormiqBlue, modifier = Modifier.size(22.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text("Your Bed", style = MaterialTheme.typography.labelSmall, color = DormiqTextSecondary)
                                    Text("Room ${tenant?.assignedRoomNumber ?: "Not Assigned"} · ${tenant?.assignedBedLabel ?: "N/A"}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = DormiqText)
                                    Text(tenant?.assignedFloorName ?: "Assigned Floor", style = MaterialTheme.typography.bodySmall, color = DormiqTextSecondary)
                                }
                                Icon(Icons.Default.ChevronRight, null, tint = DormiqBlue)
                            }
                        }
                    }
                }

                // Scrollable Content starting from Quick Actions
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    // Quick Actions
                    item {
                        Spacer(Modifier.height(16.dp))
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Row 1
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                listOf(
                                    Triple("My Room", Icons.Default.Bed, onNavigateToMyRoom),
                                    Triple("Food Menu", Icons.Default.Restaurant, onNavigateToFoodMenu),
                                    Triple("Gallery", Icons.Default.PhotoLibrary, onNavigateToGallery),
                                ).forEach { (label, icon, action) ->
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(DormiqCard, RoundedCornerShape(12.dp))
                                            .border(1.dp, DormiqBorder, RoundedCornerShape(12.dp))
                                            .clickable(onClick = action)
                                            .padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(Modifier.size(40.dp).background(DormiqBlueGlow, RoundedCornerShape(10.dp)), Alignment.Center) {
                                            Icon(icon, null, tint = DormiqBlue, modifier = Modifier.size(20.dp))
                                        }
                                        Spacer(Modifier.height(6.dp))
                                        Text(label, style = MaterialTheme.typography.labelSmall, color = DormiqText, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                            // Row 2
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                listOf(
                                    Triple("Report Issue", Icons.Default.ReportProblem, onNavigateToReportIssue),
                                    Triple("Notices", Icons.Default.Campaign, onNavigateToNotices),
                                ).forEach { (label, icon, action) ->
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(DormiqCard, RoundedCornerShape(12.dp))
                                            .border(1.dp, DormiqBorder, RoundedCornerShape(12.dp))
                                            .clickable(onClick = action)
                                            .padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(Modifier.size(40.dp).background(DormiqBlueGlow, RoundedCornerShape(10.dp)), Alignment.Center) {
                                            Icon(icon, null, tint = DormiqBlue, modifier = Modifier.size(20.dp))
                                        }
                                        Spacer(Modifier.height(6.dp))
                                        Text(label, style = MaterialTheme.typography.labelSmall, color = DormiqText, fontWeight = FontWeight.Medium)
                                    }
                                }
                                // Empty spacer to balance the Row
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    // My Issues
                    item {
                        Spacer(Modifier.height(20.dp))
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            SectionHeader("My Issues", "View All", onNavigateToIssues)
                        }
                        Spacer(Modifier.height(8.dp))
                        if (issues.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                EmptyState(
                                    icon = Icons.Default.CheckCircle,
                                    title = "No Issues",
                                    subtitle = "You haven't reported any issues.",
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                            }
                        } else {
                            issues.take(3).forEach { issue ->
                                Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                                    IssueCard(issue, onClick = onNavigateToIssues)
                                }
                            }
                        }
                    }

                    // Notices
                    item {
                        Spacer(Modifier.height(20.dp))
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            SectionHeader("Latest Notices", "View All", onNavigateToNotices)
                        }
                        Spacer(Modifier.height(8.dp))
                        if (notices.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                EmptyState(
                                    icon = Icons.Default.Campaign,
                                    title = "No Notices",
                                    subtitle = "No new notices from the management.",
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                            }
                        } else {
                            notices.take(3).forEach { notice ->
                                Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                                    NoticeCard(notice, isOwner = false)
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

// ─── Tenant My Room ───────────────────────────────────────────────────────────

@Composable
fun TenantMyRoomScreen(
    onReportIssue: () -> Unit,
    tenantViewModel: TenantViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val user = (authState as? AuthState.Authenticated)?.user
    val tenant by tenantViewModel.tenant.collectAsState()
    val room by tenantViewModel.room.collectAsState()
    val roommates by tenantViewModel.roommates.collectAsState()
    val allocation by tenantViewModel.allocation.collectAsState()
    val isLoading by tenantViewModel.isLoading.collectAsState()

    // Ensure data is loaded when this screen is shown
    LaunchedEffect(user?.id) {
        if (user != null && tenant == null) {
            tenantViewModel.loadTenantData(user.id, user.phone)
        }
    }

    Scaffold(
        containerColor = DormiqNavy,
        floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = onReportIssue,
                    containerColor = DormiqBlue,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.ReportProblem, null) },
                    text = { Text("Report Issue", color = Color.White) },
                    shape = RoundedCornerShape(14.dp)
                )
            }
    ) { padding ->
        if (isLoading && tenant == null) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = DormiqBlue)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("My Room", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = DormiqText)
                    Text(allocation?.floorName ?: tenant?.assignedFloorName ?: "Room Details", style = MaterialTheme.typography.bodyMedium, color = DormiqTextSecondary)
                }

                // Assignment card
                item {
                    DormiqCard(modifier = Modifier.fillMaxWidth(), glowColor = DormiqBlueGlow) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Room Details", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = DormiqText)
                            StatusChip(tenant?.status ?: "active")
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceAround) {
                            RoomInfoStat(allocation?.roomNumber ?: tenant?.assignedRoomNumber ?: "Unassigned", "Room")
                            RoomInfoStat(allocation?.bedLabel ?: tenant?.assignedBedLabel ?: "N/A", "Your Bed")
                            RoomInfoStat(allocation?.floorName ?: tenant?.assignedFloorName ?: "N/A", "Floor")
                            RoomInfoStat("${room?.capacity ?: "N/A"}", "Capacity")
                        }
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = DormiqDivider)
                        Spacer(Modifier.height(12.dp))
                        InfoRow("Check-in Date", allocation?.checkInDate ?: tenant?.joiningDate ?: "N/A", Icons.Default.Login)
                        InfoRow("ID Type", tenant?.idType ?: "Aadhaar", Icons.Default.Badge)
                        InfoRow("Contact", tenant?.phone ?: "N/A", Icons.Default.Phone)
                    }
                }

                // Roommates
                item {
                    SectionLabel("Roommates")
                    Spacer(Modifier.height(8.dp))
                    if (roommates.isEmpty()) {
                        Text("No roommates yet", color = DormiqTextSecondary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 8.dp))
                    } else {
                        roommates.forEach { roommate ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                    .background(DormiqCard, RoundedCornerShape(10.dp))
                                    .border(1.dp, DormiqBorder, RoundedCornerShape(10.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AvatarCircle(roommate.name, size = 36.dp)
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(roommate.name, style = MaterialTheme.typography.bodyMedium, color = DormiqText, fontWeight = FontWeight.Medium)
                                    Text(roommate.assignedBedLabel ?: "N/A", style = MaterialTheme.typography.labelSmall, color = DormiqTextSecondary)
                                }
                                StatusChip(roommate.status)
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun RoomInfoStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DormiqBlue)
        Text(label, style = MaterialTheme.typography.labelSmall, color = DormiqTextSecondary)
    }
}

@Preview
@Composable
fun TenantHomeScreenPreview() {
    DormiqTheme {
        TenantHomeScreen({}, {}, {}, {}, {}, {}, {})
    }
}

@Preview
@Composable
fun TenantMyRoomScreenPreview() {
    DormiqTheme {
        TenantMyRoomScreen({})
    }
}
