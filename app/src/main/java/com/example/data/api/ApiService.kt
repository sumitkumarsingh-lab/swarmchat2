package com.example.data.api

import com.example.data.model.ChatRequest
import com.example.data.model.ChatResponse
import com.example.data.model.LoginRequest
import com.example.data.model.LoginResponse
import com.example.data.model.RegistrationRequest
import com.example.data.model.RegistrationResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

/**
 * Retrofit contract endpoints aligned with the FastAPI Swarm backend actions.
 */
interface ApiService {
    @GET("health")
    suspend fun healthCheck(): Map<String, String>

    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/register")
    suspend fun register(@Body request: RegistrationRequest): RegistrationResponse

    @POST("api/chat")
    suspend fun chat(@Body request: ChatRequest): ChatResponse
}

/**
 * Singleton client instantiator. Utilizes the dynamic base URL interceptor
 * to automatically switch endpoints at runtime if the target IP is updated.
 */
object RetrofitClient {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(DynamicBaseUrlInterceptor())
        .addInterceptor(loggingInterceptor)
        .connectTimeout(45, TimeUnit.SECONDS) // Generous timeouts for multi-agent loops
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConfig.baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService::class.java)
    }
}
