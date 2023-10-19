package com.example.stack

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize()) {
                    val viewModel = remember { GridViewModel(6, 24) }
                    GridView(viewModel)
                }
            }
        }
    }
}

class Node(var order: Int, value: Double = 0.0, var dependency: Dependency? = null) {
    var value by mutableStateOf(value)  // Convert value to a MutableState
}
class Dependency(var nodes: List<Node>, var computation: (List<Double>) -> Double)

class GridViewModel(val cols: Int, val rows: Int) : ViewModel() {
    val nodes = mutableStateListOf(*List(cols * rows) { i -> Node(i, kotlin.random.Random.nextDouble(1.0, 10.0)) }.toTypedArray())

    init {
        for (i in nodes.indices) {
            nodes[i].value = 1.0 // Set default initial values
        }

        setFormula(nodes[3], listOf(nodes[0], nodes[1])) { values ->
            values[0] + values[1]
        }
    }

    fun updateNode(node: Node, value: Double) {
        viewModelScope.launch {
            node.value = value
            node.dependency?.let { dependency ->
                val values = dependency.nodes.map { it.value }
                node.value = dependency.computation(values)
            }
            // Iterate through all nodes to update dependent values
            for (n in nodes) {
                n.dependency?.let { dependency ->
                    if (dependency.nodes.contains(node)) {
                        val values = dependency.nodes.map { it.value }
                        n.value = dependency.computation(values)
                    }
                }
            }
        }
    }

    // increment node value by 1
    fun incrementNodeValue(node: Node) {
        viewModelScope.launch {
            node.value += 1 // Due to Kotlin's delegate syntax, this will update the MutableState
        }
    }

    private fun setFormula(node: Node, nodes: List<Node>, computation: (List<Double>) -> Double) {
        node.dependency = Dependency(nodes, computation)
        node.value = computation(nodes.map { it.value })
    }
}


@Composable
fun GridView(viewModel: GridViewModel) {
    Column {
        for (row in 0 until viewModel.rows) {
            Row {
                for (col in 0 until viewModel.cols) {
                    val node = viewModel.nodes[row * viewModel.cols + col]

                    // Convert the node's value to a string with a maximum precision of 3 decimal places
                    val roundedValue = String.format("%.3f", node.value)

                    Text(
                        text = roundedValue,
                        modifier = Modifier
                            .background(Color.Gray)
                            .clickable {
                                viewModel.incrementNodeValue(node)
                                Log.d("GridView", "Cell clicked, value = ${node.value}")
                            }
                            .padding(8.dp) // This is just to give some space. Adjust as necessary.
                    )
                }
            }
        }
    }
}




@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val viewModel = remember { GridViewModel(3, 10) }
    GridView(viewModel)
}
