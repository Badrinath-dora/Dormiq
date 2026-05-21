package com.dormiq.ui.screens.payments

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dormiq.domain.model.*
import com.dormiq.ui.components.*
import com.dormiq.ui.theme.*

import androidx.hilt.navigation.compose.hiltViewModel
import com.dormiq.ui.viewmodel.PaymentViewModel
import com.dormiq.ui.viewmodel.AuthViewModel
import com.dormiq.ui.viewmodel.AuthState
import androidx.compose.ui.platform.LocalContext
import com.dormiq.util.PdfReceiptGenerator

// ─── Tenant Payment Screen ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantPaymentScreen(
    onPayNow: (amount: Double) -> Unit,
    paymentViewModel: PaymentViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val tenant = (authState as? AuthState.Authenticated)?.user
    val payments by paymentViewModel.payments.collectAsState()
    val isLoading by paymentViewModel.isLoading.collectAsState()

    var showPaymentSheet by remember { mutableStateOf(false) }
    var selectedBill by remember { mutableStateOf<RentPayment?>(null) }

    LaunchedEffect(tenant?.id) {
        tenant?.id?.let { paymentViewModel.loadTenantPayments(it) }
    }

    val currentRent = payments.firstOrNull { it.status != PaymentStatus.PAID }
    val paymentHistory = payments.filter { it.status == PaymentStatus.PAID }

    Scaffold(
        containerColor = DormiqNavy,
        topBar = { DormiqTopBar(title = "Rent & Payments") }
    ) { padding ->
        if (isLoading && payments.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = DormiqBlue)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Active Bill Card
                currentRent?.let { bill ->
                    item {
                        SectionLabel("Current Month Due")
                        Spacer(Modifier.height(8.dp))
                        DormiqCard(modifier = Modifier.fillMaxWidth(), glowColor = DormiqBlueGlow) {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Column {
                                    Text(bill.month, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DormiqText)
                                    Text("Due Date: ${bill.dueDate}", style = MaterialTheme.typography.bodySmall, color = DormiqTextSecondary)
                                }
                                StatusChip(bill.status.name)
                            }
                            Spacer(Modifier.height(20.dp))
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Bottom) {
                                Column {
                                    Text("Total Amount", style = MaterialTheme.typography.labelSmall, color = DormiqTextSecondary)
                                    Text("₹${bill.amount}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = DormiqBlue)
                                }
                                DormiqPrimaryButton(
                                    text = "Pay Now",
                                    onClick = { 
                                        selectedBill = bill
                                        showPaymentSheet = true
                                    },
                                    modifier = Modifier.height(44.dp)
                                )
                            }
                        }
                    }
                } ?: item {
                    DormiqCard(modifier = Modifier.fillMaxWidth()) {
                        Text("No pending payments!", color = DormiqGreen, fontWeight = FontWeight.Bold)
                    }
                }

                // Summary Info
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(
                            label = "Remaining",
                            value = "₹${currentRent?.let { it.amount - it.paidAmount } ?: 0.0}",
                            icon = Icons.Default.PendingActions,
                            color = DormiqAmber,
                            glowColor = DormiqAmberGlow,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Total Paid",
                            value = "₹${paymentHistory.sumOf { it.paidAmount }}",
                            icon = Icons.Default.AccountBalanceWallet,
                            color = DormiqGreen,
                            glowColor = DormiqGreenGlow,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // History
                item {
                    SectionLabel("Payment History")
                }

                items(paymentHistory) { payment ->
                    val context = LocalContext.current
                    DormiqCard(modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(40.dp).background(DormiqGreenGlow, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Check, null, tint = DormiqGreen, modifier = Modifier.size(20.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(payment.month, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = DormiqText)
                                    Text("Paid on ${payment.paidDate ?: "N/A"}", style = MaterialTheme.typography.bodySmall, color = DormiqTextSecondary)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("₹${payment.paidAmount}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = DormiqText)
                                if (payment.receiptGenerated) {
                                    Spacer(Modifier.height(6.dp))
                                    Button(
                                        onClick = {
                                            PdfReceiptGenerator.generateAndShareReceipt(context, payment)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = DormiqBlue),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Download,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "Receipt",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Interactive simulated checkout sheet
    if (showPaymentSheet && selectedBill != null) {
        val bill = selectedBill!!
        var paymentStep by remember { mutableStateOf(1) } // 1 = Select Mode, 2 = Enter Details, 3 = Processing, 4 = Success
        var paymentMode by remember { mutableStateOf("UPI") } // UPI or CARD
        
        // UPI details
        var upiId by remember { mutableStateOf("tenant@okhdfcbank") }
        var upiPin by remember { mutableStateOf("") }
        
        // Card details
        var cardNumber by remember { mutableStateOf("") }
        var expiryDate by remember { mutableStateOf("") }
        var cvv by remember { mutableStateOf("") }

        ModalBottomSheet(
            onDismissRequest = { showPaymentSheet = false },
            containerColor = DormiqNavy,
            dragHandle = { BottomSheetDefaults.DragHandle(color = DormiqBorder) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (paymentStep) {
                    1 -> {
                        Text("Secure Payment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DormiqText)
                        Spacer(Modifier.height(4.dp))
                        Text("Amount: ₹${bill.amount}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = DormiqBlue)
                        Spacer(Modifier.height(20.dp))
                        
                        DormiqCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { 
                                paymentMode = "UPI"
                                paymentStep = 2
                            },
                            glowColor = if (paymentMode == "UPI") DormiqBlueGlow else Color.Transparent,
                            borderColor = if (paymentMode == "UPI") DormiqBlue else DormiqBorder
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(40.dp).background(DormiqBlueGlow, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.QrCode, null, tint = DormiqBlue)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("UPI / Google Pay / PhonePe", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = DormiqText)
                                    Text("Pay instantly using any UPI app", style = MaterialTheme.typography.bodySmall, color = DormiqTextSecondary)
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(12.dp))
                        
                        DormiqCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { 
                                paymentMode = "CARD"
                                paymentStep = 2
                            },
                            glowColor = if (paymentMode == "CARD") DormiqBlueGlow else Color.Transparent,
                            borderColor = if (paymentMode == "CARD") DormiqBlue else DormiqBorder
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(40.dp).background(DormiqBlueGlow, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.CreditCard, null, tint = DormiqBlue)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("Credit / Debit Card", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = DormiqText)
                                    Text("Visa, Mastercard, RuPay supported", style = MaterialTheme.typography.bodySmall, color = DormiqTextSecondary)
                                }
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                    }
                    2 -> {
                        Text("Enter Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DormiqText)
                        Spacer(Modifier.height(20.dp))
                        
                        if (paymentMode == "UPI") {
                            DormiqTextField(upiId, { upiId = it }, "UPI ID", leadingIcon = Icons.Default.AlternateEmail)
                            Spacer(Modifier.height(12.dp))
                            DormiqTextField(upiPin, { upiPin = it }, "Enter 4-Digit UPI PIN", leadingIcon = Icons.Default.Lock, keyboardType = KeyboardType.NumberPassword)
                        } else {
                            DormiqTextField(cardNumber, { cardNumber = it }, "Card Number", leadingIcon = Icons.Default.CreditCard, keyboardType = KeyboardType.Number)
                            Spacer(Modifier.height(12.dp))
                            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                                Box(Modifier.weight(1f)) {
                                    DormiqTextField(expiryDate, { expiryDate = it }, "MM/YY", keyboardType = KeyboardType.Number)
                                }
                                Box(Modifier.weight(1f)) {
                                    DormiqTextField(cvv, { cvv = it }, "CVV", keyboardType = KeyboardType.NumberPassword)
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(24.dp))
                        
                        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                            DormiqSecondaryButton("Back", { paymentStep = 1 }, Modifier.weight(1f))
                            DormiqPrimaryButton("Pay ₹${bill.amount}", {
                                paymentStep = 3
                            }, Modifier.weight(1.5f), icon = Icons.Default.CheckCircle)
                        }
                    }
                    3 -> {
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(2000)
                            val txnId = "TXN_MOCK_" + java.util.UUID.randomUUID().toString().take(12).uppercase()
                            paymentViewModel.payRent(bill, txnId) {
                                paymentStep = 4
                            }
                        }
                        
                        Spacer(Modifier.height(20.dp))
                        CircularProgressIndicator(color = DormiqBlue, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(20.dp))
                        Text("Processing secure transaction...", style = MaterialTheme.typography.bodyMedium, color = DormiqText, fontWeight = FontWeight.Bold)
                        Text("Please do not close the app or press back", style = MaterialTheme.typography.bodySmall, color = DormiqTextSecondary)
                        Spacer(Modifier.height(24.dp))
                    }
                    4 -> {
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(2500)
                            showPaymentSheet = false
                        }
                        
                        Spacer(Modifier.height(20.dp))
                        Box(
                            modifier = Modifier.size(72.dp).background(DormiqGreenGlow, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, null, tint = DormiqGreen, modifier = Modifier.size(40.dp))
                        }
                        Spacer(Modifier.height(20.dp))
                        Text("Payment Successful!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DormiqGreen)
                        Spacer(Modifier.height(8.dp))
                        Text("Rent of ₹${bill.amount} paid successfully.", style = MaterialTheme.typography.bodyMedium, color = DormiqText)
                        Text("Receipt has been auto-generated.", style = MaterialTheme.typography.bodySmall, color = DormiqTextSecondary)
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

// ─── Owner Payment Management ─────────────────────────────────────────────────

@Composable
fun OwnerPaymentManagementScreen(
    hostelId: String,
    paymentViewModel: PaymentViewModel = hiltViewModel()
) {
    val summary by paymentViewModel.summary.collectAsState()
    val payments by paymentViewModel.payments.collectAsState()
    val isLoading by paymentViewModel.isLoading.collectAsState()

    var showMarkAsPaidDialog by remember { mutableStateOf(false) }
    var selectedPaymentToMark by remember { mutableStateOf<RentPayment?>(null) }

    LaunchedEffect(hostelId) {
        paymentViewModel.loadPayments(hostelId)
    }

    val pendingPayments = payments.filter { it.status != PaymentStatus.PAID }
    val paidPayments = payments.filter { it.status == PaymentStatus.PAID }

    Scaffold(
        containerColor = DormiqNavy,
        topBar = { DormiqTopBar(title = "Rent Management") }
    ) { padding ->
        if (isLoading && payments.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = DormiqBlue)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Collection Overview
                item {
                    DormiqCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = DormiqBlue.copy(alpha = 0.05f),
                        borderColor = DormiqBlue.copy(alpha = 0.2f)
                    ) {
                        Text("Collection Overview", style = MaterialTheme.typography.labelSmall, color = DormiqTextSecondary)
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("₹${summary.totalCollected}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = DormiqBlue)
                            Spacer(Modifier.width(8.dp))
                            Text("/ ₹${summary.totalExpected}", style = MaterialTheme.typography.bodyMedium, color = DormiqTextSecondary, modifier = Modifier.padding(bottom = 6.dp))
                        }
                        Spacer(Modifier.height(16.dp))
                        
                        val progress = if (summary.totalExpected > 0) (summary.totalCollected / summary.totalExpected).toFloat() else 0f
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                            color = DormiqBlue,
                            trackColor = DormiqBlue.copy(alpha = 0.1f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("${(progress * 100).toInt()}% Collected", style = MaterialTheme.typography.labelSmall, color = DormiqBlue)
                            Text("₹${summary.totalPending} Pending", style = MaterialTheme.typography.labelSmall, color = DormiqAmber)
                        }
                    }
                }

                // Stats Row
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(
                            label = "Paid Tenants",
                            value = "${summary.paidCount}",
                            icon = Icons.Default.Groups,
                            color = DormiqGreen,
                            glowColor = DormiqGreenGlow,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Pending",
                            value = "${summary.pendingCount}",
                            icon = Icons.Default.Schedule,
                            color = DormiqAmber,
                            glowColor = DormiqAmberGlow,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Pending List
                item {
                    SectionHeader("Pending & Defaulters", "Remind All", {})
                }

                items(pendingPayments) { payment ->
                    DormiqCard(modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                            Column {
                                Text(payment.tenantName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = DormiqText)
                                Text("Room ${payment.roomNumber} · ${payment.floorName}", style = MaterialTheme.typography.bodySmall, color = DormiqTextSecondary)
                            }
                            StatusChip(payment.status.name)
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Column {
                                Text("Pending Amount", style = MaterialTheme.typography.labelSmall, color = DormiqTextSecondary)
                                Text("₹${payment.amount - payment.paidAmount}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DormiqRed)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(
                                    onClick = { 
                                        selectedPaymentToMark = payment
                                        showMarkAsPaidDialog = true
                                    },
                                    modifier = Modifier.background(DormiqGreenGlow, CircleShape)
                                ) {
                                    Icon(Icons.Default.Check, null, tint = DormiqGreen, modifier = Modifier.size(20.dp))
                                }
                                IconButton(
                                    onClick = {},
                                    modifier = Modifier.background(DormiqBlueGlow, CircleShape)
                                ) {
                                    Icon(Icons.Default.NotificationsActive, null, tint = DormiqBlue, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }

                // Paid Payments & Receipts History for Owner
                if (paidPayments.isNotEmpty()) {
                    item {
                        SectionHeader("Paid Payments & Receipts", "", {})
                    }
                    items(paidPayments) { payment ->
                        val context = LocalContext.current
                        DormiqCard(modifier = Modifier.fillMaxWidth()) {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                                Column {
                                    Text(payment.tenantName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = DormiqText)
                                    Text("Room ${payment.roomNumber} · ${payment.floorName}", style = MaterialTheme.typography.bodySmall, color = DormiqTextSecondary)
                                    Text("Paid on ${payment.paidDate ?: "N/A"}", style = MaterialTheme.typography.bodySmall, color = DormiqTextMuted)
                                }
                                Text("₹${payment.paidAmount}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = DormiqGreen)
                            }
                            Spacer(Modifier.height(12.dp))
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                if (payment.receiptGenerated) {
                                    Text("Receipt Generated ✓", style = MaterialTheme.typography.labelSmall, color = DormiqGreen, fontWeight = FontWeight.Bold)
                                    DormiqPrimaryButton(
                                        text = "Download",
                                        onClick = {
                                            PdfReceiptGenerator.generateAndShareReceipt(context, payment)
                                        },
                                        modifier = Modifier.height(36.dp)
                                    )
                                } else {
                                    Text("Receipt Pending", style = MaterialTheme.typography.labelSmall, color = DormiqTextSecondary)
                                    DormiqPrimaryButton(
                                        text = "Generate Receipt",
                                        onClick = {
                                            paymentViewModel.generateReceipt(payment)
                                        },
                                        modifier = Modifier.height(36.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showMarkAsPaidDialog && selectedPaymentToMark != null) {
        val payment = selectedPaymentToMark!!
        AlertDialog(
            onDismissRequest = { showMarkAsPaidDialog = false },
            containerColor = DormiqCard,
            title = {
                Text("Confirm Payment Received", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DormiqText)
            },
            text = {
                Text("Are you sure you want to mark rent of ₹${payment.amount - payment.paidAmount} for ${payment.tenantName} as PAID?\n\nThis will record this payment in Supabase as offline cash/UPI receipt.", style = MaterialTheme.typography.bodyMedium, color = DormiqTextSecondary)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        paymentViewModel.markAsPaid(payment) {
                            showMarkAsPaidDialog = false
                        }
                    }
                ) {
                    Text("Confirm Received", color = DormiqGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMarkAsPaidDialog = false }) {
                    Text("Cancel", color = DormiqTextSecondary)
                }
            }
        )
    }
}
