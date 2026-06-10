package com.farmlens.anarai.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class OllamaRequest(
    val model: String = "llama3", // The default model to run on PC
    val prompt: String,
    val stream: Boolean = false
)

data class OllamaResponse(
    val response: String
)

interface OllamaApiService {
    @POST("api/generate")
    suspend fun generateRecommendation(@Body request: OllamaRequest): OllamaResponse
}

object OllamaClient {
    // 10.126.159.12 is the detected IP of the developer's PC on the local network.
    private const val BASE_URL = "http://10.126.159.12:11434/"

    val api: OllamaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OllamaApiService::class.java)
    }
}
