package com.risc.alzcare.network

import com.risc.alzcare.network.model.PatientDataPayload
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Defines the HTTP API endpoints for communication with the backend server.
 */
interface ApiInterface {

    /**
     * Simple GET endpoint, for testing server connectivity or fetching basic data.
     */
    @GET("hello")
    suspend fun getData(): Response<ResponseData>

    /**
     * Submits patient data to the server.
     * @param payload The patient data to be submitted.
     * @return A response from the server, typically indicating success or failure.
     */
    @POST("submit_patient_data")
    suspend fun submitPatientData(@Body payload: PatientDataPayload): Response<PostResponse>

    // @GET("patients/{patientId}")
    // suspend fun getPatientDetails(@Path("patientId") id: String): Response<PatientDetailsResponse>
}