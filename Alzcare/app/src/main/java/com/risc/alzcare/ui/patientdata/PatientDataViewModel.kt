package com.risc.alzcare.ui.patientdata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.risc.alzcare.network.ApiInterface
import com.risc.alzcare.network.RetrofitInstance
import com.risc.alzcare.network.model.PatientDataPayload
import com.risc.alzcare.network.model.PostResponse
import com.risc.alzcare.ui.patientdata.state.AppInputType
import com.risc.alzcare.ui.patientdata.state.InputFieldModel
import com.risc.alzcare.ui.patientdata.state.PatientDataScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.util.Log

class PatientDataViewModel : ViewModel() {

    private val apiService: ApiInterface = RetrofitInstance.api

    private val _uiState = MutableStateFlow(PatientDataScreenState(inputFields = getInitialPatientFields()))
    val uiState: StateFlow<PatientDataScreenState> = _uiState.asStateFlow()

    private val _serverMessage = MutableStateFlow<String?>(null)
    val serverMessage: StateFlow<String?> = _serverMessage.asStateFlow()

    private val _predictionResult = MutableStateFlow<String?>(null)
    val predictionResult: StateFlow<String?> = _predictionResult.asStateFlow()

    fun clearServerMessage() {
        _serverMessage.value = null
        //_predictionResult.value = null
    }

    fun clearSubmissionError() {
        _uiState.update { it.copy(submissionError = null) }
    }

    private fun getInitialPatientFields(): List<InputFieldModel> {
        return listOf(
            InputFieldModel(id = "Age", label = "Age", appInputType = AppInputType.NUMBER_INTEGER),
            InputFieldModel(id = "Gender", label = "Gender (e.g., 0 or 1)", appInputType = AppInputType.NUMBER_INTEGER),
            InputFieldModel(id = "Ethnicity", label = "Ethnicity (Codified)", appInputType = AppInputType.NUMBER_INTEGER),
            InputFieldModel(id = "EducationLevel", label = "Education Level (Codified)", appInputType = AppInputType.NUMBER_INTEGER),
            InputFieldModel(id = "BMI", label = "BMI", appInputType = AppInputType.NUMBER_DECIMAL),
            InputFieldModel(id = "Smoking", label = "Smoking (0 or 1)", appInputType = AppInputType.NUMBER_INTEGER),
            InputFieldModel(id = "AlcoholConsumption", label = "Alcohol Consumption", appInputType = AppInputType.NUMBER_DECIMAL),
            InputFieldModel(id = "PhysicalActivity", label = "Physical Activity", appInputType = AppInputType.NUMBER_DECIMAL),
            InputFieldModel(id = "DietQuality", label = "Diet Quality", appInputType = AppInputType.NUMBER_DECIMAL),
            InputFieldModel(id = "SleepQuality", label = "Sleep Quality", appInputType = AppInputType.NUMBER_DECIMAL),
            InputFieldModel(id = "FamilyHistoryAlzheimers", label = "Family History of Alzheimer's (0 or 1)", appInputType = AppInputType.NUMBER_INTEGER),
            InputFieldModel(id = "CardiovascularDisease", label = "Cardiovascular Disease (0 or 1)", appInputType = AppInputType.NUMBER_INTEGER),
            InputFieldModel(id = "Diabetes", label = "Diabetes (0 or 1)", appInputType = AppInputType.NUMBER_INTEGER),
            InputFieldModel(id = "Depression", label = "Depression (0 or 1)", appInputType = AppInputType.NUMBER_INTEGER),
            InputFieldModel(id = "HeadInjury", label = "Head Injury (0 or 1)", appInputType = AppInputType.NUMBER_INTEGER),
            InputFieldModel(id = "Hypertension", label = "Hypertension (0 or 1)", appInputType = AppInputType.NUMBER_INTEGER),
            InputFieldModel(id = "SystolicBP", label = "Systolic BP", appInputType = AppInputType.NUMBER_INTEGER),
            InputFieldModel(id = "DiastolicBP", label = "Diastolic BP", appInputType = AppInputType.NUMBER_INTEGER),
            InputFieldModel(id = "CholesterolTotal", label = "Cholesterol Total", appInputType = AppInputType.NUMBER_DECIMAL),
            InputFieldModel(id = "CholesterolLDL", label = "Cholesterol LDL", appInputType = AppInputType.NUMBER_DECIMAL),
            InputFieldModel(id = "CholesterolHDL", label = "Cholesterol HDL", appInputType = AppInputType.NUMBER_DECIMAL),
            InputFieldModel(id = "CholesterolTriglycerides", label = "Cholesterol Triglycerides", appInputType = AppInputType.NUMBER_DECIMAL),
            InputFieldModel(id = "MMSE", label = "MMSE Score", appInputType = AppInputType.NUMBER_DECIMAL),
            InputFieldModel(id = "FunctionalAssessment", label = "Functional Assessment Score", appInputType = AppInputType.NUMBER_DECIMAL),
            InputFieldModel(id = "MemoryComplaints", label = "Memory Complaints (0 or 1)", appInputType = AppInputType.NUMBER_INTEGER),
            InputFieldModel(id = "BehavioralProblems", label = "Behavioral Problems (0 or 1)", appInputType = AppInputType.NUMBER_INTEGER),
            InputFieldModel(id = "ADL", label = "ADL Score", appInputType = AppInputType.NUMBER_DECIMAL),
            InputFieldModel(id = "Confusion", label = "Confusion (0 or 1)", appInputType = AppInputType.NUMBER_INTEGER),
            InputFieldModel(id = "Disorientation", label = "Disorientation (0 or 1)", appInputType = AppInputType.NUMBER_INTEGER),
            InputFieldModel(id = "PersonalityChanges", label = "Personality Changes (0 or 1)", appInputType = AppInputType.NUMBER_INTEGER),
            InputFieldModel(id = "DifficultyCompletingTasks", label = "Difficulty Completing Tasks (0 or 1)", appInputType = AppInputType.NUMBER_INTEGER),
            InputFieldModel(id = "Forgetfulness", label = "Forgetfulness (0 or 1)", appInputType = AppInputType.NUMBER_INTEGER)
        )
    }

