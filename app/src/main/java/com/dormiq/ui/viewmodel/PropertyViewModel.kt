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
class PropertyViewModel @Inject constructor(
    private val floorRepository: FloorRepository,
    private val roomRepository: RoomRepository,
    private val bedRepository: BedRepository,
    private val tenantRepository: TenantRepository
) : ViewModel() {

    private val _floors = MutableStateFlow<List<Floor>>(emptyList())
    val floors: StateFlow<List<Floor>> = _floors.asStateFlow()

    private val _rooms = MutableStateFlow<List<Room>>(emptyList())
    val rooms: StateFlow<List<Room>> = _rooms.asStateFlow()

    private val _beds = MutableStateFlow<List<Bed>>(emptyList())
    val beds: StateFlow<List<Bed>> = _beds.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadFloors(hostelId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            floorRepository.getFloors(hostelId)
                .collect { 
                    _floors.value = it
                    _isLoading.value = false
                }
        }
    }

    fun loadRooms(hostelId: String, floorId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            combine(
                floorRepository.getFloors(hostelId),
                roomRepository.getRooms(hostelId)
            ) { floors, rooms ->
                val sortedFloors = floors.sortedBy { it.number }
                val floorIndex = sortedFloors.indexOfFirst { it.id == floorId }
                if (floorIndex == -1) return@combine emptyList<Room>()
                
                rooms.filter { room ->
                    val belongsToThisFloor = when {
                        floorIndex == 0 -> {
                            room.roomNumber.startsWith("G", ignoreCase = true) ||
                            room.roomNumber.startsWith("0") ||
                            (room.roomNumber.firstOrNull()?.isDigit() == true && 
                             sortedFloors.indices.none { fIdx -> fIdx > 0 && room.roomNumber.startsWith(fIdx.toString()) })
                        }
                        else -> {
                            room.roomNumber.startsWith(floorIndex.toString()) ||
                            room.roomNumber.startsWith("F$floorIndex", ignoreCase = true) ||
                            room.roomNumber.startsWith("Floor $floorIndex", ignoreCase = true)
                        }
                    }
                    belongsToThisFloor
                }
            }.collect { 
                _rooms.value = it
                _isLoading.value = false
            }
        }
    }

    fun addFloor(hostelId: String, name: String, number: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val floor = Floor(hostelId = hostelId, name = name, number = number)
            floorRepository.createFloor(floor)
            loadFloors(hostelId)
        }
    }

    fun addRoom(hostelId: String, floorId: String, number: String, type: String, capacity: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val room = Room(
                hostelId = hostelId,
                floorId = floorId,
                roomNumber = number,
                type = type,
                capacity = capacity,
                totalBeds = capacity
            )
            roomRepository.createRoom(room)
            loadRooms(hostelId, floorId)
        }
    }

    private val _currentRoom = MutableStateFlow<Room?>(null)
    val currentRoom: StateFlow<Room?> = _currentRoom.asStateFlow()

    private val _currentBed = MutableStateFlow<Bed?>(null)
    val currentBed: StateFlow<Bed?> = _currentBed.asStateFlow()
    
    private val _currentTenant = MutableStateFlow<com.dormiq.domain.model.Tenant?>(null)
    val currentTenant: StateFlow<com.dormiq.domain.model.Tenant?> = _currentTenant.asStateFlow()
    
    private val _bedAllocations = MutableStateFlow<List<Pair<com.dormiq.domain.model.TenantAllocation, com.dormiq.domain.model.Tenant?>>>(emptyList())
    val bedAllocations: StateFlow<List<Pair<com.dormiq.domain.model.TenantAllocation, com.dormiq.domain.model.Tenant?>>> = _bedAllocations.asStateFlow()

    fun loadBeds(roomId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            bedRepository.getBeds(roomId)
                .collect { 
                    _beds.value = it
                    _isLoading.value = false
                }
        }
    }
    
    fun loadBed(bedId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            var foundBed = _beds.value.find { it.id == bedId }
            if (foundBed == null) {
                foundBed = bedRepository.getBed(bedId)
            }
            _currentBed.value = foundBed
            
            if (foundBed?.currentTenantId != null) {
                _currentTenant.value = tenantRepository.getTenant(foundBed.currentTenantId!!)
            } else {
                _currentTenant.value = null
            }
            
            val allocations = tenantRepository.getBedAllocations(bedId)
            val pairs = allocations.map { alloc ->
                val t = tenantRepository.getTenant(alloc.tenantId)
                alloc to t
            }
            _bedAllocations.value = pairs
            
            _isLoading.value = false
        }
    }

    fun loadRoomDetails(roomId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val room = roomRepository.getRoom(roomId)
            _currentRoom.value = room
            if (room != null) {
                bedRepository.getBeds(roomId).collect {
                    _beds.value = it
                }
            }
            _isLoading.value = false
        }
    }

    fun addBed(hostelId: String, roomId: String, label: String, price: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            val bed = Bed(
                hostelId = hostelId,
                roomId = roomId,
                label = label,
                price = price
            )
            bedRepository.createBed(bed)
            loadBeds(roomId)
        }
    }

    fun checkoutTenant(tenantId: String, bedId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            tenantRepository.checkoutTenant(tenantId).onSuccess {
                loadBed(bedId)
                onComplete()
            }
            _isLoading.value = false
        }
    }

    fun updateBedStatus(bedId: String, status: String) {
        viewModelScope.launch {
            _isLoading.value = true
            bedRepository.updateBedStatus(bedId, status).onSuccess {
                loadBed(bedId)
            }
            _isLoading.value = false
        }
    }
}
