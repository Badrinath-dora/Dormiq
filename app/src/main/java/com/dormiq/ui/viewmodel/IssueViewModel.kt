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
class IssueViewModel @Inject constructor(
    private val repository: IssueRepository
) : ViewModel() {

    private val _issues = MutableStateFlow<List<Issue>>(emptyList())
    val issues: StateFlow<List<Issue>> = _issues.asStateFlow()

    private val _selectedIssue = MutableStateFlow<Issue?>(null)
    val selectedIssue: StateFlow<Issue?> = _selectedIssue.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _comments = MutableStateFlow<List<IssueComment>>(emptyList())
    val comments: StateFlow<List<IssueComment>> = _comments.asStateFlow()

    private var lastHostelId: String? = null
    private var lastIsOwner: Boolean = false
    private var lastTenantId: String? = null

    private var issuesJob: kotlinx.coroutines.Job? = null

    fun loadIssues(hostelId: String, isOwner: Boolean, tenantId: String? = null) {
        lastHostelId = hostelId
        lastIsOwner = isOwner
        lastTenantId = tenantId
        issuesJob?.cancel()
        issuesJob = viewModelScope.launch {
            _isLoading.value = true
            if (isOwner) {
                repository.getIssues(hostelId).collect {
                    _issues.value = it
                    _isLoading.value = false
                }
            } else if (tenantId != null) {
                repository.getIssuesByTenant(tenantId).collect {
                    _issues.value = it
                    _isLoading.value = false
                }
            }
        }
    }

    private var issueDetailJob: kotlinx.coroutines.Job? = null

    fun loadIssue(issueId: String) {
        issueDetailJob?.cancel()
        issueDetailJob = viewModelScope.launch {
            _isLoading.value = true
            loadComments(issueId)
            
            // Poll for issue details to catch status/note updates in real time
            while (true) {
                try {
                    val issue = repository.getIssue(issueId)
                    _selectedIssue.value = issue
                } catch (e: Exception) {
                    if (e !is kotlinx.coroutines.CancellationException) {
                        e.printStackTrace()
                    }
                }
                _isLoading.value = false
                kotlinx.coroutines.delay(4000)
            }
        }
    }

    private var pollingJob: kotlinx.coroutines.Job? = null

    fun loadComments(issueId: String) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            repository.getComments(issueId).collect {
                _comments.value = it.sortedBy { c -> c.createdAt }
            }
        }
    }

    fun addComment(comment: IssueComment) {
        viewModelScope.launch {
            val result = repository.addComment(comment)
            if (result.isSuccess) {
                loadComments(comment.issueId)
            }
        }
    }

    fun updateIssueStatus(issueId: String, status: String, notes: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.updateIssueStatus(issueId, status, notes)
            if (result.isSuccess) {
                // Instantly update local detail state so UI re-renders reactively
                val updatedIssue = repository.getIssue(issueId)
                _selectedIssue.value = updatedIssue
                
                // Instantly refresh list so going back reflects status
                lastHostelId?.let { hId ->
                    loadIssues(hId, lastIsOwner, lastTenantId)
                }
            }
            _isLoading.value = false
            onComplete(result.isSuccess)
        }
    }

    fun reportIssue(issue: Issue, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.createIssue(issue)
            _isLoading.value = false
            onComplete(result.isSuccess)
        }
    }

    override fun onCleared() {
        super.onCleared()
        issuesJob?.cancel()
        issueDetailJob?.cancel()
        pollingJob?.cancel()
    }
}
