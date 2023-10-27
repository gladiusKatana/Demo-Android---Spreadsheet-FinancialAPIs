package com.example.stack

import android.os.Bundle
import android.util.Log

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Red) {
                    //SimpleCounter()
                    val viewModel = remember { GridViewModel(6, 10) }
                    //SingleNodeView(viewModel = viewModel)
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

class GridViewModel(val cols: Int, val rows: Int) : ViewModel() {
    private val _nodes = MutableStateFlow(List(cols * rows) { Node(it, 1.0) })
    val nodes: StateFlow<List<Node>> = _nodes

    init {
        _nodes.value[2].setFormula(listOf(_nodes.value[0], _nodes.value[1]), { values ->
            Log.d("FORMULA:", "Values for node 2: $values")
            values[0] + values[1]
        }, viewModelScope)

        _nodes.value[3].setFormula(listOf(_nodes.value[2]), { values ->
            Log.d("FORMULA:", "Values for node 3: $values")
            2 * values[0]
        }, viewModelScope)
    }

    fun incrementNodeValue(node: Node) {
        node.updateValue(node.value + 1)
        Log.d("VIEWMODEL_UPDATE", "Node value updated in ViewModel")
    }
}

@Composable
fun GridView(viewModel: GridViewModel, modifier: Modifier = Modifier) {
    val nodes by viewModel.nodes.collectAsState()

    var simpleState by remember { mutableStateOf(0) }

    Log.d("COMPOSABLE_RECOMPOSE", "GridView recomposed with node values: ${nodes.map { it.value }}")

    Column(
        modifier = modifier
            .clickable {
                simpleState++
                Log.d("INCREMENT_SIMPLE_STATE", "Incremented simpleState to: $simpleState")
            }
            .fillMaxSize()
            .background(Color.Blue)
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
                                Log.d("BoxClick", "Box clicked: value = ${node.value}")
                                if (!isDependent) {
                                    Log.d("GridViewModel", "Incrementing value from ${node.value} to ${node.value + 1}")
                                    viewModel.incrementNodeValue(node)
                                }
                            }
                            .graphicsLayer(clip = false),
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