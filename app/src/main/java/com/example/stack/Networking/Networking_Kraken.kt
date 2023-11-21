package com.example.stack.Networking

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class KrakenAPIFetchingUseCase(private val repository: KrakenRepository) {
    suspend fun kraken_api_execute(pair: String): KrakenResponse {
        return repository.getKrakenData(pair)
    }
}

fun Retrofit.Builder.createKrakenRepository(): KrakenRepository {
    return baseUrl("https://api.kraken.com")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(KrakenApiService::class.java)
        .let { KrakenRepository(it) }
}

interface KrakenApiService {
    @GET("0/public/Ticker")
    suspend fun getKrakenPrice(@Query("pair") pair: String): KrakenResponse
}

class KrakenRepository(private val apiService: KrakenApiService) {
    suspend fun getKrakenData(pair: String): KrakenResponse {
        return apiService.getKrakenPrice(pair)
    }
}

data class KrakenResponse(val error: List<String>, val result: KrakenResult)
data class KrakenResult(val XXBTZUSD: KrakenBitcoin)
data class KrakenBitcoin(val c: List<String>)

