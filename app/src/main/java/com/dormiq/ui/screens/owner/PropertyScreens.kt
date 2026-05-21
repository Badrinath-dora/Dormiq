package com.dormiq.ui.screens.owner

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dormiq.domain.model.*
import com.dormiq.ui.components.*
import com.dormiq.ui.theme.*

import androidx.hilt.navigation.compose.hiltViewModel
import com.dormiq.ui.viewmodel.PropertyViewModel

// ─── Floor List ───────────────────────────────────────────────────────────────

@Composable
fun FloorListScreen(
    hostelId: String,
    onFloorClick: (floorId: String) -> Unit,
    onBack: () -> Unit,
    viewModel: PropertyViewModel = hiltViewModel()
) {
    val floors by viewModel.floors.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(hostelId) {
        viewModel.loadFloors(hostelId)
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var newFloorName by remember { mutableStateOf("") }

    Scaffold(
        containerColor = DormiqNavy,
        topBar = { DormiqTopBar(title = "Floors", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = DormiqBlue,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->
        if (isLoading && floors.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = DormiqBlue)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Summary
                item {
                    DormiqCard(modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceAround) {
                            SimpleStat("${floors.size}", "Floors")
                            SimpleStat("${floors.sumOf { it.totalRooms }}", "Rooms")
                            SimpleStat("${floors.sumOf { it.totalBeds }}", "Total Beds")
                            SimpleStat("${floors.sumOf { it.occupiedBeds }}", "Occupied")
                        }
                    }
                }

                items(floors) { floor ->
                    val occupancyPercent = if (floor.totalBeds > 0)
                        (floor.occupiedBeds.toFloat() / floor.totalBeds) * 100f else 0f

                    DormiqCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onFloorClick(floor.id) }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(48.dp).background(DormiqBlueGlow, RoundedCornerShape(12.dp)),
                                Alignment.Center
                            ) {
                                Icon(Icons.Default.Layers, null, tint = DormiqBlue, modifier = Modifier.size(24.dp))
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(floor.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = DormiqText)
                                Text("${floor.totalRooms} rooms · ${floor.totalBeds} beds", style = MaterialTheme.typography.bodySmall, color = DormiqTextSecondary)
                                Spacer(Modifier.height(8.dp))
                                OccupancyBar(percent = occupancyPercent)
                            }
                            Spacer(Modifier.width(12.dp))
                            Icon(Icons.Default.ChevronRight, null, tint = DormiqTextMuted)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New Floor", color = DormiqText) },
            text = {
                DormiqTextField(
                    value = newFloorName,
                    onValueChange = { newFloorName = it },
                    label = "Floor Name",
                    placeholder = "e.g. 3rd Floor"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newFloorName.isNotBlank()) {
                        viewModel.addFloor(hostelId, newFloorName, floors.size)
                        newFloorName = ""
                        showAddDialog = false
                    }
                }) {
                    Text("Add", color = DormiqBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = DormiqTextSecondary)
                }
            },
            containerColor = DormiqCard
        )
    }
}

// ─── Room List ────────────────────────────────────────────────────────────────

