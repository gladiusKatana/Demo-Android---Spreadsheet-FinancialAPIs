package com.example.stack

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import com.example.stack.M.Node

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

class Dependency(val nodes: List<Node>, val computation: (List<Double>) -> Double)

class GridViewModel(val cols: Int, val rows: Int,
                    private val krakenAPIFetchingUseCase: KrakenAPIFetchingUseCase,
                    private val forexDataFetchingUseCase: ForexDataFetchingUseCase) : ViewModel() {
    private val _nodes = MutableStateFlow(List(cols * rows) { Node(it, 1.0) })
    val nodes: StateFlow<List<Node>> = _nodes

    private val _bitcoinPriceData = MutableStateFlow<KrakenResponse?>(null)
    private val _forexData = MutableStateFlow<ForexResponse?>(null)

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        viewModelScope.launch {
            fetchInitialData()
            initUpdate(interval_milliseconds = 5000) { fetchBitcoinPrice() }
            initUpdate(interval_milliseconds = 30000) { fetchForexRate() }
        }
    }

    private fun initUpdate(interval_milliseconds: Long, fetchData: suspend () -> Unit) {
        viewModelScope.launch {
            while (true) {
                delay(interval_milliseconds)
                fetchData()
            }
        }.invokeOnCompletion { throwable ->
            // Handle coroutine cancellation or errors here if needed
            throwable?.let {
                _errorMessage.value = "Error: ${it.message}"
            }
        }
    }

    private suspend fun fetchInitialData() {
        fetchBitcoinPrice()
        fetchForexRate()
    }

    private suspend fun fetchBitcoinPrice() {
        try {
            val data = krakenAPIFetchingUseCase.kraken_api_execute("XBTUSD")
            _bitcoinPriceData.value = data
            data.result.XXBTZUSD.c.firstOrNull()?.toDoubleOrNull()?.let { price ->
                _nodes.value[0].updateValue(price)
                updateFormulas()
            }
        } catch (e: Exception) {
            // Handle the error by setting an error message
            _errorMessage.value = "Error fetching Bitcoin price: ${e.message}"
        }
    }

    private suspend fun fetchForexRate() {
        try {
            val data = forexDataFetchingUseCase.open_er_api_execute()
            _forexData.value = data
            data.rates.CAD?.let { rate ->
                _nodes.value[5].updateValue(rate)
                updateFormulas()
            }
        } catch (e: Exception) {
            // Handle the error by setting an error message
            _errorMessage.value = "Error fetching Forex rate: ${e.message}"
        }
    }

    private fun updateFormulas() {
        _nodes.value[1].setFormula(listOf(_nodes.value[0], _nodes.value[5]), { n ->
            n[0] * n[1]
        }, viewModelScope)
        _nodes.value[6].setFormula(listOf(_nodes.value[5]), { n ->
            1 / n[0]
        }, viewModelScope)
    }
}


@Composable
fun GridView(viewModel: GridViewModel, modifier: Modifier = Modifier) {
    val nodes by viewModel.nodes.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Blue)
    ) {
        for (row in 0 until viewModel.rows) {
            Row(
                modifier = Modifier.weight(1f, fill = true) // ensures each Row takes equal height
            ) {
                for (col in 0 until viewModel.cols) {
                    val node = nodes[col * viewModel.rows + row]
                    NodeView(node = node, modifier = Modifier.weight(1f, fill = true)) // ensures each Box takes equal width inside the Row
                }
            }
        }
    }
}

@Composable
fun NodeView(node: Node, modifier: Modifier = Modifier) {
    val roundedValue = if (node.value == 1.0) "." else String.format("%.2f", node.value)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE0E0E0)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = roundedValue,
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.body1,
            color = Color.Black
        )
    }
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