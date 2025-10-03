package com.risc.alzcare.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PostResponse(
    @SerialName("status")
    val status: String,
    @SerialName("success")
    val success: Boolean? = null,
    @SerialName("message")
    val message: String? = null,
    @SerialName("patient_id")
    val patientId: Int? = null,
    @SerialName("risk_score")
    val riskScore: Double? = null,
    @SerialName("reliability_score")
    val reliabilityScore: Double? = null,
    @SerialName("data")
    val data: ResponseData? = null,
    @SerialName("next_steps")
    val nextSteps: String? = null

)