package com.risc.alzcare

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET

object RetrofitInstance{
    private const val BASE_URL ="http://192.168.0.115:5000/"
    fun getInstance(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                Json.asConverterFactory(
                    "application/json; charset=UTF8".toMediaType()))
            .build()
    }

    val api: ApiInterface by lazy {
        getInstance().create(ApiInterface::class.java)
    }
}

@Serializable
data class ResponseData(
    val hello: String,
    val hi: String,
    val oye: String
)

@Parcelize
data class UserInputs(val hello: String, val hi: String, val oye: String): Parcelable

interface ApiInterface {
    @GET("hello")
    suspend fun getData(): Response<ResponseData>
}
