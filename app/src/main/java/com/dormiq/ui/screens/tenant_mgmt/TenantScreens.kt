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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dormiq.domain.model.*
import com.dormiq.ui.components.*
import com.dormiq.ui.theme.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.dormiq.ui.viewmodel.TenantViewModel
import com.dormiq.ui.viewmodel.AssignBedViewModel
import androidx.compose.runtime.collectAsState

// ─── Tenant List ───────────────────

@Composable
fun TenantListScreen(
    hostelId: String,
    onTenantClick: (tenantId: String) -> Unit,
    onAddTenant: () -> Unit,
    onBack: () -> Unit,
    tenantViewModel: TenantViewModel = hiltViewModel()
) {
    val tenants by tenantViewModel.getTenants(hostelId).collectAsState(initial = emptyList())


    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Active", "Checked Out", "Pending")

    val filtered = tenants.filter { t ->
        val q = searchQuery.lowercase()
        val matchesSearch = t.name.lowercase().contains(q) || t.phone.contains(q) || (t.assignedRoomNumber?.contains(q) ?: false)
        val matchesFilter = when (selectedFilter) {
            "Active" -> t.status == TenantStatus.ACTIVE
            "Checked Out" -> t.status == TenantStatus.CHECKED_OUT
            "Pending" -> t.status == TenantStatus.PENDING
            else -> true
        }
        matchesSearch && matchesFilter
    }


    Scaffold(
        containerColor = DormiqNavy,
        topBar = { DormiqTopBar(title = "Tenants", onBack = onBack) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTenant,
                containerColor = DormiqBlue,
                contentColor = androidx.compose.ui.graphics.Color.White,
                icon = { Icon(Icons.Default.PersonAdd, null) },
                text = { Text("Add Tenant", color = androidx.compose.ui.graphics.Color.White) },
                shape = RoundedCornerShape(14.dp)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Stats
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple("${tenants.count { it.status == TenantStatus.ACTIVE }}", "Active", DormiqGreen),
                    Triple("${tenants.count { it.status == TenantStatus.PENDING }}", "Pending", DormiqAmber),
                    Triple("${tenants.count { it.status == TenantStatus.CHECKED_OUT }}", "Checked Out", DormiqTextSecondary),
                ).forEach { (count, label, color) ->
                    Box(
                        modifier = Modifier.weight(1f)
                            .background(DormiqCard, RoundedCornerShape(10.dp))
                            .border(1.dp, DormiqBorder, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(count, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
                            Text(label, style = MaterialTheme.typography.labelSmall, color = DormiqTextSecondary)
                        }
                    }
                }
            }

            // Search & Filter
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                DormiqTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = "Search by name, phone, room...",
                    leadingIcon = Icons.Default.Search
                )
                Spacer(Modifier.height(8.dp))
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

            Spacer(Modifier.height(8.dp))

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered) { tenant ->
                    DormiqCard(modifier = Modifier.fillMaxWidth(), onClick = { onTenantClick(tenant.id) }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AvatarCircle(tenant.name, size = 50.dp)
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(tenant.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = DormiqText)
                                    Spacer(Modifier.width(8.dp))
                                    StatusChip(tenant.status)
                                }
                                Spacer(Modifier.height(2.dp))
                                Text(tenant.phone, style = MaterialTheme.typography.bodySmall, color = DormiqTextSecondary)
                                val roomInfo = if (tenant.assignedRoomNumber != null) "Room ${tenant.assignedRoomNumber}" else "Not Assigned"
                                val floorInfo = tenant.assignedFloorName ?: ""
                                val bedInfo = tenant.assignedBedLabel ?: ""
                                
                                Text(
                                    listOfNotNull(roomInfo, floorInfo, bedInfo).filter { it.isNotBlank() }.joinToString(" · "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DormiqBlue
                                )
                            }
                            Icon(Icons.Default.ChevronRight, null, tint = DormiqTextMuted)
                        }
                    }
                }

                item { Spacer(Modifier.height(90.dp)) }
            }
        }
    }
}

// ─── Add Tenant ───────────────────────────────────────────────────────────────

