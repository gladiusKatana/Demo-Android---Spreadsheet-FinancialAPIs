package com.example.stack.V

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.stack.VM.GridViewModel

@Composable
fun GridView(viewModel: GridViewModel, modifier: Modifier = Modifier) {
    val nodes by viewModel.nodes.collectAsState()
    val colors = MaterialTheme.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.surface)
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