package com.cet.secure360

// Firebase Constants
object FirebaseConfig {
    const val USERS_NODE = "users"
}

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