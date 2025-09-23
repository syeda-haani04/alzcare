package com.risc.alzcare.ui.questionnaire

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.risc.alzcare.network.ApiInterface
import com.risc.alzcare.network.RetrofitInstance
import com.risc.alzcare.network.model.PatientDataPayload
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

val sampleQuestions: List<Question> = listOf(
    Question(
        id = "Age",
        text = "What is your current age? (60-90)",
        answerType = AnswerType.NUMBER_INTEGER
    ),
    Question(
        id = "Gender",
        text = "What is your gender?",
        answerType = AnswerType.SINGLE_CHOICE,
        options = listOf("Male" to "0", "Female" to "1")
    ),
    Question(
        id = "Ethnicity",
        text = "What is your ethnicity?",
        answerType = AnswerType.SINGLE_CHOICE,
        options = listOf(
            "Caucasian" to "0",
            "African American" to "1",
            "Asian" to "2",
            "Other" to "3"
        )
    ),
    Question(
        id = "EducationLevel",
        text = "What is your highest level of education?",
        answerType = AnswerType.SINGLE_CHOICE,
        options = listOf(
            "None" to "0",
            "High School" to "1",
            "Bachelor's Degree" to "2",
            "Higher Degree (Master's, PhD, etc.)" to "3"
        )
    ),
    Question(id = "BMI", text = "What is your Body Mass Index (BMI)? (15-40, e.g., 24.5)", answerType = AnswerType.NUMBER_DECIMAL),
    Question(
        id = "Smoking",
        text = "Do you currently smoke?",
        answerType = AnswerType.SINGLE_CHOICE,
        options = getYesNoOptions()
    ),
    Question(id = "AlcoholConsumption", text = "How many units of alcohol do you consume weekly? (0-20 units)", answerType = AnswerType.NUMBER_INTEGER),
    Question(id = "PhysicalActivity", text = "How many hours of physical activity do you engage in weekly? (0-10 hours)", answerType = AnswerType.NUMBER_DECIMAL),
    Question(
        id = "DietQuality",
        text = "On a scale of 0 to 10, how would you rate your diet quality?",
        answerType = AnswerType.NUMBER_INTEGER,
        valueRange = 0f..10f
    ),
    Question(
        id = "SleepQuality",
        text = "On a scale of 4 to 10, how would you rate your sleep quality?",
        answerType = AnswerType.NUMBER_INTEGER,
        valueRange = 4f..10f
    ),
    Question(
        id = "FamilyHistoryAlzheimers",
        text = "Do you have a family history of Alzheimer's Disease?",
        answerType = AnswerType.SINGLE_CHOICE,
        options = getYesNoOptions()
    ),
    Question(
        id = "CardiovascularDisease",
        text = "Have you been diagnosed with cardiovascular disease?",
        answerType = AnswerType.SINGLE_CHOICE,
        options = getYesNoOptions()
    ),
    Question(
        id = "Diabetes",
        text = "Have you been diagnosed with diabetes?",
        answerType = AnswerType.SINGLE_CHOICE,
        options = getYesNoOptions()
    ),
    Question(
        id = "Depression",
        text = "Have you been diagnosed with depression?",
        answerType = AnswerType.SINGLE_CHOICE,
        options = getYesNoOptions()
    ),
    Question(
        id = "HeadInjury",
        text = "Do you have a history of significant head injury?",
        answerType = AnswerType.SINGLE_CHOICE,
        options = getYesNoOptions()
    ),
    Question(
        id = "Hypertension",
        text = "Have you been diagnosed with hypertension (high blood pressure)?",
        answerType = AnswerType.SINGLE_CHOICE,
        options = getYesNoOptions()
    ),
    Question(id = "SystolicBP", text = "What is your typical Systolic Blood Pressure (mmHg)? (90-180, e.g., 120)", answerType = AnswerType.NUMBER_INTEGER),
    Question(id = "DiastolicBP", text = "What is your typical Diastolic Blood Pressure (mmHg)? (60-120, e.g., 80)", answerType = AnswerType.NUMBER_INTEGER),
    Question(id = "CholesterolTotal", text = "What is your Total Cholesterol level (mg/dL)? (150-300)", answerType = AnswerType.NUMBER_INTEGER),
    Question(id = "CholesterolLDL", text = "What is your LDL Cholesterol level (mg/dL)? (50-200)", answerType = AnswerType.NUMBER_INTEGER),
    Question(id = "CholesterolHDL", text = "What is your HDL Cholesterol level (mg/dL)? (20-100)", answerType = AnswerType.NUMBER_INTEGER),
    Question(id = "CholesterolTriglycerides", text = "What are your Triglycerides levels (mg/dL)? (50-400)", answerType = AnswerType.NUMBER_INTEGER),
    Question(id = "MMSE", text = "What is your Mini-Mental State Examination (MMSE) score? (0-30, lower scores indicate impairment)", answerType = AnswerType.NUMBER_INTEGER),
    Question(id = "FunctionalAssessment", text = "What is your Functional Assessment score? (0-10, lower scores indicate greater impairment)", answerType = AnswerType.NUMBER_INTEGER),
    Question(
        id = "MemoryComplaints",
        text = "Do you experience memory complaints?",
        answerType = AnswerType.SINGLE_CHOICE,
        options = getYesNoOptions()
    ),
    Question(
        id = "BehavioralProblems",
        text = "Have you experienced behavioral problems recently?",
        answerType = AnswerType.SINGLE_CHOICE,
        options = getYesNoOptions()
    ),
    Question(
        id = "ADL",
        text = "What is your Activities of Daily Living (ADL) score? (0-10, lower scores indicate greater impairment)",
        answerType = AnswerType.NUMBER_INTEGER
    ),
    Question(
        id = "Confusion",
        text = "Do you experience episodes of confusion?",
        answerType = AnswerType.SINGLE_CHOICE,
        options = getYesNoOptions()
    ),
    Question(
        id = "Disorientation",
        text = "Do you experience episodes of disorientation (e.g., to time or place)?",
        answerType = AnswerType.SINGLE_CHOICE,
        options = getYesNoOptions()
    ),
    Question(
        id = "PersonalityChanges",
        text = "Have you noticed significant personality changes?",
        answerType = AnswerType.SINGLE_CHOICE,
        options = getYesNoOptions()
    ),
    Question(
        id = "DifficultyCompletingTasks",
        text = "Do you have difficulty completing familiar tasks?",
        answerType = AnswerType.SINGLE_CHOICE,
        options = getYesNoOptions()
    ),
    Question(
        id = "Forgetfulness",
        text = "Do you experience significant forgetfulness beyond what is typical?",
        answerType = AnswerType.SINGLE_CHOICE,
        options = getYesNoOptions()
    )
)

