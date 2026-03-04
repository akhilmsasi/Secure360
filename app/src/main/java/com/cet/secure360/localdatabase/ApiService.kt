package com.cet.secure360.localdatabase

import com.cet.secure360.UserRegistration
import com.cet.secure360.model.IncidentRecord
import com.cet.secure360.model.RecordingStatus
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("user_registration.php")
    suspend fun syncUserToMySQL(@Body user: UserRegistration): Response<Any>

    @POST("recording_status.php")
    suspend fun updateRecordingStatus(@Body status: RecordingStatus): Response<Any>

    @GET("get_recording_status.php")
    suspend fun getRecordingStatus(): Response<RecordingStatus>

    @GET("get_recording_status.php") // Assuming this for incident table creation or change if needed
    suspend fun createIncidentTable(): Response<Any>

    @GET("fetch_incidents.php")
    suspend fun getIncidents(): Response<List<IncidentRecord>>
}
