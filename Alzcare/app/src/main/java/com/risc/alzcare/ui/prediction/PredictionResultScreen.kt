package com.risc.alzcare.ui.prediction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.risc.alzcare.network.model.PostResponse
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionResultScreen(
    response: PostResponse,
    onNavigateBack: () -> Unit
) {
    Scaffold(

    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Assessment Results",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            response.patientId?.let {
                ResultDetailItem(label = "Patient ID:", value = it.toString())
                Spacer(modifier = Modifier.height(8.dp))
            }
            response.riskScore?.let {
                ResultDetailItem(
                    label = "Risk Score:",
                    value = String.format(Locale.US, "%.2f", it)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            response.reliabilityScore?.let {
                ResultDetailItem(
                    label = "Reliability Score:",
                    value = String.format(Locale.US, "%.1f%%", it * 100)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            response.nextSteps?.let { nextStepsValue ->
                if (nextStepsValue.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Next Steps",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    Text(
                        text = nextStepsValue,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (response.nextSteps.isNullOrBlank()) {
                Spacer(modifier = Modifier.weight(1f))
            } else {
                Spacer(modifier = Modifier.height(24.dp))
            }

            Button(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ResultDetailItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(0.6f)
        )
    }
}