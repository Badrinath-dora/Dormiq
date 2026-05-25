package com.dormiq.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dormiq.domain.model.*
import com.dormiq.domain.repository.HostelRepository
import com.dormiq.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.dormiq.data.manager.ActiveHostelManager

data class BedWithTenant(
    val bed: Bed,
    val tenantName: String? = null,
    val tenantPhone: String? = null,
    val tenantEmail: String? = null,
    val tenantAvatarUrl: String? = null,
    val tenantId: String? = null
)

data class RoomWithBeds(
    val room: Room,
    val beds: List<BedWithTenant> = emptyList()
)

data class FloorWithRooms(
    val floor: Floor,
    val rooms: List<RoomWithBeds> = emptyList()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val hostelRepository: HostelRepository,
    private val authRepository: AuthRepository,
    private val activeHostelManager: ActiveHostelManager,
    private val supabase: SupabaseClient
) : ViewModel() {

    private val _hostel = MutableStateFlow<Hostel?>(null)
    val hostel: StateFlow<Hostel?> = _hostel

    private val _stats = MutableStateFlow(OccupancyStats(0, 0, 0, 0, 0, 0, 0, 0, 0))
    val stats: StateFlow<OccupancyStats> = _stats

    private val _issues = MutableStateFlow<List<Issue>>(emptyList())
    val issues: StateFlow<List<Issue>> = _issues.asStateFlow()

    private val _hostelLayout = MutableStateFlow<List<FloorWithRooms>>(emptyList())
    val hostelLayout: StateFlow<List<FloorWithRooms>> = _hostelLayout.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            activeHostelManager.activeHostelIdFlow.collect {
                loadDashboardData()
            }
        }
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _isLoading.value = true
            val activeHostelId = activeHostelManager.getActiveHostelId()
            if (activeHostelId != null) {
                val hostelData = hostelRepository.getHostel(activeHostelId)
                _hostel.value = hostelData
                if (hostelData != null) {
                    loadLayout(hostelData.id)
                    // Fetch recent issues
                    try {
                        val recentIssues = supabase.postgrest["issues"]
                            .select {
                                filter { eq("hostel_id", hostelData.id) }
                            }
                            .decodeList<Issue>()
                            .sortedByDescending { it.createdAt }
                            .take(3)
                        _issues.value = recentIssues
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    hostelRepository.getStats(hostelData.id).collect { newStats ->
                        _stats.value = newStats
                        _isLoading.value = false
                    }
                } else {
                    _isLoading.value = false
                }
            } else {
                val ownerId = authRepository.getCurrentUser()?.id ?: return@launch
                val hostelResult = hostelRepository.getHostelByOwner(ownerId)
                if (hostelResult.isSuccess) {
                    val hostelData = hostelResult.getOrNull()
                    _hostel.value = hostelData
                    if (hostelData != null) {
                        activeHostelManager.setActiveHostelId(hostelData.id)
                        loadLayout(hostelData.id)
                        // Fetch recent issues
                        try {
                            val recentIssues = supabase.postgrest["issues"]
                                .select {
                                    filter { eq("hostel_id", hostelData.id) }
                                }
                                .decodeList<Issue>()
                                .sortedByDescending { it.createdAt }
                                .take(3)
                            _issues.value = recentIssues
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        hostelRepository.getStats(hostelData.id).collect { newStats ->
                            _stats.value = newStats
                            _isLoading.value = false
                        }
                    } else {
                        _isLoading.value = false
                    }
                } else {
                    _isLoading.value = false
                }
            }
        }
    }

    fun loadLayout(hostelId: String) {
        viewModelScope.launch {
            try {
                // 1. Fetch floors
                val floors = supabase.postgrest["floors"]
                    .select { filter { eq("hostel_id", hostelId) } }
                    .decodeList<Floor>()
                    .sortedBy { it.number }

                // 2. Fetch rooms
                val rooms = supabase.postgrest["rooms"]
                    .select { filter { eq("hostel_id", hostelId) } }
                    .decodeList<Room>()
                    .sortedBy { it.roomNumber }

                // 3. Fetch beds
                val beds = supabase.postgrest["beds"]
                    .select { filter { eq("hostel_id", hostelId) } }
                    .decodeList<Bed>()
                    .sortedBy { it.label }

                // 4. Fetch tenants
                val tenants = supabase.postgrest["tenants"]
                    .select { filter { eq("hostel_id", hostelId) } }
                    .decodeList<Tenant>()

                val layout = floors.mapIndexed { index, floor ->
                    val floorRooms = rooms.filter { room ->
                        // Dynamically match room to floor by number prefix to correct any database misalignment
                        val belongsToThisFloor = when {
                            // Match Ground Floor / floor index 0
                            index == 0 -> {
                                room.roomNumber.startsWith("G", ignoreCase = true) ||
                                room.roomNumber.startsWith("0") ||
                                // if it starts with a number, but doesn't start with any other floor's index
                                (room.roomNumber.firstOrNull()?.isDigit() == true && 
                                 floors.indices.none { fIdx -> fIdx > 0 && room.roomNumber.startsWith(fIdx.toString()) })
                            }
                            // Match other floors by index (e.g. First floor index 1 matches "101", "102")
                            else -> {
                                room.roomNumber.startsWith(index.toString()) ||
                                room.roomNumber.startsWith("F$index", ignoreCase = true) ||
                                room.roomNumber.startsWith("Floor $index", ignoreCase = true)
                            }
                        }
                        belongsToThisFloor
                    }.map { room ->
                        val roomBeds = beds.filter { it.roomId == room.id }.map { bed ->
                            val tenant = tenants.find { it.assignedBedId == bed.id && it.status == TenantStatus.ACTIVE }
                            val isOccupied = tenant != null || !bed.currentTenantId.isNullOrEmpty()
                            val finalStatus = if (isOccupied) BedStatus.OCCUPIED else (bed.status ?: BedStatus.VACANT)
                            val finalBed = bed.copy(status = finalStatus)
                            
                            BedWithTenant(
                                bed = finalBed,
                                tenantName = tenant?.name ?: bed.currentTenantName,
                                tenantPhone = tenant?.phone,
                                tenantEmail = tenant?.email,
                                tenantAvatarUrl = tenant?.avatarUrl,
                                tenantId = tenant?.id
                            )
                        }
                        RoomWithBeds(room, roomBeds)
                    }
                    FloorWithRooms(floor, floorRooms)
                }

                _hostelLayout.value = layout
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