@Composable
fun EditTenantScreen(
    tenantId: String,
    onSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: TenantViewModel = hiltViewModel()
) {
    LaunchedEffect(tenantId) {
        viewModel.loadTenant(tenantId)
    }

    val tenant by viewModel.tenant.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Add a small delay/loading check to prevent immediate onBack
    if (tenant == null && !isLoading) {
        // Show loading instead of immediate exit
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator(color = DormiqBlue)
        }
        // Only go back if we've tried to load and still have nothing after a bit
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(1000)
            if (tenant == null && !isLoading) onBack()
        }
    } else if (tenant != null) {
        TenantFormScreen(
            title = "Edit Tenant",
            initialTenant = tenant!!,
            onSave = { updatedTenant ->
                viewModel.updateTenant(updatedTenant) {
                    onSaved()
                }
            },
            onBack = onBack,
            isLoading = isLoading
        )
    }
}

@Composable
fun AddTenantScreen(
    hostelId: String,
    onSaved: (tenantId: String) -> Unit,
    onBack: () -> Unit,
    viewModel: TenantViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    
    TenantFormScreen(
        title = "Add Tenant",
        initialTenant = Tenant(hostelId = hostelId),
        onSave = { tenant ->
            viewModel.createTenant(tenant) { id ->
                onSaved(id)
            }
        },
        onBack = onBack,
        isLoading = isLoading
    )
}

@Composable
fun TenantFormScreen(
    title: String,
    initialTenant: Tenant,
    onSave: (Tenant) -> Unit,
    onBack: () -> Unit,
    isLoading: Boolean
) {
    var name by remember { mutableStateOf(initialTenant.name) }
    var phone by remember { mutableStateOf(initialTenant.phone) }
    var email by remember { mutableStateOf(initialTenant.email ?: "") }
    var idType by remember { mutableStateOf(initialTenant.idType ?: "Aadhaar") }
    var idNumber by remember { mutableStateOf(initialTenant.idNumber ?: "") }
    var emergencyName by remember { mutableStateOf(initialTenant.emergencyContactName ?: "") }
    var emergencyPhone by remember { mutableStateOf(initialTenant.emergencyContactPhone ?: "") }
    var joiningDate by remember { mutableStateOf(initialTenant.joiningDate ?: "") }

    var nameError by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf("") }
    var idError by remember { mutableStateOf("") }

    val idTypes = listOf("Aadhaar", "PAN", "Passport", "DL")

    fun validate() {
        nameError = if (name.isBlank()) "Name is required" else ""
        phoneError = if (phone.length < 10) "Enter valid phone" else ""
        idError = if (idNumber.isBlank()) "ID number is required" else ""
        
        if (nameError.isEmpty() && phoneError.isEmpty() && idError.isEmpty()) {
            // Format date from DD/MM/YYYY to YYYY-MM-DD for Supabase
            val formattedDate = try {
                if (joiningDate.contains("/")) {
                    val parts = joiningDate.split("/")
                    if (parts.size == 3) {
                        "${parts[2]}-${parts[1].padStart(2, '0')}-${parts[0].padStart(2, '0')}"
                    } else joiningDate
                } else joiningDate
            } catch (e: Exception) {
                joiningDate
            }

            onSave(initialTenant.copy(
                name = name,
                phone = phone,
                email = email,
                idType = idType,
                idNumber = idNumber,
                emergencyContactName = emergencyName,
                emergencyContactPhone = emergencyPhone,
                joiningDate = formattedDate
            ))
        }
    }

    Scaffold(
        containerColor = DormiqNavy,
        topBar = { DormiqTopBar(title = title, onBack = onBack) }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = DormiqBlue)
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AvatarCircle(if (name.isNotBlank()) name else "?", size = 72.dp)
                        Spacer(Modifier.height(8.dp))
                        DormiqTextButton("Upload Photo", {})
                    }
                }
                Spacer(Modifier.height(20.dp))
                SectionLabel("Personal Details")
                Spacer(Modifier.height(12.dp))
                DormiqTextField(name, { name = it }, "Full Name *", leadingIcon = Icons.Default.Person, isError = nameError.isNotEmpty(), errorText = nameError)
                Spacer(Modifier.height(12.dp))
                DormiqTextField(phone, { if (it.length <= 10) phone = it }, "Phone Number *", leadingIcon = Icons.Default.Phone, keyboardType = KeyboardType.Phone, isError = phoneError.isNotEmpty(), errorText = phoneError)
                Spacer(Modifier.height(12.dp))
                DormiqTextField(email, { email = it }, "Email Address", leadingIcon = Icons.Default.Email, keyboardType = KeyboardType.Email, placeholder = "Optional")
                Spacer(Modifier.height(12.dp))
                DormiqTextField(joiningDate, { joiningDate = it }, "Joining Date", leadingIcon = Icons.Default.DateRange, placeholder = "YYYY-MM-DD")
                Spacer(Modifier.height(20.dp))
                SectionLabel("Identity Proof")
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    idTypes.forEach { type ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (idType == type) DormiqBlueGlow else DormiqSurface, RoundedCornerShape(8.dp))
                                .border(1.dp, if (idType == type) DormiqBlue else DormiqBorder, RoundedCornerShape(8.dp))
                                .clickable { idType = type }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(type, style = MaterialTheme.typography.labelSmall, color = if (idType == type) DormiqBlue else DormiqTextSecondary)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                DormiqTextField(idNumber, { idNumber = it }, "$idType Number *", leadingIcon = Icons.Default.Badge, isError = idError.isNotEmpty(), errorText = idError)
                Spacer(Modifier.height(20.dp))
                SectionLabel("Emergency Contact")
                Spacer(Modifier.height(12.dp))
                DormiqTextField(emergencyName, { emergencyName = it }, "Contact Name", leadingIcon = Icons.Default.ContactPhone)
                Spacer(Modifier.height(12.dp))
                DormiqTextField(emergencyPhone, { emergencyPhone = it }, "Contact Phone", leadingIcon = Icons.Default.Phone, keyboardType = KeyboardType.Phone)
                Spacer(Modifier.height(32.dp))
                DormiqPrimaryButton(if (initialTenant.id.isEmpty()) "Save & Assign Room" else "Update Details", ::validate, modifier = Modifier.fillMaxWidth(), icon = if (initialTenant.id.isEmpty()) Icons.Default.ArrowForward else Icons.Default.Save)
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}


