package com.dormiq.ui.screens.owner

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dormiq.domain.model.*
import com.dormiq.ui.components.*
import com.dormiq.ui.theme.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.dormiq.ui.viewmodel.DashboardViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@Composable
fun OwnerDashboardScreen(
    hostelId: String,
    onNavigateToFloorList: () -> Unit,
    onNavigateToRoomList: (floorId: String) -> Unit,
    onNavigateToTenantList: () -> Unit,
    onNavigateToIssueList: () -> Unit,
    onNavigateToAddTenant: () -> Unit,
    onNavigateToNoticeBoard: () -> Unit,
    onNavigateToRoomDetail: (String) -> Unit,
    onNavigateToBedDetail: (String) -> Unit,
    onNavigateToTenantProfile: (String) -> Unit,
    onLogout: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToFoodMenu: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val hostel by viewModel.hostel.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val issues by viewModel.issues.collectAsStateWithLifecycle()
    val layout by viewModel.hostelLayout.collectAsStateWithLifecycle()
    var selectedFloorId by remember { mutableStateOf("all") }
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadDashboardData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val greeting = remember {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        when (hour) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..20 -> "Good Evening"
            else -> "Good Night"
        }
    }

    Scaffold(
        containerColor = DormiqNavy,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            // FIXED Header with Status Bar Blend
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(GradientBlueStart, GradientBlueStart.copy(0.9f), DormiqNavy)
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("$greeting 👋", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.7f))
                            Text(hostel?.name ?: "Dormiq Hostel", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
                            
                            val address = hostel?.address ?: "Location not set"
                            val displayAddress = hostel?.address ?: "Location not set"
                            
                            Text(
                                text = displayAddress,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(0.6f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            HeaderIcon(icon = Icons.Outlined.Campaign, onClick = onNavigateToNoticeBoard)
                            HeaderIcon(icon = Icons.Outlined.Notifications, onClick = { /* Notifications */ })
                            HeaderIcon(icon = Icons.Default.Logout, containerColor = Color.White.copy(0.2f), onClick = onLogout)
                        }
                    }
                }
            }

            // Scrolling Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Occupancy Overview
                item {
                    DormiqCard(modifier = Modifier.fillMaxWidth(), glowColor = DormiqBlueGlow) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Occupancy Overview", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = DormiqText)
                            StatusChip(status = "active")
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            CircularStat("${stats.occupiedBeds}", "Occupied", DormiqBlue)
                            CircularStat("${stats.vacantBeds}", "Vacant", DormiqGreen)
                            CircularStat("${stats.blockedBeds}", "Blocked", DormiqAmber)
                            CircularStat("${stats.maintenanceBeds}", "Maintenance", DormiqRed)
                        }
                        Spacer(Modifier.height(16.dp))
                        OccupancyBar(percent = stats.occupancyPercent)
                    }
                }

                // Stats Grid
                item {
                    val gridStats = remember(stats) {
                        listOf(
                            Quad(Icons.Default.Layers, "${stats.totalFloors}", "Floors", DormiqBlue, DormiqBlueGlow),
                            Quad(Icons.Default.MeetingRoom, "${stats.totalRooms}", "Rooms", DormiqTeal, DormiqTealGlow),
                            Quad(Icons.Default.People, "${stats.totalTenants}", "Tenants", DormiqPurple, DormiqPurpleGlow),
                            Quad(Icons.Default.ReportProblem, "${stats.openIssues}", "Open Issues", DormiqRed, DormiqRedGlow),
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Quick Stats", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = DormiqText)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            gridStats.take(2).forEach { stat ->
                                StatCard(stat.second, stat.third, stat.first, stat.fourth, stat.fifth, modifier = Modifier.weight(1f))
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            gridStats.drop(2).forEach { stat ->
                                StatCard(stat.second, stat.third, stat.first, stat.fourth, stat.fifth, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                // Quick Actions
                item {
                    val row1 = remember {
                        listOf(
                            Triple("Add Tenant", Icons.Default.PersonAdd, onNavigateToAddTenant),
                            Triple("Food Menu", Icons.Default.Restaurant, onNavigateToFoodMenu),
                            Triple("Gallery", Icons.Default.PhotoLibrary, onNavigateToGallery),
                        )
                    }
                    val row2 = remember {
                        listOf(
                            Triple("View Issues", Icons.Default.ReportProblem, onNavigateToIssueList),
                            Triple("Floor View", Icons.Default.Layers, onNavigateToFloorList),
                            Triple("Post Notice", Icons.Default.Campaign, onNavigateToNoticeBoard),
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Quick Actions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = DormiqText)
                        
                        // Row 1
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row1.forEach { (label, icon, action) ->
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(DormiqCard, RoundedCornerShape(12.dp))
                                        .border(1.dp, DormiqBorder, RoundedCornerShape(12.dp))
                                        .clickable(onClick = action)
                                        .padding(12.dp),
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
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row2.forEach { (label, icon, action) ->
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(DormiqCard, RoundedCornerShape(12.dp))
                                        .border(1.dp, DormiqBorder, RoundedCornerShape(12.dp))
                                        .clickable(onClick = action)
                                        .padding(12.dp),
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
                    }
                }

                // ─── Real-time Room & Bed Matrix Header ───────────────────────────────
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Room & Bed Matrix", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = DormiqText)
                        Text(
                            "Manage Property",
                            style = MaterialTheme.typography.labelSmall,
                            color = DormiqBlue,
                            modifier = Modifier.clickable { onNavigateToFloorList() }
                        )
                    }
                }

                if (isLoading && layout.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().height(150.dp), Alignment.Center) {
                            CircularProgressIndicator(color = DormiqBlue)
                        }
                    }
                } else if (layout.isEmpty()) {
                    item {
                        DormiqCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.MeetingRoom, null, tint = DormiqTextSecondary, modifier = Modifier.size(48.dp))
                                Spacer(Modifier.height(12.dp))
                                Text("No Structure Created", style = MaterialTheme.typography.titleSmall, color = DormiqText)
                                Text("Add floors, rooms, and beds to start managing.", style = MaterialTheme.typography.bodySmall, color = DormiqTextSecondary)
                                Spacer(Modifier.height(16.dp))
                                DormiqPrimaryButton(text = "Add Floor Structure", onClick = onNavigateToFloorList)
                            }
                        }
                    }
                } else {
                    // Horizontal Floor Filter Tab Row
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // "All Floors" custom chip
                            val isAllSelected = selectedFloorId == "all"
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(if (isAllSelected) DormiqBlue else DormiqCard)
                                    .border(
                                        width = 1.dp,
                                        color = if (isAllSelected) DormiqBlue else DormiqBorder,
                                        shape = RoundedCornerShape(100.dp)
                                    )
                                    .clickable { selectedFloorId = "all" }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "All Floors",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isAllSelected) Color.White else DormiqTextSecondary
                                )
                            }

                            layout.forEach { floorWithRooms ->
                                val isSelected = selectedFloorId == floorWithRooms.floor.id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(100.dp))
                                        .background(if (isSelected) DormiqBlue else DormiqCard)
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) DormiqBlue else DormiqBorder,
                                            shape = RoundedCornerShape(100.dp)
                                        )
                                        .clickable { selectedFloorId = floorWithRooms.floor.id }
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = floorWithRooms.floor.name,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isSelected) Color.White else DormiqTextSecondary
                                    )
                                }
                            }
                        }
                    }

                    // Filtered rooms list as lazy items
                    val filteredRooms = if (selectedFloorId == "all") {
                        layout.flatMap { it.rooms }
                    } else {
                        layout.find { it.floor.id == selectedFloorId }?.rooms ?: emptyList()
                    }

                    if (filteredRooms.isEmpty()) {
                        item {
                            DormiqCard(modifier = Modifier.fillMaxWidth()) {
                                Box(Modifier.fillMaxWidth().padding(24.dp), Alignment.Center) {
                                    Text("No rooms on this floor yet.", style = MaterialTheme.typography.bodyMedium, color = DormiqTextSecondary)
                                }
                            }
                        }
                    } else {
                        items(filteredRooms) { roomWithBeds ->
                            DormiqCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToRoomDetail(roomWithBeds.room.id) }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Room ${roomWithBeds.room.roomNumber}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = DormiqText
                                        )
                                        Text(
                                            text = "${roomWithBeds.room.type} • ${roomWithBeds.room.bathroomType} Bathroom",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = DormiqTextSecondary
                                        )
                                    }
                                    // Status indicator
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (roomWithBeds.room.status == RoomStatus.ACTIVE) DormiqBlueGlow else DormiqRedGlow
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = roomWithBeds.room.status.uppercase(),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (roomWithBeds.room.status == RoomStatus.ACTIVE) DormiqBlue else DormiqRed
                                        )
                                    }
                                }

                                Spacer(Modifier.height(12.dp))
                                
                                // Bed Grid
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    roomWithBeds.beds.forEach { bedWithTenant ->
                                        val bed = bedWithTenant.bed
                                        val status = (bed.status ?: BedStatus.VACANT).lowercase()
                                        
                                        val borderStroke = when (status) {
                                            BedStatus.VACANT -> BorderStroke(1.dp, DormiqGreen.copy(0.4f))
                                            BedStatus.OCCUPIED -> null
                                            BedStatus.BLOCKED -> BorderStroke(1.dp, DormiqAmber.copy(0.4f))
                                            else -> BorderStroke(1.dp, DormiqRed.copy(0.4f))
                                        }
                                        
                                        val containerColor = when (status) {
                                            BedStatus.OCCUPIED -> DormiqBlue.copy(0.12f)
                                            else -> Color.Transparent
                                        }
                                        
                                        val textColor = when (status) {
                                            BedStatus.VACANT -> DormiqGreen
                                            BedStatus.OCCUPIED -> DormiqBlue
                                            BedStatus.BLOCKED -> DormiqAmber
                                            else -> DormiqRed
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(containerColor)
                                                .then(if (borderStroke != null) Modifier.border(borderStroke, RoundedCornerShape(10.dp)) else Modifier)
                                                .clickable {
                                                    if (status == BedStatus.OCCUPIED && bedWithTenant.tenantId != null) {
                                                        onNavigateToTenantProfile(bedWithTenant.tenantId)
                                                    } else {
                                                        onNavigateToAddTenant()
                                                    }
                                                }
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Bed,
                                                    contentDescription = null,
                                                    tint = textColor,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = bed.label,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = textColor
                                                )
                                                if (status == BedStatus.OCCUPIED && bedWithTenant.tenantName != null) {
                                                    Text(
                                                        text = "•",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = DormiqTextSecondary
                                                    )
                                                    Icon(
                                                        imageVector = Icons.Default.Person,
                                                        contentDescription = null,
                                                        tint = DormiqTextSecondary,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Text(
                                                        text = bedWithTenant.tenantName,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = DormiqText
                                                    )
                                                }
                                            }
                                            
                                            Text(
                                                text = when (status) {
                                                    BedStatus.VACANT -> "+ Allocate"
                                                    BedStatus.OCCUPIED -> "Occupied"
                                                    BedStatus.BLOCKED -> "Blocked"
                                                    else -> "Maintenance"
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = textColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Recent Issues
                item {
                    Column {
                        SectionHeader("Recent Issues", "View All", onNavigateToIssueList)
                        Spacer(Modifier.height(8.dp))
                        
                        if (issues.isEmpty()) {
                            DormiqCard(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.CheckCircle, null, tint = DormiqGreen, modifier = Modifier.size(32.dp))
                                    Spacer(Modifier.height(8.dp))
                                    Text("All caught up!", style = MaterialTheme.typography.titleSmall, color = DormiqText)
                                    Text("No issues reported yet", style = MaterialTheme.typography.bodySmall, color = DormiqTextSecondary)
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                issues.forEach { issue ->
                                    DormiqCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { 
                                                onNavigateToIssueList()
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .background(
                                                            when (issue.status.lowercase()) {
                                                                "open" -> DormiqRedGlow
                                                                "in_progress" -> DormiqAmberGlow
                                                                "resolved" -> DormiqGreenGlow
                                                                else -> DormiqTextSecondary.copy(alpha = 0.1f)
                                                            },
                                                            CircleShape
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = when (issue.category.lowercase()) {
                                                            "electricity" -> Icons.Default.FlashOn
                                                            "plumbing" -> Icons.Default.WaterDrop
                                                            "internet" -> Icons.Default.Wifi
                                                            "cleaning" -> Icons.Default.CleaningServices
                                                            else -> Icons.Default.ReportProblem
                                                        },
                                                        contentDescription = null,
                                                        tint = when (issue.status.lowercase()) {
                                                            "open" -> DormiqRed
                                                            "in_progress" -> DormiqAmber
                                                            "resolved" -> DormiqGreen
                                                            else -> DormiqTextSecondary
                                                        },
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Spacer(Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = issue.description?.takeIf { it.isNotEmpty() } ?: "No description provided",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = DormiqText,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = "Room ${issue.roomNumber} · Reported by ${issue.reporterName}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = DormiqTextSecondary
                                                    )
                                                }
                                            }
                                            StatusChip(status = issue.status)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                item { Spacer(Modifier.height(100.dp)) }
            }
        }
    }
}

private data class Quad<A, B, C, D, E>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E)

@Composable
private fun HeaderIcon(
    icon: ImageVector,
    containerColor: Color = Color.White.copy(0.12f),
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .background(containerColor, CircleShape)
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun CircularStat(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(color.copy(alpha = 0.12f), CircleShape)
                .border(2.dp, color.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = DormiqTextSecondary)
    }
}

@Preview
@Composable
fun OwnerDashboardScreenPreview() {
    DormiqTheme {
        OwnerDashboardScreen("hostel-001", {}, { _ -> }, {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
    }
}
