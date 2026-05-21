package com.dormiq.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dormiq.domain.model.Floor
import com.dormiq.domain.model.Room
import com.dormiq.domain.model.Bed
import com.dormiq.domain.repository.FloorRepository
import com.dormiq.domain.repository.RoomRepository
import com.dormiq.domain.repository.BedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssignBedViewModel @Inject constructor(
    private val floorRepository: FloorRepository,
    private val roomRepository: RoomRepository,
    private val bedRepository: BedRepository
) : ViewModel() {

    private val _hostelId = MutableStateFlow("")
    
    val floors = _hostelId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList()) else floorRepository.getFloors(id)
    }

    private val _selectedFloorId = MutableStateFlow("")
    val rooms = _selectedFloorId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList()) else roomRepository.getRoomsByFloor(id)
    }

    private val _selectedRoomId = MutableStateFlow("")
    val vacantBeds = _selectedRoomId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList()) else bedRepository.getVacantBeds(id)
    }

    fun setHostelId(id: String) {
        _hostelId.value = id
    }

    fun selectFloor(id: String) {
        _selectedFloorId.value = id
        _selectedRoomId.value = ""
    }

    fun selectRoom(id: String) {
        _selectedRoomId.value = id
    }
}
