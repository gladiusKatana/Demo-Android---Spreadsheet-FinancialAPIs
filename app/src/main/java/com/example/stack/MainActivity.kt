package com.example.stack // Make sure this matches the package in your AndroidManifest.xml

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateListOf

// Definition of Node
class Node(var order: Int, var value: Double = 0.0, var dependency: Dependency? = null)

// Definition of Dependency
class Dependency(var nodes: List<Node>, var computation: (List<Double>) -> Double)

// ViewModel to manage business logic
class GridViewModel(val cols: Int, val rows: Int) : ViewModel() {
    val nodes = mutableStateListOf(*List(cols * rows) { i -> Node(i, kotlin.random.Random.nextDouble(1.0, 10.0)) }.toTypedArray())

    fun updateNode(node: Node, value: Double) {
        viewModelScope.launch {
            node.value = value
            node.dependency?.let { dependency ->
                val values = dependency.nodes.map { it.value }
                node.value = dependency.computation(values)
            }
        }
    }
}

// Main Activity
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel = GridViewModel(6, 24)
            GridView(viewModel)
        }
    }
}

// UI Component displaying the Grid
@Composable
fun GridView(viewModel: GridViewModel) {
    val nodes = viewModel.nodes

    // Your UI components go here, e.g., Columns and Rows to organize your nodes
    // Use viewModel.updateNode(node, value) to trigger node updates
}

// Preview Function for visualization in Android Studio
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val viewModel = GridViewModel(6, 24)
    GridView(viewModel)
}
