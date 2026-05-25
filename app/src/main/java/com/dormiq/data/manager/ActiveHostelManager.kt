package com.dormiq.data.manager

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActiveHostelManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("dormiq_hostel_preferences", Context.MODE_PRIVATE)

    private val _activeHostelIdFlow = MutableStateFlow<String?>(getActiveHostelId())
    val activeHostelIdFlow: StateFlow<String?> = _activeHostelIdFlow.asStateFlow()

    fun getActiveHostelId(): String? {
        return prefs.getString("active_hostel_id", null)
    }

    fun setActiveHostelId(hostelId: String?) {
        prefs.edit().putString("active_hostel_id", hostelId).apply()
        _activeHostelIdFlow.value = hostelId
    }

    fun clearActiveHostelId() {
        prefs.edit().remove("active_hostel_id").apply()
        _activeHostelIdFlow.value = null
    }
}
