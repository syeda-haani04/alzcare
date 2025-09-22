package com.risc.alzcare.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseData(
    @SerialName("Age")
    val age: Int? = null,
    @SerialName("Gender")
    val gender: Int? = null,
    @SerialName("Ethnicity")
    val ethnicity: Int? = null,
    @SerialName("EducationLevel")
    val educationLevel: Int? = null,
    @SerialName("BMI")
    val bmi: Double? = null,
    @SerialName("Smoking")
    val smoking: Int? = null,
    @SerialName("AlcoholConsumption")
    val alcoholConsumption: Double? = null,
    @SerialName("PhysicalActivity")
    val physicalActivity: Double? = null,
    @SerialName("DietQuality")
    val dietQuality: Double? = null,
    @SerialName("SleepQuality")
    val sleepQuality: Double? = null,
    @SerialName("FamilyHistoryAlzheimers")
    val familyHistoryAlzheimers: Int? = null,
    @SerialName("CardiovascularDisease")
    val cardiovascularDisease: Int? = null,
    @SerialName("Diabetes")
    val diabetes: Int? = null,
    @SerialName("Depression")
    val depression: Int? = null,
    @SerialName("HeadInjury")
    val headInjury: Int? = null,
    @SerialName("Hypertension")
    val hypertension: Int? = null,
    @SerialName("SystolicBP")
    val systolicBP: Int? = null,
    @SerialName("DiastolicBP")
    val diastolicBP: Int? = null,
    @SerialName("CholesterolTotal")
    val cholesterolTotal: Double? = null,
    @SerialName("CholesterolLDL")
    val cholesterolLDL: Double? = null,
    @SerialName("CholesterolHDL")
    val cholesterolHDL: Double? = null,
    @SerialName("CholesterolTriglycerides")
    val cholesterolTriglycerides: Double? = null,
    @SerialName("MMSE")
    val mmse: Double? = null,
    @SerialName("FunctionalAssessment")
    val functionalAssessment: Double? = null,
    @SerialName("MemoryComplaints")
    val memoryComplaints: Int? = null,
    @SerialName("BehavioralProblems")
    val behavioralProblems: Int? = null,
    @SerialName("ADL")
    val adl: Double? = null,
    @SerialName("Confusion")
    val confusion: Int? = null,
    @SerialName("Disorientation")
    val disorientation: Int? = null,
    @SerialName("PersonalityChanges")
    val personalityChanges: Int? = null,
    @SerialName("DifficultyCompletingTasks")
    val difficultyCompletingTasks: Int? = null,
    @SerialName("Forgetfulness")
    val forgetfulness: Int? = null
)