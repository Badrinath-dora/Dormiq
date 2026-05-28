package com.dormiq.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dormiq.domain.model.*
import com.dormiq.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TenantViewModel @Inject constructor(
    private val tenantRepository: TenantRepository,
    private val roomRepository: RoomRepository,
    private val issueRepository: IssueRepository,
    private val noticeRepository: NoticeRepository
) : ViewModel() {

    private val _tenant = MutableStateFlow<Tenant?>(null)
    val tenant: StateFlow<Tenant?> = _tenant.asStateFlow()

    private val _room = MutableStateFlow<Room?>(null)
    val room: StateFlow<Room?> = _room.asStateFlow()

    private val _issues = MutableStateFlow<List<Issue>>(emptyList())
    val issues: StateFlow<List<Issue>> = _issues.asStateFlow()

    private val _notices = MutableStateFlow<List<Notice>>(emptyList())
    val notices: StateFlow<List<Notice>> = _notices.asStateFlow()

    private val _roommates = MutableStateFlow<List<Tenant>>(emptyList())
    val roommates: StateFlow<List<Tenant>> = _roommates.asStateFlow()

    private val _allocation = MutableStateFlow<TenantAllocation?>(null)
    val allocation: StateFlow<TenantAllocation?> = _allocation.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun getTenants(hostelId: String): Flow<List<Tenant>> = tenantRepository.getTenants(hostelId)

    fun loadTenant(tenantId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val t = tenantRepository.getTenant(tenantId)
            _tenant.value = t
            _isLoading.value = false
        }
    }

    fun createTenant(tenant: Tenant, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = tenantRepository.createTenant(tenant)
            result.onSuccess {
                onComplete(it.id)
            }
            _isLoading.value = false
        }
    }

    fun assignBed(tenantId: String, bedId: String, roomId: String, hostelId: String, rentAmount: Double, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = tenantRepository.assignBed(tenantId, bedId, roomId, hostelId, rentAmount)
            result.onSuccess {
                onComplete()
            }
            _isLoading.value = false
        }
    }

    fun checkoutTenant(tenantId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            tenantRepository.checkoutTenant(tenantId).onSuccess {
                onComplete()
            }
            _isLoading.value = false
        }
    }

    fun updateTenant(tenant: Tenant, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            tenantRepository.updateTenant(tenant).onSuccess {
                onComplete()
            }
            _isLoading.value = false
        }
    }

    fun deleteTenant(tenantId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            tenantRepository.deleteTenant(tenantId).onSuccess {
                onComplete()
            }
            _isLoading.value = false
        }
    }

    fun loadTenantData(userId: String, phone: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            println("Loading tenant data for UserID: $userId, Phone: $phone")
            
            // 1. Try fetching by UserId first
            tenantRepository.getTenantByUserId(userId).collectLatest { tenantObj ->
                if (tenantObj != null) {
                    println("Found tenant by UserID: ${tenantObj.name}")
                    _tenant.value = tenantObj
                    processTenantData(tenantObj)
                    _isLoading.value = false
                } else if (phone != null) {
                    // 2. Fallback: Try fetching by phone if UserId lookup failed (not linked yet)
                    println("UserId lookup failed, trying phone: $phone")
                    val tenantByPhone = tenantRepository.getTenantByPhone(phone)
                    if (tenantByPhone != null) {
                        println("Found tenant by Phone: ${tenantByPhone.name}")
                        _tenant.value = tenantByPhone
                        processTenantData(tenantByPhone)
                        
                        // 3. Auto-link for next time
                        if (tenantByPhone.userId == null) {
                            println("Linking tenant ${tenantByPhone.id} to userId $userId")
                            tenantRepository.updateTenantUserId(tenantByPhone.id, userId)
                        }
                    } else {
                        println("No tenant found for phone: $phone")
                    }
                    _isLoading.value = false
                } else {
                    _isLoading.value = false
                }
            }
        }
    }

    private suspend fun processTenantData(tenantObj: Tenant) {
        val currentTenantId = tenantObj.id
        
        // 1. Try to get current allocation directly if room fields are missing on tenant object
        val allocation = tenantRepository.getCurrentAllocation(currentTenantId)
        _allocation.value = allocation
        
        if (allocation != null) {
            println("Found active allocation for room: ${allocation.roomNumber}")
            // If the tenant object is missing room info, we might need to refresh it or use allocation info
            loadRoomData(allocation.roomId)
            
            // Also load roommates based on the allocation's room info
            _tenant.value?.hostelId?.let { hId ->
                loadRoommates(hId, allocation.roomNumber, currentTenantId)
            }
        } else if (tenantObj.assignedRoomNumber != null) {
            // Fallback to tenant object's fields if no explicit allocation found
            println("No explicit allocation, using tenant room fields: ${tenantObj.assignedRoomNumber}")
            tenantObj.assignedBedId?.let { bedId -> 
                 // Try to find roomId from bedId if possible, or just use the room number
            }
            loadRoommates(tenantObj.hostelId ?: "", tenantObj.assignedRoomNumber!!, currentTenantId)
        } else {
            println("No room assignment found for tenant ${tenantObj.id}")
        }
    }

    private fun loadRoomData(roomId: String) {
        viewModelScope.launch {
            val roomObj = roomRepository.getRoom(roomId)
            _room.value = roomObj
        }
    }

    private var roommatesJob: kotlinx.coroutines.Job? = null
    private fun loadRoommates(hostelId: String, roomNumber: String, currentTenantId: String) {
        roommatesJob?.cancel()
        roommatesJob = viewModelScope.launch {
            tenantRepository.getTenantsByRoom(hostelId, roomNumber).collect { list ->
                // Filter out the current user from roommates
                _roommates.value = list.filter { it.id != currentTenantId }
            }
        }
    }

    private var homeDataJob: kotlinx.coroutines.Job? = null
    fun loadHomeData(hostelId: String, tenantId: String) {
        homeDataJob?.cancel()
        homeDataJob = viewModelScope.launch {
            _isLoading.value = true
            combine(
                issueRepository.getIssuesByTenant(tenantId),
                noticeRepository.getNotices(hostelId)
            ) { issueList, noticeList ->
                _issues.value = issueList
                _notices.value = noticeList
            }.collect {
                _isLoading.value = false
            }
        }
    }
}
