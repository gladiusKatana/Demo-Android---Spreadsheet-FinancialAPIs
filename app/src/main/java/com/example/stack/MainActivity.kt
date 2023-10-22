//MainActivity.kt (all kotlin code for the app itself is contained in this file - for now):
package com.example.stack
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
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

class Node(var order: Int, value: Double = 0.0, var dependency: Dependency? = null) {
    var value by mutableStateOf(value)
}

class Dependency(var nodes: List<Node>, var computation: (List<Double>) -> Double)

class GridViewModel(val cols: Int, val rows: Int) : ViewModel() {
    val nodes = mutableStateListOf(*List(cols * rows) { Node(it, 1.0) }.toTypedArray())

    init {
        setFormula(nodes[3], listOf(nodes[0], nodes[1])) { values ->
            values[0] + values[1]
        }
    }

    fun updateDependentNodes(independentNode: Node) {
        for (n in nodes) {
            n.dependency?.let { dependency ->
                if (dependency.nodes.contains(independentNode)) {
                    val values = dependency.nodes.map { it.value }
                    n.value = dependency.computation(values)
                }
            }
        }
    }

    fun incrementNodeValue(node: Node) {
        node.value += 1 // Due to Kotlin's delegate syntax, this will update the MutableState
        updateDependentNodes(independentNode = node)
    }

    private fun setFormula(node: Node, nodes: List<Node>, computation: (List<Double>) -> Double) {
        node.dependency = Dependency(nodes, computation)
        node.value = computation(nodes.map { it.value })
    }
}

@Composable
fun GridView(viewModel: GridViewModel, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().background(Color.Blue)
    ) {
        for (row in 0 until viewModel.rows) {
            Row(
                modifier = Modifier.weight(1f, fill = true) // gives each cell an equal auto-fitted height and fills the entire height
            ) {
                for (col in 0 until viewModel.cols) {
                    val node = viewModel.nodes[row * viewModel.cols + col]
                    val roundedValue = String.format("%.2f", node.value)
                    val isDependent = node.dependency != null

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .background(Color(0xFFE0E0E0)) // Neutral background color for cell
                            .clickable(enabled = !isDependent) {
                                if (!isDependent) {
                                    viewModel.incrementNodeValue(node)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = roundedValue,
                            modifier = Modifier.padding(8.dp), // Adjust padding as necessary
                            style = MaterialTheme.typography.body1, // Typography style for better readability
                            color = Color.Black // Contrasting text color
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
