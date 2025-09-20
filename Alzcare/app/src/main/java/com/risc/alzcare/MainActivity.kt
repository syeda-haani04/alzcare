package com.risc.alzcare

// core dependencies
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.risc.alzcare.ui.theme.AlzcareTheme
import retrofit2.Response

class MainActivity : ComponentActivity() {
    private val apiService: ApiInterface by lazy {
        RetrofitInstance.api
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlzcareTheme {
                var userInputsState by rememberSaveable {
                    mutableStateOf(UserInputs(hello = "Loading Hello...", hi = "Loading Hi...", oye = "Loading Oye..."))
                }


                LaunchedEffect(Unit) {

                    val response: Response<ResponseData> = apiService.getData()

                    if (response.isSuccessful) {
                        response.body()?.let { responseData ->
                            userInputsState = userInputsState.copy(
                                hello = responseData.hello,
                                hi = responseData.hi,
                                oye = responseData.oye
                            )
                        }
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier.padding(innerPadding)
                    ){
                        Labels(userInputsState.hello)
                        Labels(userInputsState.hi)
                        Labels(userInputsState.oye)
                    }

                }
            }
        }
    }
}

@Composable
fun Labels(text: String){
    Text(
        text = text,
        modifier = Modifier.padding(16.dp)
    )
}