package com.risc.alzcare.ui.questionnaire

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.risc.alzcare.R
import com.risc.alzcare.network.*
import com.risc.alzcare.network.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private fun getYesNoOptions(app: Application): List<Pair<String, String>> {
    val res = app.resources
    return listOf(
        res.getString(R.string.option_yes) to "1",
        res.getString(R.string.option_no) to "0"
    )
}

private fun getQuestions(app: Application): List<Question> {
    val res = app.resources
    return listOf(
        Question(
            id = "Age",
            text = res.getString(R.string.q_age_title),
            answerType = AnswerType.NUMBER_INTEGER
        ),
        Question(
            id = "Gender",
            text = res.getString(R.string.q_gender_title),
            answerType = AnswerType.SINGLE_CHOICE,
            options = listOf(res.getString(R.string.gender_male) to "0", res.getString(R.string.gender_female) to "1")
        ),
        Question(
            id = "EducationLevel",
            text = res.getString(R.string.q_education_title),
            answerType = AnswerType.SINGLE_CHOICE,
            options = listOf(
                res.getString(R.string.education_none) to "0",
                res.getString(R.string.education_secondary) to "1",
                res.getString(R.string.education_bachelors) to "2",
                res.getString(R.string.education_masters) to "3"
            )
        ),
        Question(
            id = "Smoking",
            text = res.getString(R.string.q_smoking_title),
            answerType = AnswerType.SINGLE_CHOICE,
            options = getYesNoOptions(app)
        ),
        Question(id = "AlcoholConsumption", text = res.getString(R.string.q_alcohol_title), answerType = AnswerType.NUMBER_INTEGER),
        Question(id = "PhysicalActivity", text = res.getString(R.string.q_activity_title), answerType = AnswerType.NUMBER_DECIMAL),
        Question(
            id = "DietQuality",
            text = res.getString(R.string.q_diet_title),
            answerType = AnswerType.NUMBER_INTEGER,
            valueRange = 0f..10f
        ),
        Question(
            id = "SleepQuality",
            text = res.getString(R.string.q_sleep_title),
            answerType = AnswerType.NUMBER_INTEGER,
            valueRange = 4f..10f
        ),
        Question(
            id = "FamilyHistoryAlzheimers",
            text = res.getString(R.string.q_family_history_title),
            answerType = AnswerType.SINGLE_CHOICE,
            options = getYesNoOptions(app)
        ),
        Question(
            id = "CardiovascularDisease",
            text = res.getString(R.string.q_cardiovascular_title),
            answerType = AnswerType.SINGLE_CHOICE,
            options = getYesNoOptions(app)
        ),
        Question(
            id = "Diabetes",
            text = res.getString(R.string.q_diabetes_title),
            answerType = AnswerType.SINGLE_CHOICE,
            options = getYesNoOptions(app)
        ),
        Question(
            id = "Depression",
            text = res.getString(R.string.q_depression_title),
            answerType = AnswerType.SINGLE_CHOICE,
            options = getYesNoOptions(app)
        ),
        Question(
            id = "HeadInjury",
            text = res.getString(R.string.q_head_injury_title),
            answerType = AnswerType.SINGLE_CHOICE,
            options = getYesNoOptions(app)
        ),
        Question(
            id = "Hypertension",
            text = res.getString(R.string.q_hypertension_title),
            answerType = AnswerType.SINGLE_CHOICE,
            options = getYesNoOptions(app)
        ),
        Question(id = "CholesterolHDL", text = res.getString(R.string.q_hdl_cholesterol_title), answerType = AnswerType.NUMBER_INTEGER),
        Question(id = "MMSE", text = res.getString(R.string.q_mmse_title), answerType = AnswerType.NUMBER_INTEGER),
        Question(id = "FunctionalAssessment", text = res.getString(R.string.q_functional_assessment_title), answerType = AnswerType.NUMBER_INTEGER),
        Question(
            id = "MemoryComplaints",
            text = res.getString(R.string.q_memory_complaints_title),
            answerType = AnswerType.SINGLE_CHOICE,
            options = getYesNoOptions(app)
        ),
        Question(
            id = "BehavioralProblems",
            text = res.getString(R.string.q_behavioral_problems_title),
            answerType = AnswerType.SINGLE_CHOICE,
            options = getYesNoOptions(app)
        ),
        Question(
            id = "Confusion",
            text = res.getString(R.string.q_confusion_title),
            answerType = AnswerType.SINGLE_CHOICE,
            options = getYesNoOptions(app)
        ),
        Question(
            id = "Disorientation",
            text = res.getString(R.string.q_disorientation_title),
            answerType = AnswerType.SINGLE_CHOICE,
            options = getYesNoOptions(app)
        ),
        Question(
            id = "PersonalityChanges",
            text = res.getString(R.string.q_personality_changes_title),
            answerType = AnswerType.SINGLE_CHOICE,
            options = getYesNoOptions(app)
        ),
        Question(
            id = "DifficultyCompletingTasks",
            text = res.getString(R.string.q_task_difficulty_title),
            answerType = AnswerType.SINGLE_CHOICE,
            options = getYesNoOptions(app)
        ),
        Question(
            id = "Forgetfulness",
            text = res.getString(R.string.q_forgetfulness_title),
            answerType = AnswerType.SINGLE_CHOICE,
            options = getYesNoOptions(app)
        )
    )
}

