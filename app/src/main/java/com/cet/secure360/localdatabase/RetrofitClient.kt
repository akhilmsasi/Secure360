package com.cet.secure360.localdatabase

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // IMPORTANT: Ensure this matches the IP in your screenshot (192.168.1.40)
    // Updated BASE_URL to point to the correct directory containing your PHP files.
    private const val BASE_URL = "http://192.168.1.38/my_app/"

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}
