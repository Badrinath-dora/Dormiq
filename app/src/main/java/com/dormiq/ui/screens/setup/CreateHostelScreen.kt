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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.dormiq.ui.components.*
import com.dormiq.ui.theme.*

import androidx.hilt.navigation.compose.hiltViewModel
import com.dormiq.ui.viewmodel.SetupViewModel
import com.dormiq.ui.viewmodel.SetupState

@Composable
fun CreateHostelScreen(
    onNext: (hostelId: String) -> Unit,
    onBack: () -> Unit,
    viewModel: SetupViewModel = hiltViewModel()
) {
    var nameError by remember { mutableStateOf("") }
    var addressError by remember { mutableStateOf("") }

    val hostelTypes = listOf("Boys", "Girls", "Mixed")

    val setupState by viewModel.setupState.collectAsState()
    val isLoading = setupState is SetupState.Loading

    fun validate() {
        nameError = if (viewModel.hostelName.isBlank()) "Hostel name is required" else ""
        addressError = if (viewModel.address.isBlank()) "Address is required" else ""
        if (nameError.isEmpty() && addressError.isEmpty()) {
            viewModel.createHostel { id ->
                onNext(id)
            }
        }
    }

    Scaffold(
        containerColor = DormiqNavy,
        topBar = {
            DormiqTopBar(title = "Create Hostel", onBack = onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // Progress indicator
            SetupProgress(currentStep = 1, totalSteps = 5)

            Spacer(Modifier.height(24.dp))

            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(listOf(GradientBlueStart.copy(0.3f), DormiqSurface)),
                        RoundedCornerShape(16.dp)
                    )
                    .border(1.dp, DormiqBorder, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(48.dp).background(DormiqBlueGlow, RoundedCornerShape(12.dp)),
                        Alignment.Center
                    ) {
                        Icon(Icons.Default.Apartment, null, tint = DormiqBlue, modifier = Modifier.size(26.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Hostel Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DormiqText)
                        Text("Enter basic property information", style = MaterialTheme.typography.bodySmall, color = DormiqTextSecondary)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            DormiqTextField(
                value = viewModel.hostelName,
                onValueChange = { viewModel.hostelName = it },
                label = "Hostel Name *",
                leadingIcon = Icons.Default.Apartment,
                isError = nameError.isNotEmpty(),
                errorText = nameError,
                placeholder = "e.g., Sunrise Boys Hostel"
            )

            Spacer(Modifier.height(16.dp))

            DormiqTextField(
                value = viewModel.address,
                onValueChange = { viewModel.address = it },
                label = "Full Address *",
                leadingIcon = Icons.Default.LocationOn,
                isError = addressError.isNotEmpty(),
                errorText = addressError,
                singleLine = false,
                maxLines = 3,
                minLines = 3,
                placeholder = "Street, City, PIN code"
            )

            Spacer(Modifier.height(16.dp))

            // Hostel Type Selector
            Text("Hostel Type *", style = MaterialTheme.typography.labelLarge, color = DormiqTextSecondary)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                hostelTypes.forEach { type ->
                    val selected = viewModel.hostelType == type
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (selected) DormiqBlueGlow else DormiqSurface,
                                RoundedCornerShape(10.dp)
                            )
                            .border(
                                1.dp,
                                if (selected) DormiqBlue else DormiqBorder,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { viewModel.hostelType = type }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val selected = viewModel.hostelType == type
                        Text(
                            type,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) DormiqBlue else DormiqTextSecondary,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            DormiqTextField(
                value = viewModel.phone,
                onValueChange = { viewModel.phone = it },
                label = "Contact Number",
                leadingIcon = Icons.Default.Phone,
                keyboardType = KeyboardType.Phone,
                placeholder = "Optional"
            )

            Spacer(Modifier.height(16.dp))

            DormiqTextField(
                value = viewModel.email,
                onValueChange = { viewModel.email = it },
                label = "Contact Email",
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
                placeholder = "Optional"
            )

            Spacer(Modifier.height(32.dp))

            if (setupState is SetupState.Error) {
                Text(
                    text = (setupState as SetupState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            DormiqPrimaryButton(
                text = "Next: Setup Floors",
                onClick = ::validate,
                modifier = Modifier.fillMaxWidth(),
                isLoading = isLoading,
                icon = Icons.Default.ArrowForward
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun SetupProgress(currentStep: Int, totalSteps: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { step ->
            val completed = step < currentStep
            val active = step == currentStep - 1

            Box(
                modifier = Modifier
                    .size(if (active) 32.dp else 24.dp)
                    .background(
                        when {
                            completed -> DormiqGreen
                            active -> DormiqBlue
                            else -> DormiqCard
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (completed) {
                    Icon(
                        Icons.Default.Check,
                        null,
                        tint = DormiqText,
                        modifier = Modifier.size(14.dp)
                    )
                } else {
                    Text(
                        "${step + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (active) DormiqText else DormiqTextMuted,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (step < totalSteps - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(if (completed) DormiqGreen else DormiqBorder)
                )
            }
        }
    }

    @Preview
    @Composable
    fun CreateHostelScreenPreview() {
        DormiqTheme {
            CreateHostelScreen({}, {})
        }
    }

    @Preview
    @Composable
    fun SetupProgressPreview() {
        DormiqTheme {
            Column(modifier = Modifier.padding(16.dp)) {
                SetupProgress(currentStep = 3, totalSteps = 5)
            }
        }
    }
}
