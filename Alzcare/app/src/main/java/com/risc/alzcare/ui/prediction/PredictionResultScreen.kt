package com.risc.alzcare.ui.prediction

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.risc.alzcare.network.model.PostResponse
import com.risc.alzcare.ui.theme.CirclesBackground
import com.risc.alzcare.ui.theme.myLayerConfigs
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionResultScreen(
    response: PostResponse,
    onNavigateBack: () -> Unit,
    finalQuestionnaireOffset: Float
) {
    var isScreenActive by remember { mutableStateOf(false) }

    val screenHeightPx = with(LocalDensity.current) { LocalConfiguration.current.screenHeightDp.dp.toPx() }
    val additionalYShiftAmount = screenHeightPx * 0.01f

    val animatedAdditionalYShift by animateFloatAsState(
        targetValue = if (isScreenActive) additionalYShiftAmount else 0f,
        animationSpec = tween(durationMillis = 450),
        label = "AdditionalYShiftAnimation"
    )

    LaunchedEffect(Unit) {
        isScreenActive = true
    }

    CirclesBackground(
        modifier = Modifier.fillMaxSize(),
        layerConfigs = myLayerConfigs,
        questionBasedOffset = finalQuestionnaireOffset,
        entryExitVerticalOffset = animatedAdditionalYShift
    ) {
        AnimatedVisibility(
            visible = isScreenActive,
            modifier = Modifier.fillMaxSize(),
            enter = slideInVertically(
                initialOffsetY = { it / 8 },
                animationSpec = tween(durationMillis = 400, delayMillis = 150)
            ),
            exit = slideOutVertically(
                targetOffsetY = { it / 8 },
                animationSpec = tween(durationMillis = 350)
            )
        ) {
            Scaffold(
                containerColor = Color.Transparent,
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Assessment Results",
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    response.patientId?.let {
                        ResultDetailItem(label = "Patient ID:", value = it.toString())
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    response.riskScore?.let {
                        ResultDetailItem(label = "Risk Score:", value = String.format(Locale.US, "%.2f", it))
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    response.reliabilityScore?.let {
                        ResultDetailItem(label = "Reliability Score:", value = String.format(Locale.US, "%.1f%%", it * 100))
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    response.nextSteps?.let { nextStepsValue ->
                        if (nextStepsValue.isNotBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "Next Steps", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                            Text(text = nextStepsValue, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = {
                            isScreenActive = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp),
                        shape = RectangleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                       ) {
                        Text(
                            text = "Back",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            }
        }
    }

    var previousScreenActiveState by remember { mutableStateOf(isScreenActive) }
    LaunchedEffect(isScreenActive) {
        if (!isScreenActive && previousScreenActiveState) {
            delay(470)
            onNavigateBack()
        }
        previousScreenActiveState = isScreenActive
    }
}

@Composable
fun ResultDetailItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.4f))
        Text(text = value, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(0.6f))
    }
}

