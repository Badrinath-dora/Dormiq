package com.dormiq.ui.screens.owner

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dormiq.domain.model.Hostel
import com.dormiq.ui.components.*
import com.dormiq.ui.theme.*
import com.dormiq.ui.viewmodel.ManagePropertiesViewModel

@Composable
fun ManagePropertiesScreen(
    onBack: () -> Unit,
    onRegisterNewHostel: () -> Unit,
    onHostelSwitched: () -> Unit,
    viewModel: ManagePropertiesViewModel = hiltViewModel()
) {
    val hostels by viewModel.hostels.collectAsStateWithLifecycle()
    val activeHostelId by viewModel.activeHostelId.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = DormiqNavy,
        topBar = {
            DormiqTopBar(
                title = "Manage Properties",
                onBack = onBack
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading && hostels.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = DormiqBlue)
                }
            } else if (hostels.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Apartment,
                        contentDescription = null,
                        tint = DormiqTextSecondary,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "No Properties Registered",
                        style = MaterialTheme.typography.titleMedium,
                        color = DormiqText,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Register your first hostel to start managing.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DormiqTextSecondary
                    )
                    Spacer(Modifier.height(24.dp))
                    DormiqPrimaryButton(
                        text = "Register Hostel",
                        onClick = onRegisterNewHostel,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Your Hostels & Properties",
                        style = MaterialTheme.typography.labelMedium,
                        color = DormiqTextSecondary,
                        modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(hostels) { hostel ->
                            val isActive = hostel.id == activeHostelId
                            HostelItemCard(
                                hostel = hostel,
                                isActive = isActive,
                                onSelect = {
                                    if (!isActive) {
                                        viewModel.switchHostel(hostel.id) {
                                            onHostelSwitched()
                                        }
                                    }
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    DormiqPrimaryButton(
                        text = "Register Another Hostel",
                        icon = Icons.Default.Add,
                        onClick = onRegisterNewHostel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HostelItemCard(
    hostel: Hostel,
    isActive: Boolean,
    onSelect: () -> Unit
) {
    DormiqCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        glowColor = if (isActive) DormiqBlueGlow else Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = hostel.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DormiqText
                    )
                    
                    if (isActive) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DormiqGreenGlow)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = DormiqGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "ACTIVE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = DormiqGreen
                                )
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(4.dp))
                
                Text(
                    text = hostel.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = DormiqTextSecondary,
                    maxLines = 2
                )
                
                Spacer(Modifier.height(12.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PropertyStatPill(label = hostel.type, color = DormiqBlue)
                    PropertyStatPill(label = "${hostel.totalFloors} Floors", color = DormiqTeal)
                    PropertyStatPill(label = "${hostel.totalRooms} Rooms", color = DormiqPurple)
                }
            }
            
            if (!isActive) {
                Text(
                    text = "Switch",
                    style = MaterialTheme.typography.labelMedium,
                    color = DormiqBlue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
fun PropertyStatPill(
    label: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}
