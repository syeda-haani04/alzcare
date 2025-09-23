package com.risc.alzcare.network

import com.risc.alzcare.network.model.PatientDataPayload
import com.risc.alzcare.network.model.PostResponse
import com.risc.alzcare.network.model.ResponseData
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiInterface {

    //@GET("hello")
    //suspend fun getData(): Response<HelloResponse>

    @POST("api/patientdata")
    suspend fun submitPatientData(@Body payload: PatientDataPayload): Response<PostResponse>
}

//@Serializable
//data class HelloResponse(
//    val message: String
//)
