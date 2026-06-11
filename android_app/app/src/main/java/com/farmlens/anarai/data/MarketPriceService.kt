package com.farmlens.anarai.data

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

data class AgmarknetRequest(
    val from_date: String,
    val to_date: String,
    val data_type: String = "100006",
    val group: String = "5",
    val commodity: String = "160",
    val state: String = "[20]", // Maharashtra
    val district: String = "[100001]",
    val market: String = "[100002]",
    val grade: String = "[100003]",
    val variety: String = "[100007]",
    val page: String = "1",
    val limit: String = "10"
)

data class AgmarknetResponse(
    val status: Boolean,
    val message: String,
    val data: AgmarknetData?
)

data class AgmarknetData(
    val records: List<AgmarknetRecord>
)

data class AgmarknetRecord(
    val data: List<MarketPriceDto>
)

data class MarketPriceDto(
    val cmdt_name: String?,
    val state_name: String?,
    val district_name: String?,
    val market_name: String?,
    val variety_name: String?,
    val grade_name: String?,
    val arrival_qty: String?,
    val min_price: String?,
    val max_price: String?,
    val model_price: String?,
    val arrival_date: String?
)

interface MarketPriceService {
    @Headers("Content-Type: application/json")
    @POST("v1/daily-price-arrival/report")
    suspend fun getLivePrices(@Body request: AgmarknetRequest): AgmarknetResponse

    companion object {
        fun create(): MarketPriceService {
            val client = OkHttpClient.Builder().build()
            return Retrofit.Builder()
                .baseUrl("https://api.agmarknet.gov.in/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MarketPriceService::class.java)
        }
    }
}
