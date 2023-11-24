package com.example.stack.Networking

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class ForexDataFetchingUseCase(private val repository: ForexRepository) {
    suspend fun open_er_api_execute(): ForexResponse {
        return repository.getForexData()
    }
}

fun Retrofit.Builder.createForexRepository(): ForexRepository {
    return baseUrl("https://open.er-api.com")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(OpenErApiService::class.java)
        .let { ForexRepository(it) }
}

interface OpenErApiService {
    @GET("/v6/latest/USD")
    suspend fun getForexRates(): ForexResponse
}

class ForexRepository(private val apiService: OpenErApiService) {
    suspend fun getForexData(): ForexResponse {
        return apiService.getForexRates()
    }
}

data class ForexResponse(val rates: Rates)
data class Rates(val CAD: Double?, val EUR: Double?, val JPY: Double?)