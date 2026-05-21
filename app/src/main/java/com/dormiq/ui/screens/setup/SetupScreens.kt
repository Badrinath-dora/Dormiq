package com.dormiq.ui.screens.setup

import androidx.compose.animation.*
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.dormiq.domain.model.Floor
import com.dormiq.ui.components.*
import com.dormiq.ui.theme.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.dormiq.ui.viewmodel.SetupViewModel
import com.dormiq.ui.viewmodel.SetupState
import com.dormiq.ui.viewmodel.RoomDraft
import com.dormiq.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun AddFloorsScreen(
    hostelId: String,
    onNext: (hostelId: String, floorId: String) -> Unit,
    onBack: () -> Unit,
    viewModel: SetupViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()

    val floorNames = viewModel.floorNames
    var newFloorName by remember { mutableStateOf("") }
    var showAddField by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DormiqNavy,
        topBar = { DormiqTopBar(title = "Add Floors", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            SetupProgress(currentStep = 2, totalSteps = 5)

            Text("Define Floors", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = DormiqText)
            Spacer(Modifier.height(4.dp))
            Text("Add all floors in your hostel", style = MaterialTheme.typography.bodyMedium, color = DormiqTextSecondary)

            Spacer(Modifier.height(24.dp))

            floorNames.forEachIndexed { index, name ->
                AnimatedVisibility(visible = true, enter = slideInVertically() + fadeIn()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(DormiqCard, RoundedCornerShape(12.dp))
                            .border(1.dp, DormiqBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(DormiqBlueGlow, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${index + 1}", style = MaterialTheme.typography.labelLarge, color = DormiqBlue, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(name, style = MaterialTheme.typography.bodyLarge, color = DormiqText, modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = { floorNames.removeAt(index) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Delete, null, tint = DormiqRed, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Add floor button
            if (showAddField) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DormiqTextField(
                        value = newFloorName,
                        onValueChange = { newFloorName = it },
                        label = "Floor Name",
                        modifier = Modifier.weight(1f),
                        placeholder = "e.g., 3rd Floor"
                    )
                    IconButton(
                        onClick = {
                            if (newFloorName.isNotBlank()) {
                                floorNames.add(newFloorName)
                                newFloorName = ""
                                showAddField = false
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(DormiqBlue, RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.Check, null, tint = DormiqText)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            DormiqSecondaryButton(
                text = "Add Floor",
                onClick = { showAddField = !showAddField },
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Add
            )

            Spacer(Modifier.height(32.dp))

            DormiqPrimaryButton(
                text = "Next: Add Rooms",
                onClick = { onNext(hostelId, "temp-floor") },
                modifier = Modifier.fillMaxWidth(),
                enabled = floorNames.isNotEmpty(),
                icon = Icons.Default.ArrowForward
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun AddRoomsScreen(
    hostelId: String,
    floorId: String,
    onNext: (hostelId: String, roomId: String) -> Unit,
    onBack: () -> Unit,
    viewModel: SetupViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()

    val rooms = viewModel.rooms

    var roomNumber by remember { mutableStateOf("") }
    var roomType by remember { mutableStateOf("Standard") }
    var capacity by remember { mutableStateOf("4") }
    val roomTypes = listOf("Standard", "Deluxe", "Dormitory")

    Scaffold(
        containerColor = DormiqNavy,
        topBar = { DormiqTopBar(title = "Add Rooms", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            SetupProgress(currentStep = 3, totalSteps = 5)

            Text("Add Rooms", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = DormiqText)
            Spacer(Modifier.height(4.dp))
            Text("Define all rooms across your floors · ${rooms.size} added", style = MaterialTheme.typography.bodyMedium, color = DormiqTextSecondary)

            Spacer(Modifier.height(20.dp))

            // Add Room Form
            DormiqCard(modifier = Modifier.fillMaxWidth()) {
                Text("New Room", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = DormiqText)
                Spacer(Modifier.height(12.dp))

                DormiqTextField(
                    value = roomNumber,
                    onValueChange = { roomNumber = it },
                    label = "Room Number",
                    placeholder = "e.g., G01, 101",
                    leadingIcon = Icons.Default.MeetingRoom
                )

                Spacer(Modifier.height(10.dp))

                Text("Room Type", style = MaterialTheme.typography.labelMedium, color = DormiqTextSecondary)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    roomTypes.forEach { type ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (roomType == type) DormiqBlueGlow else DormiqSurface, RoundedCornerShape(8.dp))
                                .border(1.dp, if (roomType == type) DormiqBlue else DormiqBorder, RoundedCornerShape(8.dp))
                                .clickable { roomType = type }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(type, style = MaterialTheme.typography.labelSmall, color = if (roomType == type) DormiqBlue else DormiqTextSecondary)
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                DormiqTextField(
                    value = capacity,
                    onValueChange = { capacity = it },
                    label = "Bed Capacity",
                    keyboardType = KeyboardType.Number,
                    leadingIcon = Icons.Default.Bed
                )

                Spacer(Modifier.height(12.dp))

                DormiqPrimaryButton(
                    text = "Add Room",
                    onClick = {
                        if (roomNumber.isNotBlank()) {
                            rooms.add(RoomDraft(roomNumber, roomType, capacity.toIntOrNull() ?: 2))
                            roomNumber = ""
                            capacity = "4"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Default.Add
                )
            }

            Spacer(Modifier.height(16.dp))

            // Room List
            rooms.forEachIndexed { index, room ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(DormiqCard, RoundedCornerShape(12.dp))
                        .border(1.dp, DormiqBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(40.dp).background(DormiqBlueGlow, RoundedCornerShape(10.dp)),
                        Alignment.Center
                    ) {
                        Icon(Icons.Default.MeetingRoom, null, tint = DormiqBlue, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Room ${room.number}", style = MaterialTheme.typography.bodyLarge, color = DormiqText, fontWeight = FontWeight.Medium)
                        Text("${room.type} · ${room.capacity} beds", style = MaterialTheme.typography.bodySmall, color = DormiqTextSecondary)
                    }
                    IconButton(onClick = { rooms.removeAt(index) }, Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, null, tint = DormiqRed, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            DormiqPrimaryButton(
                text = "Next: Add Beds",
                onClick = { onNext(hostelId, "temp-room") },
                modifier = Modifier.fillMaxWidth(),
                enabled = rooms.isNotEmpty(),
                icon = Icons.Default.ArrowForward
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun AddBedsScreen(
    hostelId: String,
    roomId: String,
    onNext: (hostelId: String) -> Unit,
    onBack: () -> Unit,
    viewModel: SetupViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()

    val beds = viewModel.beds

    var newBedLabel by remember { mutableStateOf("") }
    val bedLabels = listOf("Bed A", "Bed B", "Bed C", "Bed D", "Upper Bunk", "Lower Bunk", "Bed E", "Bed F")

    Scaffold(
        containerColor = DormiqNavy,
        topBar = { DormiqTopBar(title = "Add Beds", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            SetupProgress(currentStep = 4, totalSteps = 5)

            Text("Add Beds", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = DormiqText)
            Spacer(Modifier.height(4.dp))
            Text("Define a standard layout for your rooms · ${beds.size} beds", style = MaterialTheme.typography.bodyMedium, color = DormiqTextSecondary)

            Spacer(Modifier.height(20.dp))
            
            SectionLabel("Standard Rent")
            Spacer(Modifier.height(8.dp))
            DormiqTextField(
                value = viewModel.bedPrice,
                onValueChange = { viewModel.bedPrice = it },
                label = "Amount per Bed (Monthly)",
                placeholder = "e.g. 7500",
                leadingIcon = Icons.Default.CurrencyRupee,
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
            )

            Spacer(Modifier.height(20.dp))
            SectionLabel("Bed Layout")

            // Quick add chips
            Text("Quick Add", style = MaterialTheme.typography.labelMedium, color = DormiqTextSecondary)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                bedLabels.forEach { label ->
                    val exists = beds.any { it == label }
                    FilterChip(
                        selected = exists,
                        onClick = {
                            if (exists) beds.removeIf { it == label }
                            else beds.add(label)
                        },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DormiqBlueGlow,
                            selectedLabelColor = DormiqBlue,
                            containerColor = DormiqCard,
                            labelColor = DormiqTextSecondary
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DormiqTextField(
                    value = newBedLabel,
                    onValueChange = { newBedLabel = it },
                    label = "Custom Bed Label",
                    modifier = Modifier.weight(1f),
                    placeholder = "e.g., Bed E"
                )
                IconButton(
                    onClick = { if (newBedLabel.isNotBlank()) { beds.add(newBedLabel); newBedLabel = "" } },
                    modifier = Modifier.size(48.dp).background(DormiqBlue, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.Add, null, tint = DormiqText)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Bed grid
            val columns = 2
            val rows = (beds.size + columns - 1) / columns
            repeat(rows) { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(columns) { col ->
                        val index = row * columns + col
                        if (index < beds.size) {
                            BedGridItem(
                                label = beds[index],
                                status = "vacant",
                                modifier = Modifier.weight(1f)
                            ) { beds.removeAt(index) }
                        } else {
                            Box(Modifier.weight(1f))
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(24.dp))

            DormiqPrimaryButton(
                text = "Next: Review Setup",
                onClick = { onNext(hostelId) },
                modifier = Modifier.fillMaxWidth(),
                enabled = beds.isNotEmpty(),
                icon = Icons.Default.ArrowForward
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun ReviewStructureScreen(
    hostelId: String,
    onConfirm: () -> Unit,
    onBack: () -> Unit,
    onEditStep: (Int) -> Unit = {},
    viewModel: SetupViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val setupState by viewModel.setupState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = DormiqNavy,
            topBar = { DormiqTopBar(title = "Review Setup", onBack = onBack) }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                SetupProgress(currentStep = 5, totalSteps = 5)

                Text("Review Your Hostel", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = DormiqText)
                Spacer(Modifier.height(4.dp))
                Text("Confirm everything looks correct before publishing", style = MaterialTheme.typography.bodyMedium, color = DormiqTextSecondary)

                Spacer(Modifier.height(24.dp))

                // Summary cards
                listOf(
                    Quadruple(Icons.Default.Apartment, "Hostel", viewModel.hostelName.ifEmpty { "Not named" }, 1),
                    Quadruple(Icons.Default.Layers, "Floors", "${viewModel.floorNames.size} floors configured", 2),
                    Quadruple(Icons.Default.MeetingRoom, "Rooms", "${viewModel.rooms.size} rooms defined", 3),
                    Quadruple(Icons.Default.Bed, "Beds", "${viewModel.beds.size} beds total", 4),
                ).forEach { (icon, label, value, step) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(DormiqCard, RoundedCornerShape(12.dp))
                            .border(1.dp, DormiqBorder, RoundedCornerShape(12.dp))
                            .clickable { onEditStep(step) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(44.dp).background(DormiqBlueGlow, RoundedCornerShape(10.dp)), Alignment.Center) {
                            Icon(icon, null, tint = DormiqBlue, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(label, style = MaterialTheme.typography.bodySmall, color = DormiqTextSecondary)
                            Text(value, style = MaterialTheme.typography.bodyLarge, color = DormiqText, fontWeight = FontWeight.SemiBold)
                        }
                        IconButton(onClick = { onEditStep(step) }) {
                            Icon(Icons.Default.EditNote, null, tint = DormiqBlue, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                DormiqCard(modifier = Modifier.fillMaxWidth(), glowColor = DormiqGreenGlow) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = DormiqGreen, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Your hostel structure is ready to publish. You can always edit it later from Settings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DormiqTextSecondary
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                if (setupState is SetupState.Error) {
                    Text((setupState as SetupState.Error).message, color = DormiqRed, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                }

                DormiqPrimaryButton(
                    text = "Publish Hostel",
                    onClick = {
                        viewModel.publishHostel {
                            authViewModel.refreshUser()
                            onConfirm()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Default.Publish,
                    enabled = setupState !is SetupState.Loading
                )

                Spacer(Modifier.height(12.dp))

                DormiqSecondaryButton(
                    text = "Go Back & Edit",
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(24.dp))
            }
        }

        if (setupState is SetupState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                DormiqCard(glowColor = DormiqBlueGlow) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        CircularProgressIndicator(color = DormiqBlue)
                        Spacer(Modifier.height(16.dp))
                        Text("Publishing Hostel...", color = DormiqText, fontWeight = FontWeight.Bold)
                        Text("Setting up floors, rooms, and beds", style = MaterialTheme.typography.bodySmall, color = DormiqTextSecondary)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun AddFloorsScreenPreview() {
    DormiqTheme {
        AddFloorsScreen("hostel-001", { _, _ -> }, {})
    }
}

@Preview
@Composable
fun AddRoomsScreenPreview() {
    DormiqTheme {
        AddRoomsScreen("hostel-001", "floor-g", { _, _ -> }, {})
    }
}

@Preview
@Composable
fun AddBedsScreenPreview() {
    DormiqTheme {
        AddBedsScreen("hostel-001", "room-g01", { _ -> }, {})
    }
}

@Preview
@Composable
fun ReviewStructureScreenPreview() {
    DormiqTheme {
        ReviewStructureScreen("hostel-001", {}, {})
    }
}
