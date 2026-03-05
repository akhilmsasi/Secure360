package com.cet.secure360.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cet.secure360.localdatabase.ApiService
import com.cet.secure360.model.EventStatus
import com.cet.secure360.model.IncidentRecord
import com.cet.secure360.model.RecordingStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecordingViewModel(private val apiService: ApiService) : ViewModel() {

    private val TAG = "RecordingViewModel"

    // Holds the current status (0 or 1)
    private val _recordingStatus = MutableStateFlow(0)
    val recordingStatus = _recordingStatus.asStateFlow()

    // Error handling state
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _incidents = MutableStateFlow<List<IncidentRecord>>(emptyList())
    val incidents: StateFlow<List<IncidentRecord>> = _incidents

    private val _eventStatuses = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val eventStatuses = _eventStatuses.asStateFlow()

    init {
        refreshStatus()
        startAutoCheck()
        startMonitoring()
        startEventStatusMonitoring()
    }

    // Function to fetch the value once
    fun refreshStatus() {
        viewModelScope.launch {
            try {
                val response = apiService.getRecordingStatus()
                if (response.isSuccessful) {
                    response.body()?.let {
                        _recordingStatus.value = it.status
                    }
                } else {
                    _errorMessage.value = "Server error: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network failure: ${e.message}"
            }
        }
    }

    fun startAutoCheck() {
        viewModelScope.launch {
            while (true) {
                refreshStatus()
                delay(500) // Poll every 500ms
            }
        }
    }

    fun setRecordingStatus(newStatus: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val statusObject = RecordingStatus(status = newStatus)
                val response = apiService.updateRecordingStatus(statusObject)

                if (response.isSuccessful) {
                    _recordingStatus.value = newStatus
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun startMonitoring() {
        viewModelScope.launch {
            while (true) {
                try {
                    val response = apiService.getIncidents()
                    if (response.isSuccessful) {
                        response.body()?.let { newList ->
                            if (_incidents.value != newList) {
                                _incidents.value = newList
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching incidents", e)
                }
                delay(500) // Polls every 500ms
            }
        }
    }

    private fun startEventStatusMonitoring() {
        viewModelScope.launch {
            while (true) {
                try {
                    val response = apiService.getEventStatus()
                    if (response.isSuccessful) {
                        response.body()?.let { list ->
                            val statusMap = list.associate { it.eventType to it.eventStatus }
                            if (_eventStatuses.value != statusMap) {
                                _eventStatuses.value = statusMap
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching event statuses", e)
                }
                delay(500)
            }
        }
    }

    fun updateEventStatus(eventType: Int, newStatus: Int) {
        viewModelScope.launch {
            try {
                val response = apiService.updateEventStatus(eventType, newStatus)
                if (response.isSuccessful) {
                    // Update local state immediately for better UI response
                    val currentMap = _eventStatuses.value.toMutableMap()
                    currentMap[eventType] = newStatus
                    _eventStatuses.value = currentMap
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating event status", e)
            }
        }
    }

    class Factory(private val apiService: ApiService) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RecordingViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RecordingViewModel(apiService) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
