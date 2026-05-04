package com.cet.secure360.model

import com.google.gson.annotations.SerializedName

data class EventStatus(
    @SerializedName("event_type")
    val eventType: Int,
    @SerializedName("event_status")
    val eventStatus: Int
)