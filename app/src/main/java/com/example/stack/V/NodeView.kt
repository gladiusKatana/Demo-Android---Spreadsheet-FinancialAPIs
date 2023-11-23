package com.example.stack.V
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.stack.M.Node

@Composable
fun NodeView(node: Node, modifier: Modifier = Modifier) {
    val typography = MaterialTheme.typography
    val colors = MaterialTheme.colors

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.surface),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (node.value == 1.0) "." else String.format("%.2f", node.value),
            modifier = Modifier.padding(8.dp),
            style = typography.body1,
            color = colors.onSurface
        )
    }
}