package com.example.stack
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.stack.V.GridView
import com.example.stack.VM.GridViewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Red) {
                    val viewModel = remember {
                        GridViewModel(6, 10,
                            KrakenAPIFetchingUseCase(Retrofit.Builder().createKrakenRepository()),
                            ForexDataFetchingUseCase(Retrofit.Builder().createForexRepository())
                        )
                    }
                    val errorMessage by viewModel.errorMessage.collectAsState()
                    GridView(viewModel = viewModel)

                    errorMessage?.let { message ->
                        // Show a toast message for errors
                        Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}

fun Retrofit.Builder.createKrakenRepository(): KrakenRepository {
    return baseUrl("https://api.kraken.com")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(KrakenApiService::class.java)
        .let { KrakenRepository(it) }
}

fun Retrofit.Builder.createForexRepository(): ForexRepository {
    return baseUrl("https://open.er-api.com")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(OpenErApiService::class.java)
        .let { ForexRepository(it) }
}

// KRAKEN API -------------------------------------------------------------------

interface KrakenApiService {
    @GET("0/public/Ticker")
    suspend fun getKrakenPrice(@Query("pair") pair: String): KrakenResponse
}

class KrakenRepository(private val apiService: KrakenApiService) {
    suspend fun getKrakenData(pair: String): KrakenResponse {
        return apiService.getKrakenPrice(pair)
    }
}

class KrakenAPIFetchingUseCase(private val repository: KrakenRepository) {
    suspend fun kraken_api_execute(pair: String): KrakenResponse {
        return repository.getKrakenData(pair)
    }
}

data class KrakenResponse(val error: List<String>, val result: KrakenResult)
data class KrakenResult(val XXBTZUSD: KrakenBitcoin)
data class KrakenBitcoin(val c: List<String>)

// OPEN-ER API ------------------------------------------------------------------

interface OpenErApiService {
    @GET("/v6/latest/USD")
    suspend fun getForexRates(): ForexResponse
}

class ForexRepository(private val apiService: OpenErApiService) {
    suspend fun getForexData(): ForexResponse {
        return apiService.getForexRates()
    }
}

class ForexDataFetchingUseCase(private val repository: ForexRepository) {
    suspend fun open_er_api_execute(): ForexResponse {
        return repository.getForexData()
    }
}

data class ForexResponse(val rates: Rates)
data class Rates(val CAD: Double?, val EUR: Double?, val JPY: Double?)


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val viewModel = remember { GridViewModel(6, 10,
        KrakenAPIFetchingUseCase(Retrofit.Builder().createKrakenRepository()),
        ForexDataFetchingUseCase(Retrofit.Builder().createForexRepository())
    ) }
    GridView(viewModel = viewModel)
}