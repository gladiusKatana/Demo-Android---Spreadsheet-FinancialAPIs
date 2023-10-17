import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Node(
    val order: Int,
    var value: Double,
    var dependency: Dependency? = null
) {
    var valueChanged: (Double) -> Unit = {}

    init {
        dependency?.nodes?.forEach { node ->
            node.valueChanged = { updateValue() }
        }
        updateValue()
    }

    private fun updateValue() {
        dependency?.let {
            value = it.computation(it.nodes.map { node -> node.value })
            valueChanged(value)
        }
    }
}

data class Dependency(
    val nodes: List<Node>,
    val computation: (List<Double>) -> Double
)

// NetworkManager, APIFetchingGridViewModel, DashboardViewModel, and other networking classes will be similar to Swift's implementation,
// just translated into Kotlin and adapted for Android networking libraries such as Retrofit or OkHttp.

@Composable
fun GridView(viewModel: GridViewModel) {
    val nodes by viewModel.nodes.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(16.dp)
    ) {
        nodes.forEach { node ->
            Text(text = node.value.toString())
        }
    }
}

@Composable
@Preview
fun PreviewGridView() {
    // Provide a mocked ViewModel or a PreviewViewModel to show a preview of the GridView.
    // GridView(PreviewViewModel())
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Column {
                // Your Composables here, e.g., GridView
            }
        }
    }
}

// Remember to handle Android lifecycle events, network permissions, and error handling, which might be different or additional compared to iOS/Swift.