// ─── Assign Room/Bed ─────────────────────────────────────────────────────────

@Composable
fun AssignBedScreen(
    tenantId: String,
    hostelId: String,
    onAssigned: () -> Unit,
    onBack: () -> Unit,
    tenantViewModel: TenantViewModel = hiltViewModel(),
    assignViewModel: AssignBedViewModel = hiltViewModel()
) {
    LaunchedEffect(hostelId, tenantId) {
        assignViewModel.setHostelId(hostelId)
        tenantViewModel.loadTenant(tenantId)
    }

    val tenant by tenantViewModel.tenant.collectAsState()
    val floors by assignViewModel.floors.collectAsState(initial = emptyList())
    val rooms by assignViewModel.rooms.collectAsState(initial = emptyList())
    val vacantBeds by assignViewModel.vacantBeds.collectAsState(initial = emptyList())

    var selectedFloorId by remember { mutableStateOf("") }
    var selectedRoomId by remember { mutableStateOf("") }
    var selectedBedId by remember { mutableStateOf("") }
    var selectedBedLabel by remember { mutableStateOf("") }
    var selectedRoomNumber by remember { mutableStateOf("") }
    var rentAmountStr by remember { mutableStateOf("") }


    Scaffold(
        containerColor = DormiqNavy,
        topBar = { DormiqTopBar(title = "Assign Room & Bed", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Tenant summary
            tenant?.let { t ->
                DormiqCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AvatarCircle(t.name, size = 44.dp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(t.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = DormiqText)
                            Text("Assigning a bed for this tenant", style = MaterialTheme.typography.bodySmall, color = DormiqTextSecondary)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            StepCard(
                step = 1,
                title = "Select Floor",
                isComplete = selectedFloorId.isNotEmpty()
            ) {
                floors.forEach { floor ->
                    SelectionRow(
                        label = floor.name,
                        isSelected = selectedFloorId == floor.id,
                        onClick = { 
                            selectedFloorId = floor.id
                            selectedRoomId = ""
                            selectedBedId = ""
                            assignViewModel.selectFloor(floor.id)
                        }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Step 2: Room
            if (selectedFloorId.isNotEmpty()) {
                StepCard(step = 2, title = "Select Room", isComplete = selectedRoomId.isNotEmpty()) {
                    if (rooms.isEmpty()) {
                        Box(Modifier.fillMaxWidth().padding(16.dp), Alignment.Center) {
                            Text("No rooms found on this floor", color = DormiqTextSecondary, style = MaterialTheme.typography.bodySmall)
                        }
                    } else {
                        rooms.forEach { room ->
                            SelectionRow(
                                label = "Room ${room.roomNumber}",
                                subtitle = "${room.type} · ${room.capacity - room.occupiedBeds} vacant beds",
                                isSelected = selectedRoomId == room.id,
                                onClick = { 
                                    selectedRoomId = room.id
                                    selectedRoomNumber = room.roomNumber
                                    selectedBedId = ""
                                    assignViewModel.selectRoom(room.id)
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Step 3: Bed
            if (selectedRoomId.isNotEmpty()) {
                StepCard(step = 3, title = "Select Vacant Bed", isComplete = selectedBedId.isNotEmpty()) {
                    if (vacantBeds.isEmpty()) {
                        Box(Modifier.fillMaxWidth().padding(16.dp), Alignment.Center) {
                            Text("No vacant beds in this room", color = DormiqTextSecondary, style = MaterialTheme.typography.bodySmall)
                        }
                    } else {
                        vacantBeds.forEach { bed ->
                            SelectionRow(
                                label = bed.label,
                                subtitle = "₹${bed.price ?: 0.0}/month",
                                isSelected = selectedBedId == bed.id,
                                onClick = { 
                                    selectedBedId = bed.id
                                    selectedBedLabel = bed.label
                                    rentAmountStr = (bed.price ?: 0.0).toInt().toString()
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Confirm
            if (selectedBedId.isNotEmpty()) {
                StepCard(step = 4, title = "Set Monthly Rent", isComplete = rentAmountStr.isNotEmpty()) {
                    Text("Customize the monthly rent amount for this tenant if needed.", style = MaterialTheme.typography.bodySmall, color = DormiqTextSecondary)
                    Spacer(Modifier.height(12.dp))
                    DormiqTextField(
                        value = rentAmountStr,
                        onValueChange = { rentAmountStr = it },
                        label = "Monthly Rent Amount",
                        leadingIcon = Icons.Default.Payments,
                        keyboardType = KeyboardType.Number
                    )
                }
                Spacer(Modifier.height(16.dp))
                DormiqCard(modifier = Modifier.fillMaxWidth(), glowColor = DormiqGreenGlow) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = DormiqGreen, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Ready to assign: Room $selectedRoomNumber · $selectedBedLabel for ₹$rentAmountStr/month", style = MaterialTheme.typography.bodyMedium, color = DormiqText)
                    }
                }
                Spacer(Modifier.height(16.dp))
                DormiqPrimaryButton(
                    text = "Confirm Assignment", 
                    onClick = {
                        val rentAmount = rentAmountStr.toDoubleOrNull() ?: 0.0
                        tenantViewModel.assignBed(tenantId, selectedBedId, selectedRoomId, hostelId, rentAmount) {
                            onAssigned()
                        }
                    }, 
                    modifier = Modifier.fillMaxWidth(), 
                    icon = Icons.Default.Check
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun StepCard(step: Int, title: String, isComplete: Boolean, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DormiqCard, RoundedCornerShape(14.dp))
            .border(1.dp, if (isComplete) DormiqGreen.copy(0.4f) else DormiqBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(28.dp).background(if (isComplete) DormiqGreenGlow else DormiqBlueGlow, CircleShape),
                Alignment.Center
            ) {
                if (isComplete) Icon(Icons.Default.Check, null, tint = DormiqGreen, modifier = Modifier.size(14.dp))
                else Text("$step", style = MaterialTheme.typography.labelMedium, color = DormiqBlue, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(10.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = DormiqText)
        }
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Preview
@Composable
fun TenantListScreenPreview() {
    DormiqTheme {
        TenantListScreen("hostel-001", {}, {}, {})
    }
}

@Preview
@Composable
fun AddTenantScreenPreview() {
    DormiqTheme {
        AddTenantScreen("hostel-001", {}, {})
    }
}

@Preview
@Composable
fun AssignBedScreenPreview() {
    DormiqTheme {
        AssignBedScreen("tenant-001", "hostel-001", {}, {})
    }
}
