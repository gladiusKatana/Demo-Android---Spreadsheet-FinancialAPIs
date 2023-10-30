package com.example.stack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Red) {
                    val retrofit = Retrofit.Builder()
                        .baseUrl("https://api.kraken.com")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                    val service = retrofit.create(KrakenApiService::class.java)
                    val repository = FinancialRepository(apiService = service)
                    val viewModel = remember { GridViewModel(6, 10, repository = repository) }
                    GridView(viewModel = viewModel)
                }
            }
        }
    }
}

// Node class with reactive properties
data class Node(val order: Int, val initialValue: Double = 0.0) {
    var value by mutableStateOf(initialValue)
    private val _valueFlow = MutableStateFlow<Double>(initialValue)
    val valueFlow: StateFlow<Double> = _valueFlow
    var dependency: Dependency? = null

    fun updateValue(newValue: Double) {
        value = newValue
        _valueFlow.value = newValue
    }

    fun setFormula(inputNodes: List<Node>, computation: (List<Double>) -> Double, scope: CoroutineScope) {
        this.dependency = Dependency(inputNodes, computation)

        combine(*inputNodes.map { it.valueFlow }.toTypedArray()) { values ->
            computation.invoke(values.toList()) // apply the formula
        }.onEach { newValue: Double ->
            updateValue(newValue) // update the value of the node itself
        }.launchIn(scope)
    }
}

class Dependency(val nodes: List<Node>, val computation: (List<Double>) -> Double)

class GridViewModel(val cols: Int, val rows: Int, private val repository: FinancialRepository) : ViewModel() {
    private val _nodes = MutableStateFlow(List(cols * rows) { Node(it, 1.0) })
    val nodes: StateFlow<List<Node>> = _nodes

    // New property to store fetched financial data
    private val _financialData = MutableStateFlow<TickerResponse?>(null)
    val financialData: StateFlow<TickerResponse?> = _financialData

    init {
        fetchFinancialData() // fetch data when the ViewModel is initialized
        startFetchingBitcoinPrice(interval_milliseconds = 5000)
    }

    private fun startFetchingBitcoinPrice(interval_milliseconds: Long) {
        viewModelScope.launch {
            while (true) {
                delay(interval_milliseconds)
                fetchFinancialData()
            }
        }
    }

    // New function to fetch financial data
    private fun fetchFinancialData() {
        viewModelScope.launch {
            val data = repository.getFinancialData("XBTUSD")
            _financialData.value = data

            // Extract value from API response & update node value(s)
            data.result.XXBTZUSD.c.firstOrNull()?.toDoubleOrNull()?.let { price ->
                _nodes.value[0].updateValue(price)
            }

            updateFormulas()
        }
    }

    private fun updateFormulas() {
        _nodes.value[1].setFormula(listOf(_nodes.value[0]), { values ->
            values[0] / 0.72
        }, viewModelScope)

        _nodes.value[30].setFormula(listOf(_nodes.value[10], _nodes.value[20]), { values ->
            values[0] + values[1]
        }, viewModelScope)
    }

    fun incrementNodeValue(node: Node) {
        node.updateValue(node.value + 1)
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
                    NodeView(node = node, onClick = { viewModel.incrementNodeValue(node) },
                        modifier = Modifier.weight(1f, fill = true)) // ensures each Box takes equal width inside the Row
                }
            }
        }
    }
}

@Composable
fun NodeView(node: Node, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val roundedValue = if (node.value == 1.0) "." else String.format("%.2f", node.value)
    val isDependent = node.dependency != null

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE0E0E0))
            .clickable(enabled = !isDependent, onClick = onClick),
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

interface KrakenApiService {
    @GET("0/public/Ticker")
    suspend fun getTickerInfo(@Query("pair") pair: String): TickerResponse
}

class FinancialRepository(private val apiService: KrakenApiService) {
    suspend fun getFinancialData(pair: String): TickerResponse {
        return apiService.getTickerInfo(pair)
    }
}

data class TickerResponse(val error: List<String>, val result: KrakenResult)
data class KrakenResult(val XXBTZUSD: KrakenBitcoin)
data class KrakenBitcoin(val c: List<String>)


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.kraken.com")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val service = retrofit.create(KrakenApiService::class.java)
    val repository = FinancialRepository(apiService = service)
    val viewModel = remember { GridViewModel(6, 10, repository = repository) }
    GridView(viewModel = viewModel)
}