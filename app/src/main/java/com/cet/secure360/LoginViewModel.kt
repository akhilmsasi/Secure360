package com.cet.secure360

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cet.secure360.localdatabase.RetrofitClient
import com.cet.secure360.model.RecordingStatus
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val TAG = "LoginViewModel"
    private val dbRef = FirebaseDatabase.getInstance().getReference(FirebaseConfig.USERS_NODE)

    var isLoading by mutableStateOf(false)
    var loginError by mutableStateOf<String?>(null)

    fun loginUser(username: String, passwordEntered: String, context: Context, onComplete: (Boolean) -> Unit) {
        if (username.isBlank() || passwordEntered.isBlank()) {
            loginError = "Please fill all fields"
            onComplete(false)
            return
        }

        isLoading = true
        loginError = null

        dbRef.child(username).child("ProfileDetails").get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val user = snapshot.getValue(UserRegistration::class.java)
                
                if (user != null) {
                    if (user.password == passwordEntered) {
                        saveUserToPrefs(context, user){
                            success ->
                            isLoading = false
                            if (success){
                                onComplete(true)
                            } else {
                                onComplete(false)
                            }
                        }
                    } else {
                        loginError = "Invalid password"
                        isLoading = false
                        onComplete(false)
                    }
                } else {
                    loginError = "User data error"
                    isLoading = false
                    onComplete(false)
                }
            } else {
                loginError = "User does not exist"
                isLoading = false
                onComplete(false)
            }
        }.addOnFailureListener {
            loginError = "Login failed: ${it.message}"
            isLoading = false
            onComplete(false)
        }
    }

    private fun saveUserToPrefs(context: Context, user: UserRegistration,
                                onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.syncUserToMySQL(user)
                if (response.isSuccessful) {
                    RetrofitClient.apiService.updateRecordingStatus(RecordingStatus(0))
                    RetrofitClient.apiService.createIncidentTable()
                    Log.d(TAG, "updateToLocalDatabase: Success! MySQL Updated.")
                    val sharedPref = context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putString("username", user.username)
                        putString("name", user.name)
                        putString("email", user.email)
                        putString("vehicleNumber", user.vehicleNumber)
                        putBoolean("isLoggedIn", true)
                        apply()
                        onComplete(true)
                    }
                } else {
                    onComplete(false)
                    Log.d(TAG, "updateToLocalDatabase: Failed Code: ${response.code()}")
                }
            } catch (e: Exception) {
                onComplete(false)
                Log.e(TAG, "updateToLocalDatabase: Network/Parsing Error", e)
            }
        }
    }
}