@Composable
fun RoomListScreen(
    hostelId: String,
    floorId: String,
    onRoomClick: (roomId: String) -> Unit,
    onBack: () -> Unit,
    viewModel: PropertyViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Vacant", "Full", "Maintenance")

    val rooms by viewModel.rooms.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(floorId) {
        viewModel.loadRooms(hostelId, floorId)
    }

    val filteredRooms = rooms.filter { room ->
        val matchesSearch = room.roomNumber.contains(searchQuery, true) || room.type.contains(searchQuery, true)
        val matchesFilter = when (selectedFilter) {
            "Vacant" -> room.occupiedBeds < room.totalBeds
            "Full" -> room.occupiedBeds == room.totalBeds
            "Maintenance" -> room.status == RoomStatus.MAINTENANCE
            else -> true
        }
        matchesSearch && matchesFilter
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var newRoomNumber by remember { mutableStateOf("") }
    var newRoomType by remember { mutableStateOf("Standard") }
    var newCapacity by remember { mutableStateOf("2") }

    val displayTitle = rooms.firstOrNull()?.floorName?.let { "$it Rooms" } ?: "Rooms"

    Scaffold(
        containerColor = DormiqNavy,
        topBar = { DormiqTopBar(title = displayTitle, onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = DormiqBlue,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->
        if (isLoading && rooms.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = DormiqBlue)
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                // Search & Filter
                Column(modifier = Modifier.padding(16.dp)) {
                    DormiqTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = "Search rooms...",
                        leadingIcon = Icons.Default.Search,
                        trailingIcon = if (searchQuery.isNotEmpty()) {{
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, null, tint = DormiqTextSecondary)
                            }
                        }} else null
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        filters.forEach { filter ->
                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = { selectedFilter = filter },
                                label = { Text(filter) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DormiqBlueGlow,
                                    selectedLabelColor = DormiqBlue,
                                    containerColor = DormiqCard,
                                    labelColor = DormiqTextSecondary
                                )
                            )
                        }
                    }
                }

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredRooms) { room ->
                        val vacantBeds = room.totalBeds - room.occupiedBeds
                        DormiqCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onRoomClick(room.id) },
                            glowColor = if (room.status == RoomStatus.MAINTENANCE) DormiqRedGlow else Color.Transparent
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier.size(52.dp)
                                        .background(
                                            if (room.status == RoomStatus.MAINTENANCE) DormiqRedGlow else DormiqBlueGlow,
                                            RoundedCornerShape(12.dp)
                                        ),
                                    Alignment.Center
                                ) {
                                    Text(room.roomNumber, style = MaterialTheme.typography.titleSmall, color = if (room.status == RoomStatus.MAINTENANCE) DormiqRed else DormiqBlue, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Room ${room.roomNumber}", style = MaterialTheme.typography.bodyLarge, color = DormiqText, fontWeight = FontWeight.SemiBold)
                                        Spacer(Modifier.width(8.dp))
                                        StatusChip(if (room.status == RoomStatus.MAINTENANCE) "maintenance" else if (vacantBeds == 0) "occupied" else "vacant")
                                    }
                                    Text("${room.type} · ${room.bathroomType} Bath", style = MaterialTheme.typography.bodySmall, color = DormiqTextSecondary)
                                    Spacer(Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        MiniStat("${room.occupiedBeds}", "Occupied", DormiqBlue)
                                        MiniStat("$vacantBeds", "Vacant", DormiqGreen)
                                        MiniStat("${room.totalBeds}", "Total", DormiqTextSecondary)
                                    }
                                }
                                Icon(Icons.Default.ChevronRight, null, tint = DormiqTextMuted)
                            }
                        }
                    }

                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New Room", color = DormiqText) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DormiqTextField(value = newRoomNumber, onValueChange = { newRoomNumber = it }, label = "Room Number", placeholder = "e.g. 301")
                    DormiqTextField(value = newRoomType, onValueChange = { newRoomType = it }, label = "Room Type", placeholder = "e.g. Deluxe")
                    DormiqTextField(value = newCapacity, onValueChange = { newCapacity = it }, label = "Capacity (Beds)", placeholder = "e.g. 2")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newRoomNumber.isNotBlank()) {
                        viewModel.addRoom(hostelId, floorId, newRoomNumber, newRoomType, newCapacity.toIntOrNull() ?: 2)
                        newRoomNumber = ""
                        showAddDialog = false
                    }
                }) {
                    Text("Add", color = DormiqBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = DormiqTextSecondary)
                }
            },
            containerColor = DormiqCard
        )
    }
}

@Composable
fun SimpleStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DormiqText)
        Text(label, style = MaterialTheme.typography.labelSmall, color = DormiqTextSecondary)
    }
}

@Composable
fun MiniStat(value: String, label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        Box(Modifier.size(6.dp).background(color, CircleShape))
        Text("$value $label", style = MaterialTheme.typography.labelSmall, color = DormiqTextSecondary)
    }
}

@Preview
@Composable
fun FloorListScreenPreview() {
    DormiqTheme {
        FloorListScreen("hostel-001", {}, {})
    }
}

@Preview
@Composable
fun RoomListScreenPreview() {
    DormiqTheme {
        RoomListScreen("hostel-001", "floor-g", {}, {})
    }
}
