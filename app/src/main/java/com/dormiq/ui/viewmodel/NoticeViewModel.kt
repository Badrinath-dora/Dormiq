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
class NoticeViewModel @Inject constructor(
    private val repository: NoticeRepository
) : ViewModel() {

    private val _notices = MutableStateFlow<List<Notice>>(emptyList())
    val notices: StateFlow<List<Notice>> = _notices.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadNotices(hostelId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getNotices(hostelId).collect {
                _notices.value = it
                _isLoading.value = false
            }
        }
    }

    fun createNotice(notice: Notice, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.createNotice(notice)
            _isLoading.value = false
            onComplete(result.isSuccess)
        }
    }
}
