package com.cet.secure360.model

import com.google.gson.annotations.SerializedName

data class RecordingStatus(
    val status: Int = 0,
    @SerializedName("event_type")
    val eventType: Int = 1, // Default to 1q (Idle/Normal)

    @SerializedName("requested_username")
    val requestedUsername: String = "NULL"
)