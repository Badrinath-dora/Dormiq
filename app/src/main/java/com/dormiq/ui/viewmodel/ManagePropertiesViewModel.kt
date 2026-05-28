package com.dormiq.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dormiq.domain.model.Hostel
import com.dormiq.domain.repository.AuthRepository
import com.dormiq.domain.repository.HostelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManagePropertiesViewModel @Inject constructor(
    private val hostelRepository: HostelRepository,
    private val authRepository: AuthRepository,
    private val activeHostelManager: com.dormiq.data.manager.ActiveHostelManager
) : ViewModel() {

    private val _hostels = MutableStateFlow<List<Hostel>>(emptyList())
    val hostels: StateFlow<List<Hostel>> = _hostels.asStateFlow()

    private val _activeHostelId = MutableStateFlow<String?>(activeHostelManager.getActiveHostelId())
    val activeHostelId: StateFlow<String?> = _activeHostelId.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadHostels()
        viewModelScope.launch {
            activeHostelManager.activeHostelIdFlow.collect {
                _activeHostelId.value = it
            }
        }
    }

    fun loadHostels() {
        viewModelScope.launch {
            _isLoading.value = true
            val ownerId = authRepository.getCurrentUser()?.id
            if (ownerId != null) {
                hostelRepository.getHostelsByOwner(ownerId).collect { list ->
                    val updatedList = list.map { hostel ->
                        try {
                            val statsPair = hostelRepository.getHostelStatsDirect(hostel.id)
                            hostel.copy(
                                totalFloors = statsPair.first,
                                totalRooms = statsPair.second
                            )
                        } catch (e: Exception) {
                            hostel
                        }
                    }
                    
                    val groupedHostels = updatedList.groupBy { it.name.trim().lowercase() }
                    val filteredList = mutableListOf<Hostel>()
                    
                    groupedHostels.forEach { (_, group) ->
                        if (group.size > 1) {
                            val realHostel = group.find { it.totalFloors > 0 || it.totalRooms > 0 || it.id == _activeHostelId.value }
                            if (realHostel != null) {
                                group.forEach { hostel ->
                                    if (hostel.id != realHostel.id && hostel.totalFloors == 0 && hostel.totalRooms == 0) {
                                        viewModelScope.launch {
                                            hostelRepository.deleteHostel(hostel.id)
                                        }
                                    } else {
                                        filteredList.add(hostel)
                                    }
                                }
                            } else {
                                val firstHostel = group.first()
                                filteredList.add(firstHostel)
                                group.drop(1).forEach { hostel ->
                                    viewModelScope.launch {
                                        hostelRepository.deleteHostel(hostel.id)
                                    }
                                }
                            }
                        } else {
                            filteredList.addAll(group)
                        }
                    }
                    
                    _hostels.value = filteredList
                    _isLoading.value = false
                }
            } else {
                _isLoading.value = false
            }
        }
    }

    fun switchHostel(hostelId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            authRepository.switchActiveHostel(hostelId).onSuccess {
                _activeHostelId.value = hostelId
                onComplete()
            }
            _isLoading.value = false
        }
    }
}
