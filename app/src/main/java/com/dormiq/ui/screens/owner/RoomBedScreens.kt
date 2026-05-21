package com.dormiq.ui.screens.owner

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dormiq.domain.model.*
import com.dormiq.ui.components.*
import com.dormiq.ui.theme.*

// ─── Room Detail Screen ───────────────────────────────────────────────────────

@Composable
fun RoomDetailScreen(
    roomId: String,
    isOwner: Boolean = true,
    onBedClick: (bedId: String) -> Unit,
    onReportIssue: () -> Unit,
    onBack: () -> Unit,
    viewModel: com.dormiq.ui.viewmodel.PropertyViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    issueViewModel: com.dormiq.ui.viewmodel.IssueViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val room by viewModel.currentRoom.collectAsState()
    val beds by viewModel.beds.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    val allIssues by issueViewModel.issues.collectAsState()

    LaunchedEffect(roomId) {
        viewModel.loadRoomDetails(roomId)
    }
    
    LaunchedEffect(room) {
        if (room != null) {
            issueViewModel.loadIssues(room!!.hostelId, isOwner = true)
        }
    }

    if (room == null) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator(color = DormiqBlue)
        }
        return
    }

    val safeRoom = room!!
    val actualOccupied = beds.count { it.status == BedStatus.OCCUPIED }
    val actualTotal = beds.size
    val rentPerBed = beds.firstOrNull()?.price ?: 0.0
    val roomIssues = allIssues.filter { it.roomId == roomId }

    Scaffold(
        containerColor = DormiqNavy,
        topBar = {
            DormiqTopBar(
                title = "Room ${safeRoom.roomNumber}",
                onBack = onBack,
                actions = {
                    if (isOwner) {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Edit, null, tint = DormiqText)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onReportIssue,
                containerColor = DormiqBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ReportProblem, null)
                    if (!isOwner) {
                        Spacer(Modifier.width(8.dp))
                        Text("Report Issue", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Room info card
            item {
                DormiqCard(modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                        Column {
                            Text("Room ${safeRoom.roomNumber}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = DormiqText)
                            Text("${safeRoom.floorName ?: ""} · ${safeRoom.type}", style = MaterialTheme.typography.bodyMedium, color = DormiqTextSecondary)
                        }
                        StatusChip(safeRoom.status)
                    }
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = DormiqDivider)
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceAround) {
                        SimpleStat("${actualTotal}", "Total")
                        SimpleStat("${actualOccupied}", "Occupied")
                        SimpleStat("${actualTotal - actualOccupied}", "Vacant")
                        SimpleStat("₹${rentPerBed.toInt()}", "Rent/Bed")
                    }
                    Spacer(Modifier.height(12.dp))
                    OccupancyBar(if (actualTotal > 0) (actualOccupied.toFloat() / actualTotal) * 100f else 0f)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        InfoChip(Icons.Default.Bathroom, safeRoom.bathroomType)
                        InfoChip(Icons.Default.Bed, "${safeRoom.capacity} Beds")
                        if (!safeRoom.notes.isNullOrEmpty()) InfoChip(Icons.Default.Notes, safeRoom.notes!!)
                    }
                }
            }

            // Bed Layout
            item {
                Text("Bed Layout", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DormiqText)
                Spacer(Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val chunkedBeds = beds.chunked(2)
                    chunkedBeds.forEach { rowBeds ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowBeds.forEach { bed ->
                                Box(modifier = Modifier.weight(1f)) {
                                    BedGridItem(
                                        label = bed.label,
                                        status = bed.status ?: BedStatus.VACANT,
                                        tenantName = bed.currentTenantName ?: "",
                                        onClick = { if (isOwner) onBedClick(bed.id) }
                                    )
                                }
                            }
                            if (rowBeds.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Issues for this room
            if (isOwner) {
                item {
                    SectionHeader("Room Issues")
                    Spacer(Modifier.height(8.dp))
                    
                    if (roomIssues.isEmpty()) {
                        DormiqCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.CheckCircle, null, tint = DormiqGreen, modifier = Modifier.size(32.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("No open issues!", style = MaterialTheme.typography.titleSmall, color = DormiqText)
                                Text("This room is in perfect condition", style = MaterialTheme.typography.bodySmall, color = DormiqTextSecondary)
                            }
                        }
                    } else {
                        roomIssues.forEach { issue ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                                    .background(DormiqCard, RoundedCornerShape(10.dp))
                                    .border(1.dp, DormiqBorder, RoundedCornerShape(10.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ReportProblem, null, tint = DormiqRed, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(10.dp))
                                val issueTitle = issue.description.takeIf { !it.isNullOrBlank() } ?: issue.category.replaceFirstChar { it.uppercase() }
                                Text(issueTitle, style = MaterialTheme.typography.bodyMedium, color = DormiqText, modifier = Modifier.weight(1f), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                StatusChip(issue.status)
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(140.dp)) }
        }
    }
}

@Composable
fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(
        modifier = Modifier
            .background(DormiqSurface, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, null, tint = DormiqTextSecondary, modifier = Modifier.size(12.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = DormiqTextSecondary)
    }
}

// ─── Bed Detail Screen ────────────────────────────────────────────────────────

@Composable
fun BedDetailScreen(
    bedId: String,
    onBack: () -> Unit,
    onAssignTenant: () -> Unit = {},
    propertyViewModel: com.dormiq.ui.viewmodel.PropertyViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    tenantViewModel: com.dormiq.ui.viewmodel.TenantViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    LaunchedEffect(bedId) {
        propertyViewModel.loadBed(bedId)
    }
    
    val currentBedState by propertyViewModel.currentBed.collectAsState()
    val bedStatus = currentBedState?.status ?: BedStatus.VACANT
    val tenant by propertyViewModel.currentTenant.collectAsState()
    val bedAllocations by propertyViewModel.bedAllocations.collectAsState()
    val isLoading by propertyViewModel.isLoading.collectAsState()

    val currentBed = currentBedState
    if (currentBed == null || isLoading) {
        Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = DormiqBlue) }
        return
    }

    Scaffold(
        containerColor = DormiqNavy,
        topBar = {
            DormiqTopBar(
                title = "Bed Detail",
                onBack = onBack,
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Edit, null, tint = DormiqText)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Bed status header
            item {
                DormiqCard(
                    modifier = Modifier.fillMaxWidth(),
                    glowColor = if (bedStatus == BedStatus.OCCUPIED) DormiqBlueGlow else DormiqGreenGlow
                ) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                        Column {
                            Text(currentBed.label, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = DormiqText)
                            Text("Rent/Month", style = MaterialTheme.typography.bodyMedium, color = DormiqTextSecondary)
                        }
                        StatusChip(bedStatus)
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("₹${(currentBed.price ?: 0.0).toInt()}", style = MaterialTheme.typography.titleMedium, color = DormiqBlue, fontWeight = FontWeight.Bold)
                }
            }

            // Current occupant
            if (bedStatus == BedStatus.OCCUPIED && tenant != null) {
                item {
                    Text("Current Occupant", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = DormiqText)
                    Spacer(Modifier.height(8.dp))
                    DormiqCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AvatarCircle(tenant?.name ?: "", size = 52.dp)
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(tenant?.name ?: "", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = DormiqText)
                                Text(tenant?.phone ?: "", style = MaterialTheme.typography.bodySmall, color = DormiqTextSecondary)
                            }
                            StatusChip("active")
                        }
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = DormiqDivider)
                        Spacer(Modifier.height(12.dp))
                        
                        val rawDate = tenant?.joiningDate ?: ""
                        val checkInDate = if (rawDate.length >= 10) rawDate.substring(0, 10) else "Unknown"

                        InfoRow("Check-in", checkInDate, Icons.Default.Login)
                        InfoRow("ID Type", tenant?.idType ?: "Not Provided", Icons.Default.Badge)
                        if (!tenant?.idNumber.isNullOrEmpty()) {
                            InfoRow("ID Number", tenant?.idNumber ?: "", Icons.Default.CreditCard)
                        }
                    }
                }
            }

            // Bed Actions (owner only)
            item {
                Text("Actions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = DormiqText)
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (bedStatus == BedStatus.OCCUPIED) {
                        DormiqSecondaryButton(
                            text = "Checkout Tenant",
                            onClick = {
                                tenant?.id?.let { tenantId ->
                                    propertyViewModel.checkoutTenant(tenantId, bedId)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Default.Logout
                        )
                    }
                    if (bedStatus == BedStatus.VACANT) {
                        DormiqPrimaryButton(
                            text = "Assign Tenant",
                            onClick = onAssignTenant,
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Default.PersonAdd
                        )
                    }
                    DormiqSecondaryButton(
                        text = if (bedStatus == BedStatus.BLOCKED) "Unblock Bed" else "Block Bed",
                        onClick = {
                            val newStatus = if (bedStatus == BedStatus.BLOCKED) BedStatus.VACANT else BedStatus.BLOCKED
                            propertyViewModel.updateBedStatus(bedId, newStatus)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        icon = if (bedStatus == BedStatus.BLOCKED) Icons.Default.LockOpen else Icons.Default.Block
                    )
                }
            }

            // Allocation History
            item {
                SectionHeader("Allocation History")
                Spacer(Modifier.height(8.dp))
                
                if (bedAllocations.isEmpty()) {
                    DormiqCard(modifier = Modifier.fillMaxWidth()) {
                        EmptyState(
                            icon = Icons.Default.History,
                            title = "No History",
                            subtitle = "No tenants have been allocated to this bed yet.",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        bedAllocations.forEach { (alloc, t) ->
                            val tenantName = t?.name ?: "Unknown Tenant"
                            val checkIn = if (alloc.checkInDate.length >= 10) alloc.checkInDate.substring(0, 10) else alloc.checkInDate
                            val checkOut = if (alloc.isActive) "Present" else {
                                val outDate = alloc.checkOutDate ?: ""
                                if (outDate.length >= 10) outDate.substring(0, 10) else outDate.ifBlank { "N/A" }
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                    .background(DormiqCard, RoundedCornerShape(10.dp))
                                    .border(1.dp, DormiqBorder, RoundedCornerShape(10.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AvatarCircle(tenantName, size = 36.dp)
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(tenantName, style = MaterialTheme.typography.bodyMedium, color = DormiqText, fontWeight = FontWeight.Medium)
                                    Text("$checkIn → $checkOut", style = MaterialTheme.typography.bodySmall, color = DormiqTextSecondary)
                                }
                                if (alloc.isActive) StatusChip("active") else StatusChip("checked_out")
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(140.dp)) }
        }
    }
}

@Preview
@Composable
fun RoomDetailScreenPreview() {
    DormiqTheme {
        RoomDetailScreen("room-g01", true, {}, {}, {})
    }
}

@Preview
@Composable
fun BedDetailScreenPreview() {
    DormiqTheme {
        BedDetailScreen("bed-g01a", {})
    }
}
