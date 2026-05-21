package com.dormiq.ui.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dormiq.domain.model.Hostel
import com.dormiq.domain.model.Floor
import com.dormiq.domain.model.Room
import com.dormiq.domain.model.Bed
import com.dormiq.domain.repository.AuthRepository
import com.dormiq.domain.repository.HostelRepository
import com.dormiq.domain.repository.FloorRepository
import com.dormiq.domain.repository.RoomRepository
import com.dormiq.domain.repository.BedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val hostelRepository: HostelRepository,
    private val floorRepository: FloorRepository,
    private val roomRepository: RoomRepository,
    private val bedRepository: BedRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _setupState = MutableStateFlow<SetupState>(SetupState.Idle)
    val setupState: StateFlow<SetupState> = _setupState

    // ─── Draft State (Persists when navigating back/forward) ──────────────────
    var hostelName by mutableStateOf("")
    var address by mutableStateOf("")
    var hostelType by mutableStateOf("Boys")
    var phone by mutableStateOf("")
    var email by mutableStateOf("")

    val floorNames = mutableStateListOf("Ground Floor", "1st Floor", "2nd Floor")
    
    // For simplicity, we'll store rooms and beds for the setup wizard
    val rooms = mutableStateListOf(
        RoomDraft("G01", "Standard", 4),
        RoomDraft("G02", "Deluxe", 2)
    )
    
    val beds = mutableStateListOf("Bed A", "Bed B", "Bed C", "Bed D")
    var bedPrice by mutableStateOf("7500")

    // ─── Persistence ──────────────────────────────────────────────────────────

    private var createdHostelId: String? = null

    fun publishHostel(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _setupState.value = SetupState.Loading
            try {
                val user = authRepository.getCurrentUser() ?: throw Exception("Not logged in")
                val ownerId = user.id
                
                // 1. Get or Create Hostel ID
                val hostelId = createdHostelId ?: user.hostelId ?: run {
                    val hostel = Hostel(
                        name = hostelName,
                        address = address,
                        type = hostelType,
                        phone = phone,
                        email = email,
                        ownerId = ownerId
                    )
                    val result = hostelRepository.createHostel(hostel)
                    val created = result.getOrNull() ?: throw Exception("Failed to create hostel")
                    created.id
                }

                // 2. Create Floors
                floorNames.forEachIndexed { index, name ->
                    // Try to create or find existing floor
                    val floor = Floor(hostelId = hostelId, name = name, number = index)
                    var createdFloor = floorRepository.createFloor(floor).getOrNull()
                    
                    if (createdFloor == null) {
                        // Attempt to find existing floor by name if creation failed
                        createdFloor = floorRepository.getFloors(hostelId)
                            .first().find { it.name == name }
                    }

                    if (createdFloor != null) {
                        val floorId = createdFloor.id
                        
                        // 3. Create Rooms
                        rooms.forEach { roomDraft ->
                            // Determine if this room belongs to the current floor
                            val belongsToThisFloor = when {
                                // Match Ground Floor / floor index 0
                                index == 0 -> {
                                    roomDraft.number.startsWith("G", ignoreCase = true) ||
                                    roomDraft.number.startsWith("0") ||
                                    // if it starts with a number, but doesn't start with any other floor's index
                                    (roomDraft.number.firstOrNull()?.isDigit() == true && 
                                     floorNames.indices.none { fIdx -> fIdx > 0 && roomDraft.number.startsWith(fIdx.toString()) })
                                }
                                // Match other floors by index (e.g. First floor index 1 matches "101", "102")
                                else -> {
                                    roomDraft.number.startsWith(index.toString()) ||
                                    roomDraft.number.startsWith("F$index", ignoreCase = true) ||
                                    roomDraft.number.startsWith("Floor $index", ignoreCase = true)
                                }
                            }
                            
                            if (!belongsToThisFloor) return@forEach

                            val room = Room(
                                hostelId = hostelId,
                                floorId = floorId,
                                roomNumber = roomDraft.number,
                                type = roomDraft.type,
                                capacity = roomDraft.capacity
                            )
                            var createdRoom = roomRepository.createRoom(room).getOrNull()
                            
                            if (createdRoom == null) {
                                // Attempt to find existing room by number
                                createdRoom = roomRepository.getRoomsByFloor(floorId)
                                    .first().find { it.roomNumber == roomDraft.number }
                            }

                            if (createdRoom != null) {
                                val roomId = createdRoom.id
                                
                                // 4. Create Beds for this room
                                beds.forEach { bedLabel ->
                                    val bed = Bed(
                                        hostelId = hostelId, 
                                        roomId = roomId, 
                                        label = bedLabel,
                                        status = com.dormiq.domain.model.BedStatus.VACANT,
                                        price = bedPrice.toDoubleOrNull() ?: 0.0
                                    )
                                    // Ignore failures for individual beds (e.g. if some already exist)
                                    bedRepository.createBed(bed)
                                }
                            }
                        }
                    }
                }

                // Finalize registration
                authRepository.completeRegistration(hostelId)
                
                _setupState.value = SetupState.Success
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                _setupState.value = SetupState.Error(e.message ?: "Unknown error during publish")
            }
        }
    }

    fun createHostel(onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _setupState.value = SetupState.Loading
            var ownerId: String? = null
            try {
                ownerId = authRepository.getCurrentUser()?.id ?: throw Exception("Not logged in")
                val hostel = Hostel(
                    name = hostelName,
                    address = address,
                    type = hostelType,
                    phone = phone,
                    email = email,
                    ownerId = ownerId
                )
                val result = hostelRepository.createHostel(hostel)
                val created = result.getOrNull() ?: throw Exception("Failed to create hostel")
                
                createdHostelId = created.id
                // Link hostel to owner profile immediately
                authRepository.completeRegistration(created.id)
                
                _setupState.value = SetupState.Success
                onSuccess(created.id)
            } catch (e: Exception) {
                // If it already exists, try to get the ID
                val existing = ownerId?.let { hostelRepository.getHostelByOwner(it).getOrNull() }
                if (existing != null) {
                    createdHostelId = existing.id
                    authRepository.completeRegistration(existing.id)
                    _setupState.value = SetupState.Success
                    onSuccess(existing.id)
                } else {
                    _setupState.value = SetupState.Error(e.message ?: "Failed to create hostel")
                }
            }
        }
    }
}

data class RoomDraft(val number: String, val type: String, val capacity: Int)


sealed class SetupState {
    object Idle : SetupState()
    object Loading : SetupState()
    object Success : SetupState()
    data class Error(val message: String) : SetupState()
}
