package com.example.stack.VM
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stack.M.Node
import com.example.stack.Networking.ForexDataFetchingUseCase
import com.example.stack.Networking.ForexResponse
import com.example.stack.Networking.KrakenAPIFetchingUseCase
import com.example.stack.Networking.KrakenResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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