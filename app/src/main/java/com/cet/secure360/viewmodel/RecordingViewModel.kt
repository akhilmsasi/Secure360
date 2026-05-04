package com.cet.secure360.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cet.secure360.localdatabase.ApiService
import com.cet.secure360.model.CrashData
import com.cet.secure360.model.IncidentRecord
import com.cet.secure360.model.RecordingStatus
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

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

    // Firebase references / listeners
    private var eventStatusDbRef: DatabaseReference? = null
    private var eventStatusDbListener: ValueEventListener? = null

    // Track event types that are currently being updated locally to avoid overwrites
    private val updatingEventTypes = mutableSetOf<Int>()

    init {
        // Initial fetch
        refreshStatus()
        // Start polling workers
        startAutoCheck()
        startMonitoring()
        startEventStatusMonitoring()
        startCrashEventMonitoring()
    }

    private fun startRecordingStatusMonitoring() {
        Log.d(TAG, "startRecordingStatusMonitoring: Entry UserName :: ${_username.value}")

        val database = FirebaseDatabase.getInstance().reference
        val user = _username.value
        if (user != null) {
            database.child("users").child(user)
                .child("RecordingUpdateFromApp")
                .child("isRecording").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot) {
                        Log.d(TAG, "startRecordingStatusMonitoring: Status :: $p0")
                        val valObj = p0.getValue()
                        val newStatus = when (valObj) {
                            is Long -> valObj.toInt()
                            is Int -> valObj
                            is String -> valObj.toIntOrNull()
                            else -> null
                        }
                        if (newStatus != null) {
                            setRecordingStatus(newStatus)
                        } else {
                            setRecordingStatus(if (_recordingStatus.value == 1) 0 else 1)
                        }
                    }

                    override fun onCancelled(p0: DatabaseError) {}
                })
        }
    }

    private fun startCrashEventMonitoring() {
        Log.d(TAG, "startCrashEventMonitoring: Entry UserName :: ${_username.value}")

        val database = FirebaseDatabase.getInstance().reference
        database.child("CrashEvents").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                for (snapshot in p0.children) {
                    val data = snapshot.getValue(CrashData::class.java)
                    if (data?.lat == null || data.long == null) continue
                    val distance = calculateDistance(data.lat, data.long)
                    Log.d(TAG, "onDataChange: Distance :: $distance")
                    if (_recordingStatus.value == 0 && distance < 1) {
                        Log.d(TAG, "onDataChange: CrashEvent :: Recording started for crash event")
                        setRecordingStatus(1)
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double = 8.5458566, lon2: Double = 76.9037658
    ): Double {
        val earthRadius = 6371.0 // Radius of the earth in km

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
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
                delay(1500)
            }
        }
    }

    fun setRecordingStatus(newStatus: Int) {
        Log.d(TAG, "setRecordingStatus: Entry :: $newStatus")
        viewModelScope.launch {
            _isLoading.value = true

            val previousStatus = _recordingStatus.value
            _recordingStatus.value = newStatus

            try {
                val statusObject = RecordingStatus(status = newStatus)
                val response = apiService.updateRecordingStatus(statusObject)

                Log.d(TAG, "setRecordingStatus: Update Recording status :: ${response.isSuccessful}")

                if (!response.isSuccessful) {
                    _recordingStatus.value = previousStatus
                    _errorMessage.value = "Server error: ${response.code()}"
                }
            } catch (e: Exception) {
                _recordingStatus.value = previousStatus
                _errorMessage.value = "Failed to update: ${e.message}"
            } finally {
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
                delay(2000)
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
                            // Merge fetched statuses with locally-updated ones while protecting
                            // event types that are actively being updated locally.
                            val fetched = list.associate { it.eventType to it.eventStatus }
                            val merged = _eventStatuses.value.toMutableMap()
                            var changed = false
                            for ((k, v) in fetched) {
                                if (updatingEventTypes.contains(k)) continue
                                if (merged[k] != v) {
                                    merged[k] = v
                                    changed = true
                                }
                            }
                            if (changed) _eventStatuses.value = merged
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching event statuses", e)
                }
                delay(2000)
            }
        }
    }

    fun updateEventStatus(eventType: Int, newStatus: Int) {
        viewModelScope.launch {
            // mark this event as updating so polling/listeners don't overwrite immediately
            updatingEventTypes.add(eventType)

            try {
                val response = apiService.updateEventStatus(eventType, newStatus)
                if (response.isSuccessful) {
                    // Update local state immediately for better UI response
                    val currentMap = _eventStatuses.value.toMutableMap()
                    currentMap[eventType] = newStatus
                    _eventStatuses.value = currentMap

                    // Also update Firebase realtime DB so other clients see changes instantly
                    try {
                        val user = _username.value
                        if (user != null) {
                            val database = FirebaseDatabase.getInstance().reference
                            val eventKey = eventTypeToKey(eventType)
                            if (eventKey != null) {
                                database.child("users").child(user)
                                    .child("EventEnabledStatusSQLDB")
                                    .child(eventKey)
                                    .setValue(newStatus)
                            }
                        }
                    } catch (fe: Exception) {
                        Log.e(TAG, "Failed to write event status to Firebase", fe)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating event status", e)
            } finally {
                // keep marked as updating for a short cooldown so polling doesn't overwrite
                viewModelScope.launch {
                    try {
                        delay(1500)
                    } catch (_: Exception) {}
                    updatingEventTypes.remove(eventType)
                }
            }
        }
    }

    private fun eventTypeToKey(eventType: Int): String? {
        return when (eventType) {
            2 -> "FACE_DETECTION"
            3 -> "HONK_EVENT"
            4 -> "HARD_BRAKING"
            5 -> "ALARM"
            else -> null
        }
    }

    fun loadUsername(context: Context) {
        val sharedPref = context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
        val newUsername = sharedPref.getString("username", null)
        if (_username.value != newUsername) {
            _username.value = newUsername
            startRecordingStatusMonitoring()
            attachEventStatusFirebaseListener()
        }
    }

    private fun attachEventStatusFirebaseListener() {
        try {
            eventStatusDbListener?.let { listener ->
                eventStatusDbRef?.removeEventListener(listener)
            }

            val user = _username.value ?: return
            val database = FirebaseDatabase.getInstance().reference
            val ref = database.child("users").child(user).child("EventEnabledStatusSQLDB")
            eventStatusDbRef = ref

            eventStatusDbListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val fetched = mutableMapOf<Int, Int>()
                        for (child in snapshot.children) {
                            val key = child.key ?: continue
                            val value = when (val v = child.getValue()) {
                                is Long -> v.toInt()
                                is Int -> v
                                is Double -> v.toInt()
                                else -> null
                            }
                            val eventType = when (key.uppercase()) {
                                "FACE_DETECTION" -> 2
                                "HONK_EVENT" -> 3
                                "HARD_BRAKING" -> 4
                                "ALARM" -> 5
                                else -> null
                            }
                            if (eventType != null && value != null) {
                                fetched[eventType] = value
                            }
                        }

                        // For each fetched change originating from Firebase (external client),
                        // sync it to the local DB first, then update in-memory state. Skip any
                        // event types that are currently being updated locally to avoid loops.
                        val merged = _eventStatuses.value.toMutableMap()
                        for ((k, v) in fetched) {
                            if (updatingEventTypes.contains(k)) continue

                            val current = merged[k]
                            if (current == v) continue

                            // Synchronize change into local DB via API before applying locally
                            updatingEventTypes.add(k)
                            viewModelScope.launch {
                                try {
                                    val resp = try {
                                        apiService.updateEventStatus(k, v)
                                    } catch (ex: Exception) {
                                        Log.e(TAG, "API error syncing event from Firebase: $k", ex)
                                        null
                                    }

                                    if (resp != null && resp.isSuccessful) {
                                        // Apply the synced value into in-memory state
                                        val m = _eventStatuses.value.toMutableMap()
                                        m[k] = v
                                        _eventStatuses.value = m
                                    } else {
                                        Log.e(TAG, "Failed to sync event $k to local DB from Firebase")
                                    }
                                } finally {
                                    try { delay(500) } catch (_: Exception) {}
                                    updatingEventTypes.remove(k)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing Firebase event statuses", e)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Event status listener cancelled: ${error.message}")
                }
            }

            ref.addValueEventListener(eventStatusDbListener!!)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to attach Firebase event status listener", e)
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
