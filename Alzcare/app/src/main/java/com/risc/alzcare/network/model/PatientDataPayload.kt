package com.risc.alzcare.network.model

import kotlinx.serialization.Serializable

@Serializable
data class PatientDataPayload(
    val Age: Int? = null,
    val Gender: Int? = null,
    //val Ethnicity: Int? = null,
    val EducationLevel: Int? = null,
    //val BMI: Double? = null,
    val Smoking: Int? = null,
    val AlcoholConsumption: Double? = null,
    val PhysicalActivity: Double? = null,
    val DietQuality: Double? = null,
    val SleepQuality: Double? = null,
    val FamilyHistoryAlzheimers: Int? = null,
    val CardiovascularDisease: Int? = null,
    val Diabetes: Int? = null,
    val Depression: Int? = null,
    val HeadInjury: Int? = null,
    val Hypertension: Int? = null,
    //val SystolicBP: Int? = null,
    //val DiastolicBP: Int? = null,
    //val CholesterolTotal: Double? = null,
    //val CholesterolLDL: Double? = null,
    val CholesterolHDL: Double? = null,
    //val CholesterolTriglycerides: Double? = null,
    val MMSE: Double? = null,
    val FunctionalAssessment: Double? = null,
    val MemoryComplaints: Int? = null,
    val BehavioralProblems: Int? = null,
    //val ADL: Double? = null,
    val Confusion: Int? = null,
    val Disorientation: Int? = null,
    val PersonalityChanges: Int? = null,
    val DifficultyCompletingTasks: Int? = null,
    val Forgetfulness: Int? = null
)
