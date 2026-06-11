package com.farmlens.anarai.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class WeatherResponse(
    val current_condition: List<CurrentCondition>
)

data class CurrentCondition(
    val temp_C: String,
    val humidity: String
)

interface WeatherService {
    @GET("{lat},{lon}?format=j1")
    suspend fun getCurrentWeather(
        @retrofit2.http.Path("lat") lat: Double,
        @retrofit2.http.Path("lon") lon: Double
    ): WeatherResponse

    companion object {
        fun create(): WeatherService {
            return Retrofit.Builder()
                .baseUrl("https://wttr.in/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WeatherService::class.java)
        }
    }
}
