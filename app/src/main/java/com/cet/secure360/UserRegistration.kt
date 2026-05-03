package com.cet.secure360

// Data Model
data class UserRegistration(
    val name: String = "",
    val address: String = "",
    val email: String = "",
    val contactNumber: String = "",
    val altContactNumber: String = "",
    val vehicleNumber: String = "",
    val vehicleModel: String = "",
    val vehicleColor: String = "",
    val username: String = "", // Primary Key
    val password: String = ""
)