    fun updateFieldValue(fieldId: String, newValue: String) {
        _uiState.update { currentState ->
            val updatedFields = currentState.inputFields.map { field ->
                if (field.id == fieldId) {
                    field.copy(value = newValue, errorMessage = null)
                } else {
                    field
                }
            }
            currentState.copy(inputFields = updatedFields)
        }
    }

    fun submitPatientData() {
        val currentState = _uiState.value
        if (currentState.isLoading) return

        _serverMessage.value = null
        _predictionResult.value = null
        _uiState.update { it.copy(isLoading = true, submissionError = null) }

        val payload = PatientDataPayload(
            Age = currentState.inputFields.find { it.id == "Age" }?.value?.toIntOrNull(),
            Gender = currentState.inputFields.find { it.id == "Gender" }?.value?.toIntOrNull(),
            Ethnicity = currentState.inputFields.find { it.id == "Ethnicity" }?.value?.toIntOrNull(),
            EducationLevel = currentState.inputFields.find { it.id == "EducationLevel" }?.value?.toIntOrNull(),
            BMI = currentState.inputFields.find { it.id == "BMI" }?.value?.toDoubleOrNull(),
            Smoking = currentState.inputFields.find { it.id == "Smoking" }?.value?.toIntOrNull(),
            AlcoholConsumption = currentState.inputFields.find { it.id == "AlcoholConsumption" }?.value?.toDoubleOrNull(),
            PhysicalActivity = currentState.inputFields.find { it.id == "PhysicalActivity" }?.value?.toDoubleOrNull(),
            DietQuality = currentState.inputFields.find { it.id == "DietQuality" }?.value?.toDoubleOrNull(),
            SleepQuality = currentState.inputFields.find { it.id == "SleepQuality" }?.value?.toDoubleOrNull(),
            FamilyHistoryAlzheimers = currentState.inputFields.find { it.id == "FamilyHistoryAlzheimers" }?.value?.toIntOrNull(),
            CardiovascularDisease = currentState.inputFields.find { it.id == "CardiovascularDisease" }?.value?.toIntOrNull(),
            Diabetes = currentState.inputFields.find { it.id == "Diabetes" }?.value?.toIntOrNull(),
            Depression = currentState.inputFields.find { it.id == "Depression" }?.value?.toIntOrNull(),
            HeadInjury = currentState.inputFields.find { it.id == "HeadInjury" }?.value?.toIntOrNull(),
            Hypertension = currentState.inputFields.find { it.id == "Hypertension" }?.value?.toIntOrNull(),
            SystolicBP = currentState.inputFields.find { it.id == "SystolicBP" }?.value?.toIntOrNull(),
            DiastolicBP = currentState.inputFields.find { it.id == "DiastolicBP" }?.value?.toIntOrNull(),
            CholesterolTotal = currentState.inputFields.find { it.id == "CholesterolTotal" }?.value?.toDoubleOrNull(),
            CholesterolLDL = currentState.inputFields.find { it.id == "CholesterolLDL" }?.value?.toDoubleOrNull(),
            CholesterolHDL = currentState.inputFields.find { it.id == "CholesterolHDL" }?.value?.toDoubleOrNull(),
            CholesterolTriglycerides = currentState.inputFields.find { it.id == "CholesterolTriglycerides" }?.value?.toDoubleOrNull(),
            MMSE = currentState.inputFields.find { it.id == "MMSE" }?.value?.toDoubleOrNull(),
            FunctionalAssessment = currentState.inputFields.find { it.id == "FunctionalAssessment" }?.value?.toDoubleOrNull(),
            MemoryComplaints = currentState.inputFields.find { it.id == "MemoryComplaints" }?.value?.toIntOrNull(),
            BehavioralProblems = currentState.inputFields.find { it.id == "BehavioralProblems" }?.value?.toIntOrNull(),
            ADL = currentState.inputFields.find { it.id == "ADL" }?.value?.toDoubleOrNull(),
            Confusion = currentState.inputFields.find { it.id == "Confusion" }?.value?.toIntOrNull(),
            Disorientation = currentState.inputFields.find { it.id == "Disorientation" }?.value?.toIntOrNull(),
            PersonalityChanges = currentState.inputFields.find { it.id == "PersonalityChanges" }?.value?.toIntOrNull(),
            DifficultyCompletingTasks = currentState.inputFields.find { it.id == "DifficultyCompletingTasks" }?.value?.toIntOrNull(),
            Forgetfulness = currentState.inputFields.find { it.id == "Forgetfulness" }?.value?.toIntOrNull()
        )

        viewModelScope.launch {
            try {
                val response = apiService.submitPatientData(payload)

                if (response.isSuccessful) {
                    val postResponse: PostResponse? = response.body()
                    if (postResponse != null) {
                        Log.d("PATIENT_VM_SUCCESS", "Server Status: ${postResponse.status}, Message: ${postResponse.message}")
                        _serverMessage.value = postResponse.message ?: "Success!"
                        val riskScoreText = postResponse.riskScore?.let { "Risk Score: $it" } ?: "Risk score not available."
                        val reliabilityScoreText = postResponse.reliabilityScore?.let { "Reliability Score: $it" } ?: "Reliability score not available."
                        _predictionResult.value = riskScoreText + reliabilityScoreText

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                submissionError = null
                            )
                        }
                    } else {
                        Log.e("PATIENT_VM_ERROR", "Response successful but body is null")
                        _uiState.update {
                            it.copy(isLoading = false, submissionError = "Success, but no response data from server.")
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = "Error: ${response.code()} - ${errorBody ?: "Unknown server error"}"
                    Log.e("PATIENT_VM_ERROR", errorMessage)
                    _uiState.update { it.copy(isLoading = false, submissionError = errorMessage) }
                }
            } catch (e: Exception) {
                Log.e("PATIENT_VM_EXCEPTION", "Exception: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        submissionError = e.message ?: "An unexpected error occurred. Please check your network connection."
                    )
                }
            }
        }
    }

    private fun isFormValid(fields: List<InputFieldModel>): Boolean {
        var isValid = true
        val updatedFields = fields.map { field ->
            var errorMessage: String? = null
            if (field.isMandatory && field.value.isBlank()) {
                errorMessage = "${field.label} is required."
                isValid = false
            }
            if (field.appInputType == AppInputType.NUMBER_INTEGER && field.value.toIntOrNull() == null && field.value.isNotBlank()) {
                errorMessage = "${field.label} must be a valid integer."
                isValid = false
            }
            field.copy(errorMessage = errorMessage)
        }
        if (!isValid) {
            _uiState.update { it.copy(inputFields = updatedFields) }
        }
        return isValid
    }
}