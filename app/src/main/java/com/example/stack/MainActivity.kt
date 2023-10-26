package com.example.stack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Red) {
                    val viewModel = remember { GridViewModel(6, 10) }
                    GridView(viewModel = viewModel)
                }
            }
        }
    }
}

//class Node(order: Int, initialValue: Double) {
//    var order = order
//    private var _valueFlow = MutableStateFlow(initialValue)
//    val valueFlow: StateFlow<Double> = _valueFlow // External immutable access to the flow.
//    var value: Double // Whenever the value changes, it's reflected in the flow.
//        get() = _valueFlow.value
//        set(newValue) {
//            _valueFlow.value = newValue
//        }
//    var dependency: Dependency? = null
//
////    // Nested class to define Dependency
////    data class Dependency(val nodes: List<Node>, val computation: (List<Double>) -> Double)
//}

// Node class with reactive properties
class Node(var order: Int, initialValue: Double = 0.0, var dependency: Dependency? = null) {
    private val _valueFlow = MutableSharedFlow<Double>() // Hidden Flow to handle internal updates
    val valueFlow: SharedFlow<Double> = _valueFlow // Exposed Flow for consumers to observe

    var value by mutableStateOf(initialValue)
        private set(value) { // Limit external modification
            field = value
            _valueFlow.tryEmit(value) // Emit value when it's changed
        }

    // Explanation: valueFlow and _valueFlow are utilized to build a reactive paradigm around our Node.
    // While _valueFlow (MutableSharedFlow) is for internal modifications, valueFlow (SharedFlow)
    // is exposed for other components to observe without being able to modify it directly.
}

class Dependency(val nodes: List<Node>, val computation: (List<Double>) -> Double)

class GridViewModel(val cols: Int, val rows: Int) : ViewModel() {
    private val _nodes = MutableStateFlow(List(cols * rows) { Node(it, 1.0) })
    val nodes: StateFlow<List<Node>> = _nodes

    init {
        setFormula(_nodes.value[3], listOf(_nodes.value[0], _nodes.value[1])) { values ->
            values[0] + values[1]
        }
    }

    fun updateDependentNodes(independentNode: Node) {
        for (n in _nodes.value) {
            n.dependency?.let { dependency ->
                if (dependency.nodes.contains(independentNode)) {
                    val values = dependency.nodes.map { it.value }
                    n.value = dependency.computation(values)
                }
            }
        }
        _nodes.value = _nodes.value.toList()
    }

    fun incrementNodeValue(node: Node) {
        node.value += 1
        updateDependentNodes(independentNode = node)
    }

    private fun setFormula(node: Node, nodes: List<Node>, computation: (List<Double>) -> Double) {
        node.dependency = Dependency(nodes, computation)
        node.value = computation(nodes.map { it.value })
    }
}

@Composable
fun GridView(viewModel: GridViewModel, modifier: Modifier = Modifier) {
    val nodes by viewModel.nodes.collectAsState()

    Column(
        modifier = modifier.fillMaxSize().background(Color.Blue)
    ) {
        for (row in 0 until viewModel.rows) {
            Row(
                modifier = Modifier.weight(1f, fill = true)
            ) {
                for (col in 0 until viewModel.cols) {
                    val node = nodes[row * viewModel.cols + col]
                    val roundedValue = String.format("%.2f", node.value)
                    val isDependent = node.dependency != null

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .background(Color(0xFFE0E0E0))
                            .clickable(enabled = !isDependent) {
                                if (!isDependent) {
                                    viewModel.incrementNodeValue(node)
                                }
                            },
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
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val viewModel = remember { GridViewModel(6, 10) }
    GridView(viewModel = viewModel)
}