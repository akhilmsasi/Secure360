package com.cet.secure360.localdatabase

import com.cet.secure360.UserRegistration
import com.cet.secure360.model.EventStatus
import com.cet.secure360.model.IncidentRecord
import com.cet.secure360.model.RecordingStatus
import com.cet.secure360.model.UpdateResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
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

    @GET("fetch_event_status.php")
    suspend fun getEventStatus(): Response<List<EventStatus>>

    @FormUrlEncoded
    @POST("set_event_status.php")
    suspend fun updateEventStatus(
        @Field("event_type") type: Int,
        @Field("event_status") status: Int
    ): Response<UpdateResponse>
}