data class QuestionnaireUiState(
    val questions: List<Question> = sampleQuestions,
    val currentPage: Int = 0,
    val answers: Map<String, String> = emptyMap(),
    val isLastPage: Boolean = false,
    val submissionStatus: String? = null,
    val isLoading: Boolean = false
)

class QuestionnaireViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(QuestionnaireUiState())
    val uiState: StateFlow<QuestionnaireUiState> = _uiState.asStateFlow()

    private val apiService: ApiInterface = RetrofitInstance.api

    init {
        _uiState.update { it.copy(isLastPage = it.questions.isNotEmpty() && it.currentPage == it.questions.size - 1) }
    }

    fun onPageChanged(page: Int) {
        _uiState.update {
            it.copy(
                currentPage = page,
                isLastPage = it.questions.isNotEmpty() && page == it.questions.size - 1
            )
        }
    }

    fun recordAnswer(questionId: String, answerCode: String) {
        _uiState.update { currentState ->
            val newAnswers = currentState.answers.toMutableMap()
            newAnswers[questionId] = answerCode
            currentState.copy(answers = newAnswers)
        }
    }

    fun submitQuestionnaire() {
        val currentAnswers = uiState.value.answers
        _uiState.update { it.copy(isLoading = true, submissionStatus = "Submitting...") }

        viewModelScope.launch {
            try {
                val payload = PatientDataPayload(
                    Age = currentAnswers["Age"]?.toIntOrNull(),
                    Gender = currentAnswers["Gender"]?.toIntOrNull(),
                    Ethnicity = currentAnswers["Ethnicity"]?.toIntOrNull(),
                    EducationLevel = currentAnswers["EducationLevel"]?.toIntOrNull(),
                    BMI = currentAnswers["BMI"]?.toDoubleOrNull(),
                    Smoking = currentAnswers["Smoking"]?.toIntOrNull(),
                    AlcoholConsumption = currentAnswers["AlcoholConsumption"]?.toDoubleOrNull(),
                    PhysicalActivity = currentAnswers["PhysicalActivity"]?.toDoubleOrNull(),
                    DietQuality = currentAnswers["DietQuality"]?.toIntOrNull()?.toDouble(),
                    SleepQuality = currentAnswers["SleepQuality"]?.toIntOrNull()?.toDouble(),
                    FamilyHistoryAlzheimers = currentAnswers["FamilyHistoryAlzheimers"]?.toIntOrNull(),
                    CardiovascularDisease = currentAnswers["CardiovascularDisease"]?.toIntOrNull(),
                    Diabetes = currentAnswers["Diabetes"]?.toIntOrNull(),
                    Depression = currentAnswers["Depression"]?.toIntOrNull(),
                    HeadInjury = currentAnswers["HeadInjury"]?.toIntOrNull(),
                    Hypertension = currentAnswers["Hypertension"]?.toIntOrNull(),
                    SystolicBP = currentAnswers["SystolicBP"]?.toIntOrNull(),
                    DiastolicBP = currentAnswers["DiastolicBP"]?.toIntOrNull(),
                    CholesterolTotal = currentAnswers["CholesterolTotal"]?.toDoubleOrNull(),
                    CholesterolLDL = currentAnswers["CholesterolLDL"]?.toDoubleOrNull(),
                    CholesterolHDL = currentAnswers["CholesterolHDL"]?.toDoubleOrNull(),
                    CholesterolTriglycerides = currentAnswers["CholesterolTriglycerides"]?.toDoubleOrNull(),
                    MMSE = currentAnswers["MMSE"]?.toDoubleOrNull(),
                    FunctionalAssessment = currentAnswers["FunctionalAssessment"]?.toDoubleOrNull(),
                    MemoryComplaints = currentAnswers["MemoryComplaints"]?.toIntOrNull(),
                    BehavioralProblems = currentAnswers["BehavioralProblems"]?.toIntOrNull(),
                    ADL = currentAnswers["ADL"]?.toDoubleOrNull(),
                    Confusion = currentAnswers["Confusion"]?.toIntOrNull(),
                    Disorientation = currentAnswers["Disorientation"]?.toIntOrNull(),
                    PersonalityChanges = currentAnswers["PersonalityChanges"]?.toIntOrNull(),
                    DifficultyCompletingTasks = currentAnswers["DifficultyCompletingTasks"]?.toIntOrNull(),
                    Forgetfulness = currentAnswers["Forgetfulness"]?.toIntOrNull()
                )

                Log.d("QuestionnaireVM", "Submitting Payload: $payload")
                val response = apiService.submitPatientData(payload)

                if (response.isSuccessful) {
                    val postResponse = response.body()
                    val successMessage = postResponse?.message ?: "Submitted successfully!"
                    val riskScoreMessage = postResponse?.riskScore?.let { " Risk Score: $it" } ?: ""
                    _uiState.update { it.copy(submissionStatus = "$successMessage$riskScoreMessage") }
                    Log.d("QuestionnaireVM", "Submission Success: Status - ${postResponse?.status}, Message - ${postResponse?.message}, Risk - ${postResponse?.riskScore}")
                } else {
                    val errorBody = response.errorBody()?.string()
                    _uiState.update { it.copy(submissionStatus = "Error ${response.code()}: ${errorBody ?: "Unknown server error"}") }
                    Log.e("QuestionnaireVM", "Submission Error: ${response.code()} - $errorBody")
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(submissionStatus = "Submission failed: ${e.message ?: "Network error"}") }
                Log.e("QuestionnaireVM", "Submission Exception", e)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun clearSubmissionStatus() {
        _uiState.update { it.copy(submissionStatus = null) }
    }
}