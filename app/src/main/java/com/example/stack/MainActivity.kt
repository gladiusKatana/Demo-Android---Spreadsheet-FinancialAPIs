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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Red) {
                    val retrofit = Retrofit.Builder()
                        .baseUrl("https://your_api_base_url.com/") // Replace with your API's base URL
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()

                    val service = retrofit.create(FinancialApiService::class.java)
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
    private val _financialData = MutableStateFlow<FinancialDataResponse?>(null)
    val financialData: StateFlow<FinancialDataResponse?> = _financialData

    init {
        fetchFinancialData() // Call this if you want to fetch data when the ViewModel is initialized
    }

    // New function to fetch financial data
    private fun fetchFinancialData() {
        viewModelScope.launch {
            val data = repository.getFinancialData()
            _financialData.value = data

            // Populate the nodes (cells) with the fetched data
            _nodes.value[0].updateValue(data.someValue1) // Assuming FinancialDataResponse has a property called someValue1
            _nodes.value[5].updateValue(data.someValue2) // Assuming FinancialDataResponse has a property called someValue2

            // Update the formulas to match the iOS app
            updateFormulas()
        }
    }

    private fun updateFormulas() {
        // Update your node dependency relationships here to match the iOS app logic
        // Example:
        _nodes.value[2].setFormula(listOf(_nodes.value[0], _nodes.value[1]), { values ->
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
                    val node = nodes[row * viewModel.cols + col]
                    NodeView(node = node, onClick = { viewModel.incrementNodeValue(node) },
                        modifier = Modifier.weight(1f, fill = true)) // ensures each Box takes equal width inside the Row
                }
            }
        }
    }
}

@Composable
fun NodeView(node: Node, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val roundedValue = String.format("%.2f", node.value)
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

interface FinancialApiService {
    @GET("path_to_endpoint")
    suspend fun fetchFinancialData(): FinancialDataResponse
}

class FinancialRepository(private val apiService: FinancialApiService) {
    suspend fun getFinancialData(): FinancialDataResponse {
        return apiService.fetchFinancialData()
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://your_api_base_url.com/") // Replace with your API's base URL
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service = retrofit.create(FinancialApiService::class.java)
    val repository = FinancialRepository(apiService = service)
    val viewModel = remember { GridViewModel(6, 10, repository = repository) }
    GridView(viewModel = viewModel)
}