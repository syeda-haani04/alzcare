package com.risc.alzcare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.risc.alzcare.network.model.PostResponse
import com.risc.alzcare.ui.prediction.PredictionResultScreen
import com.risc.alzcare.ui.questionnaire.QuestionnaireScreen
import com.risc.alzcare.ui.questionnaire.QuestionnaireViewModel
import com.risc.alzcare.ui.theme.AlzcareTheme
import kotlinx.serialization.json.Json
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object AppDestinations {
    const val QUESTIONNAIRE_ROUTE = "questionnaire"
    private const val PREDICTION_RESULT_ROUTE_BASE = "prediction_result"

    const val PREDICTION_RESPONSE_ARG_KEY = "postResponseJson"
    const val PREDICTION_OFFSET_ARG_KEY = "offset"

    const val PREDICTION_RESULT_ROUTE_TEMPLATE =
        "$PREDICTION_RESULT_ROUTE_BASE/{$PREDICTION_RESPONSE_ARG_KEY}/{$PREDICTION_OFFSET_ARG_KEY}"

    fun buildPredictionResultRoute(postResponse: PostResponse, offset: Float): String {
        val jsonString = Json.encodeToString(PostResponse.serializer(), postResponse)
        val encodedJson = URLEncoder.encode(jsonString, StandardCharsets.UTF_8.toString())
        val encodedOffset = URLEncoder.encode(offset.toString(), StandardCharsets.UTF_8.toString())
        return "$PREDICTION_RESULT_ROUTE_BASE/$encodedJson/$encodedOffset"
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            AlzcareTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = AppDestinations.QUESTIONNAIRE_ROUTE
                    ) {
                        composable(AppDestinations.QUESTIONNAIRE_ROUTE) {
                            val questionnaireViewModel: QuestionnaireViewModel = viewModel()
                            QuestionnaireScreen(
                                viewModel = questionnaireViewModel,
                                onNavigateToPredictionResult = { responseData, offset ->
                                    navController.navigate(
                                        AppDestinations.buildPredictionResultRoute(responseData, offset)
                                    )
                                }
                            )
                        }

                        composable(
                            route = AppDestinations.PREDICTION_RESULT_ROUTE_TEMPLATE,
                            arguments = listOf(
                                navArgument(AppDestinations.PREDICTION_RESPONSE_ARG_KEY) { type = NavType.StringType },
                                navArgument(AppDestinations.PREDICTION_OFFSET_ARG_KEY) { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val postResponseJson = backStackEntry.arguments?.getString(AppDestinations.PREDICTION_RESPONSE_ARG_KEY)
                            val offsetString = backStackEntry.arguments?.getString(AppDestinations.PREDICTION_OFFSET_ARG_KEY)

                            var responseState by remember { mutableStateOf<PostResponse?>(null) }
                            var finalOffsetState by remember { mutableFloatStateOf(0f) }
                            var errorState by remember { mutableStateOf<String?>(null) }
                            var dataProcessed by remember { mutableStateOf(false) }

                            LaunchedEffect(key1 = postResponseJson, key2 = offsetString) {
                                if (!dataProcessed) {
                                    if (postResponseJson != null && offsetString != null) {
                                        try {
                                            val decodedJsonString = URLDecoder.decode(postResponseJson, StandardCharsets.UTF_8.toString())
                                            responseState = Json.decodeFromString(PostResponse.serializer(), decodedJsonString)

                                            val decodedOffsetString = URLDecoder.decode(offsetString, StandardCharsets.UTF_8.toString())
                                            finalOffsetState = decodedOffsetString.toFloatOrNull() ?: 0f
                                            errorState = null
                                        } catch (e: Exception) {
                                            errorState = "Error: Could not parse prediction data. ${e.localizedMessage}"
                                        }
                                    } else {
                                        errorState = "Error: Prediction data or offset not found in navigation."
                                    }
                                    dataProcessed = true
                                }
                            }

                            if (errorState != null) {
                                ErrorDisplay(message = errorState!!)
                            } else if (responseState != null) {
                                PredictionResultScreen(
                                    response = responseState!!,
                                    onNavigateBack = {
                                        navController.popBackStack()
                                    },
                                    finalQuestionnaireOffset = finalOffsetState
                                )
                            } else if (dataProcessed) {
                                ErrorDisplay(message = "Error: Data could not be loaded for the result screen.")
                            } else {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Loading results...")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorDisplay(message: String) {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
    }
}
