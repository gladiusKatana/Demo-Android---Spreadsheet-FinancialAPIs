package com.example.stack
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.stack.Networking.ForexDataFetchingUseCase
import com.example.stack.Networking.KrakenAPIFetchingUseCase
import com.example.stack.Networking.createForexRepository
import com.example.stack.Networking.createKrakenRepository
import com.example.stack.V.GridView
import com.example.stack.VM.GridViewModel
import com.example.stack.ui.theme.StackTheme
import retrofit2.Retrofit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StackTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Red) { // Red color is temporary (Pre-Production)
                    val viewModel = remember {
                        GridViewModel(6, 10,
                            KrakenAPIFetchingUseCase(Retrofit.Builder().createKrakenRepository()),
                            ForexDataFetchingUseCase(Retrofit.Builder().createForexRepository())
                        )
                    }
                    val errorMessage by viewModel.errorMessage.collectAsState()
                    GridView(viewModel = viewModel)

                    errorMessage?.let { message ->
                        // Show a toast message for errors
                        Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val viewModel = remember { GridViewModel(6, 10,
        KrakenAPIFetchingUseCase(Retrofit.Builder().createKrakenRepository()),
        ForexDataFetchingUseCase(Retrofit.Builder().createForexRepository())
    ) }
    GridView(viewModel = viewModel)
}