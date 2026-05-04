package com.cet.secure360.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cet.secure360.localdatabase.ApiService
import com.cet.secure360.model.EventStatus
import com.cet.secure360.model.IncidentRecord
import com.cet.secure360.model.RecordingStatus
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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

    private val _username = MutableStateFlow<String?>(null)
    val username = _username.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _incidents = MutableStateFlow<List<IncidentRecord>>(emptyList())
    val incidents: StateFlow<List<IncidentRecord>> = _incidents

    private val _eventStatuses = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val eventStatuses = _eventStatuses.asStateFlow()

    init {
        // Initial fetch
        refreshStatus()
        // Start polling workers
        startAutoCheck()
        startMonitoring()
        startEventStatusMonitoring()
    }

    private fun startRecordingStatusMonitoring() {

        Log.d(TAG, "startRecordingStatusMonitoring: Entry UserName :: ${_username.value}")

        val database = FirebaseDatabase.getInstance().reference
        if (_username.value!=null){
            database.child("users").child(_username.value!!)
                .child("RecordingUpdateFromApp")
                .child("isRecording").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot) {

                        Log.d(TAG, "startRecordingStatusMonitoring: Status :: $p0")

                        setRecordingStatus(if (_recordingStatus.value == 1) 0 else 1)
                    }

                    override fun onCancelled(p0: DatabaseError) {
//                        TODO("Not yet implemented")
                    }
                })
        }
    }

    /**
     * Public method to refresh status manually.
     */
    fun refreshStatus() {
        viewModelScope.launch {
            fetchRecordingStatusInternal()
        }
    }

    /**
     * Internal suspend function to fetch status. 
     * Skips if a manual update is in progress to avoid race conditions.
     */
    private suspend fun fetchRecordingStatusInternal() {
        if (_isLoading.value) return

        try {
            val response = apiService.getRecordingStatus()
            if (response.isSuccessful) {
                response.body()?.let {
                    // Double check isLoading to ensure no manual update started during the network call
                    if (!_isLoading.value) {
                        _recordingStatus.value = it.status
                    }
                }
            } else {
                Log.e(TAG, "Server error during refresh: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network failure during refresh: ${e.message}")
        }
    }

    fun startAutoCheck() {
        viewModelScope.launch {
            while (true) {
                fetchRecordingStatusInternal()
                delay(1500) // Poll every 1.5 seconds (less aggressive than 500ms)
            }
        }
    }

    fun setRecordingStatus(newStatus: Int) {
        Log.d(TAG, "setRecordingStatus: Entry :: $newStatus")
        viewModelScope.launch {
            _isLoading.value = true
            
            // Optimistic UI update: Assume success for better responsiveness
            val previousStatus = _recordingStatus.value
            _recordingStatus.value = newStatus
            
            try {
                val statusObject = RecordingStatus(status = newStatus)
                val response = apiService.updateRecordingStatus(statusObject)

                Log.d(TAG, "setRecordingStatus: Update Recording status :: ${response.isSuccessful}")


                if (!response.isSuccessful) {
                    // Rollback if server update failed
                    _recordingStatus.value = previousStatus
                    _errorMessage.value = "Server error: ${response.code()}"
                }
            } catch (e: Exception) {
                // Rollback if network failed
                _recordingStatus.value = previousStatus
                _errorMessage.value = "Failed to update: ${e.message}"
            } finally {
                // Stay in loading state for a short cooldown to let server state stabilize 
                // and avoid immediate polling overwriting our change.
                delay(1000)
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
                delay(2000) // Polls every 2 seconds
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
                delay(2000) // Poll every 2 seconds
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

    fun loadUsername(context: Context) {
        val sharedPref = context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)

        val newUsername = sharedPref.getString("username", null)

        if (_username.value != newUsername){
            _username.value = newUsername
            startRecordingStatusMonitoring()
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
