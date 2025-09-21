package com.risc.alzcare.ui.patientdata

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.risc.alzcare.ui.patientdata.state.AppInputType
import com.risc.alzcare.ui.patientdata.state.InputFieldModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDataScreen(
    viewModel: PatientDataViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val serverMessage by viewModel.serverMessage.collectAsState()
    val predictionResult by viewModel.predictionResult.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(serverMessage) {
        serverMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
            }
            viewModel.clearServerMessage()
        }
    }

    LaunchedEffect(uiState.submissionError) {
        uiState.submissionError?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = error,
                    duration = SnackbarDuration.Long,
                    actionLabel = "Dismiss"
                )
            }
            viewModel.clearSubmissionError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("Patient Data Entry") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.isLoading && uiState.submissionError == null && serverMessage == null) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }

            predictionResult?.let { result ->
                Text(
                    text = "Prediction Result:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = result,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(uiState.inputFields) { field ->
                    PatientDataInputField(
                        fieldModel = field,
                        onValueChange = { newValue ->
                            viewModel.updateFieldValue(field.id, newValue)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.submitPatientData() },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (uiState.isLoading) "Submitting..." else "Submit Data")
            }
        }
    }
}

@Composable
fun PatientDataInputField(
    fieldModel: InputFieldModel,
    onValueChange: (String) -> Unit
) {
    Column {
        OutlinedTextField(
            value = fieldModel.value,
            onValueChange = onValueChange,
            label = { Text(fieldModel.label) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = when (fieldModel.appInputType) {
                AppInputType.NUMBER_INTEGER -> KeyboardOptions(keyboardType = KeyboardType.Number)
                AppInputType.NUMBER_DECIMAL -> KeyboardOptions(keyboardType = KeyboardType.Decimal)
                else -> KeyboardOptions.Default
            },
            isError = fieldModel.errorMessage != null,
            singleLine = true
        )
        fieldModel.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}