data class QuestionnaireUiState(
    val questions: List<Question>,
    val currentPage: Int = 0,
    val answers: Map<String, String> = emptyMap(),
    val isLastPage: Boolean = false,
    val isLoading: Boolean = false,
    val predictionNavTrigger: PostResponse? = null,
    val submissionError: String? = null
)

class QuestionnaireViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState: MutableStateFlow<QuestionnaireUiState>
    val uiState: StateFlow<QuestionnaireUiState>

    private val apiService: ApiInterface = RetrofitInstance.api

    init {
        val questions = getQuestions(application)
        _uiState = MutableStateFlow(QuestionnaireUiState(questions = questions))
        uiState = _uiState.asStateFlow()
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
        _uiState.update { it.copy(isLoading = true, predictionNavTrigger = null, submissionError = null) }

        viewModelScope.launch {
            try {
                val payload = PatientDataPayload(
                    Age = currentAnswers["Age"]?.toIntOrNull(),
                    Gender = currentAnswers["Gender"]?.toIntOrNull(),
                    EducationLevel = currentAnswers["EducationLevel"]?.toIntOrNull(),
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
                    CholesterolHDL = currentAnswers["CholesterolHDL"]?.toDoubleOrNull(),
                    MMSE = currentAnswers["MMSE"]?.toDoubleOrNull(),
                    FunctionalAssessment = currentAnswers["FunctionalAssessment"]?.toDoubleOrNull(),
                    MemoryComplaints = currentAnswers["MemoryComplaints"]?.toIntOrNull(),
                    BehavioralProblems = currentAnswers["BehavioralProblems"]?.toIntOrNull(),
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
                    if (postResponse != null && postResponse.success == true) {
                        _uiState.update {
                            it.copy(predictionNavTrigger = postResponse)
                        }
                        Log.d("QuestionnaireVM", "Submission Success: Ready for navigation. Status - ${postResponse.status}, Message - ${postResponse.message}")
                    } else {
                        val errorMessage = postResponse?.message ?: "Server indicated an issue with the submission."
                        _uiState.update {
                            it.copy(submissionError = errorMessage)
                        }
                        Log.e("QuestionnaireVM", "Submission not fully successful by backend: ${postResponse?.message}")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = "Error ${response.code()}: ${errorBody ?: "Unknown server error"}"
                    _uiState.update {
                        it.copy(submissionError = errorMessage)
                    }
                    Log.e("QuestionnaireVM", "Submission HTTP Error: ${response.code()} - $errorBody")
                }

            } catch (e: Exception) {
                val errorMessage = "Submission failed: ${e.message ?: "Network error"}"
                _uiState.update {
                    it.copy(submissionError = errorMessage)
                }
                Log.e("QuestionnaireVM", "Submission Exception", e)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun predictionNavigated() {
        _uiState.update { it.copy(predictionNavTrigger = null) }
    }

    fun clearSubmissionError() {
        _uiState.update { it.copy(submissionError = null) }
    }
}
