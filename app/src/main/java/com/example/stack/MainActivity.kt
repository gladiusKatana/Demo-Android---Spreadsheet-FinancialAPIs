package com.example.stack

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
                val configuration = LocalConfiguration.current
                val screenWidth = configuration.screenWidthDp.dp
                val screenHeight = configuration.screenHeightDp.dp

                val horizontalPadding = screenWidth * 0.15f
                val verticalPadding = screenHeight * 0.2f

                Surface(modifier = Modifier.fillMaxSize()/*, color = Color.Red*/) {
                    val viewModel = remember {
                        GridViewModel(
                            4, 12,
                            KrakenAPIFetchingUseCase(Retrofit.Builder().createKrakenRepository()),
                            ForexDataFetchingUseCase(Retrofit.Builder().createForexRepository())
                        )
                    }
                    val errorMessage by viewModel.errorMessage.collectAsState()

                    GridView(
                        viewModel = viewModel, modifier = Modifier
                            .padding(
                                start = horizontalPadding,
                                end = horizontalPadding,
                                top = verticalPadding,
                                //bottom = verticalPadding
                            )
                    )

                    errorMessage?.let { message ->
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
    StackTheme {
        val viewModel = remember {
            GridViewModel(
                5, 10,
                KrakenAPIFetchingUseCase(Retrofit.Builder().createKrakenRepository()),
                ForexDataFetchingUseCase(Retrofit.Builder().createForexRepository())
            )
        }
        GridView(viewModel = viewModel)
    }
}
