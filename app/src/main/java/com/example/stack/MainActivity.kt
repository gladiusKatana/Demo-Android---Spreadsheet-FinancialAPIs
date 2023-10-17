import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

class Node(var order: Int, var value: Double = 0.0, var dependency: Dependency? = null)

class Dependency(var nodes: List<Node>, var computation: (List<Double>) -> Double)

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

@Composable
fun GridView(viewModel: GridViewModel) {
    Column {
        for (row in 0 until viewModel.rows) {
            Row {
                for (col in 0 until viewModel.cols) {
                    val node = viewModel.nodes[row * viewModel.cols + col]
                    val textState = remember { mutableStateOf(TextFieldValue(node.value.toString())) }

                    BasicTextField(
                        value = textState.value,
                        onValueChange = {
                            textState.value = it
                            // Maybe here you want to parse the text as Double and update the node value
                            // For example:
                            viewModel.updateNode(node, it.text.toDoubleOrNull() ?: 0.0)
                        },
                        modifier = Modifier.background(Color.Gray) // Add your modifiers here
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val viewModel = remember { GridViewModel(6, 24) }
    GridView(viewModel)
}
