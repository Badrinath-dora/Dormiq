package com.dormiq.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dormiq.domain.model.*
import com.dormiq.domain.repository.PaymentRepository
import com.dormiq.domain.repository.TenantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val tenantRepository: TenantRepository
) : ViewModel() {

    private val _payments = MutableStateFlow<List<RentPayment>>(emptyList())
    val payments: StateFlow<List<RentPayment>> = _payments.asStateFlow()

    private val _summary = MutableStateFlow(PaymentSummary())
    val summary: StateFlow<PaymentSummary> = _summary.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadPayments(hostelId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            combine(
                paymentRepository.getPayments(hostelId),
                paymentRepository.getPaymentSummary(hostelId)
            ) { list, summ ->
                _payments.value = list
                _summary.value = summ
            }.collect {
                _isLoading.value = false
            }
        }
    }

    private var tenantPaymentsJob: kotlinx.coroutines.Job? = null
    private var tenantPaymentsCollectorJob: kotlinx.coroutines.Job? = null

    fun loadTenantPayments(userId: String, phone: String? = null) {
        tenantPaymentsJob?.cancel()
        tenantPaymentsJob = viewModelScope.launch {
            _isLoading.value = true
            tenantRepository.getTenantByUserId(userId).collectLatest { tenantObj ->
                if (tenantObj != null) {
                    loadPaymentsForTenant(tenantObj, userId, phone)
                } else if (phone != null) {
                    println("PaymentViewModel: getTenantByUserId returned null, trying phone: $phone")
                    val tenantByPhone = tenantRepository.getTenantByPhone(phone)
                    if (tenantByPhone != null) {
                        println("PaymentViewModel: Found tenant by phone: ${tenantByPhone.name}")
                        
                        // Auto-link user_id for future fetches
                        if (tenantByPhone.userId == null) {
                            println("PaymentViewModel: Auto-linking tenant ${tenantByPhone.id} to userId $userId")
                            tenantRepository.updateTenantUserId(tenantByPhone.id, userId)
                        }
                        
                        loadPaymentsForTenant(tenantByPhone, userId, phone)
                    } else {
                        println("PaymentViewModel: No tenant found by phone: $phone")
                        _payments.value = emptyList()
                        _isLoading.value = false
                    }
                } else {
                    _payments.value = emptyList()
                    _isLoading.value = false
                }
            }
        }
    }

    private fun loadPaymentsForTenant(tenantObj: Tenant, userId: String, phone: String?) {
        tenantPaymentsCollectorJob?.cancel()
        tenantPaymentsCollectorJob = viewModelScope.launch {
            paymentRepository.getTenantPayments(tenantObj.id)
                .collect { list ->
                    _payments.value = list
                    _isLoading.value = false

                    // Auto-billing: If a new month has arrived, automatically generate the pending bill
                    val currentMonth = java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy").format(java.time.LocalDate.now())
                    val hasCurrentMonthBill = list.any { it.month.equals(currentMonth, ignoreCase = true) }
                    if (!hasCurrentMonthBill && list.isNotEmpty()) {
                        val latest = list.maxByOrNull { it.dueDate } ?: list.first()
                        val dueDateStr = java.time.LocalDate.now().withDayOfMonth(5).toString() // Due on 5th of the month
                        val newPayment = RentPayment(
                            id = java.util.UUID.randomUUID().toString(),
                            hostelId = latest.hostelId,
                            tenantId = latest.tenantId,
                            tenantName = latest.tenantName,
                            amount = latest.amount,
                            paidAmount = 0.0,
                            dueDate = dueDateStr,
                            paidDate = null,
                            month = currentMonth,
                            status = PaymentStatus.PENDING,
                            transactionId = null,
                            roomNumber = latest.roomNumber,
                            floorName = latest.floorName
                        )
                        viewModelScope.launch {
                            paymentRepository.recordPayment(newPayment).onSuccess {
                                // Reload payments list once bill is successfully recorded
                                loadTenantPayments(userId, phone)
                            }
                        }
                    }
                }
        }
    }

    fun payRent(payment: RentPayment, transactionId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val updatedPayment = payment.copy(
                status = PaymentStatus.PAID,
                paidAmount = payment.amount,
                paidDate = java.time.LocalDate.now().toString(),
                transactionId = transactionId
            )
            val result = paymentRepository.updatePayment(updatedPayment)
            result.onSuccess {
                // Fetch the tenant to link back to the logged in user ID for reloading
                val tenantObj = tenantRepository.getTenant(payment.tenantId)
                if (tenantObj?.userId != null) {
                    loadTenantPayments(tenantObj.userId)
                }
                onComplete()
            }
            _isLoading.value = false
        }
    }

    fun markAsPaid(payment: RentPayment, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val updatedPayment = payment.copy(
                status = PaymentStatus.PAID,
                paidAmount = payment.amount,
                paidDate = java.time.LocalDate.now().toString(),
                transactionId = "CASH_OFFLINE_" + java.util.UUID.randomUUID().toString().take(8).uppercase()
            )
            val result = paymentRepository.updatePayment(updatedPayment)
            result.onSuccess {
                // Reload
                loadPayments(payment.hostelId)
                onComplete()
            }
            _isLoading.value = false
        }
    }

    fun generateReceipt(payment: RentPayment) {
        viewModelScope.launch {
            _isLoading.value = true
            val updatedPayment = payment.copy(receiptGenerated = true)
            val result = paymentRepository.updatePayment(updatedPayment)
            result.onSuccess {
                // Reload
                loadPayments(payment.hostelId)
            }
            _isLoading.value = false
        }
    }

    fun sendPaymentReminder(payment: RentPayment, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            paymentRepository.sendPaymentReminder(payment).onSuccess {
                onComplete()
            }
            _isLoading.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        tenantPaymentsJob?.cancel()
        tenantPaymentsCollectorJob?.cancel()
    }
}
