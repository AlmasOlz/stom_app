package com.example.stomatology.app.data.remote

import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import com.google.gson.annotations.SerializedName

data class AiResponseDto(
    @SerializedName("status") val status: String,
    @SerializedName("teeth_count") val teethCount: Int,
    @SerializedName("findings") val findings: List<FindingDto>
)

data class FindingDto(
    @SerializedName("class") val toothClass: String,
    @SerializedName("confidence") val confidence: Double
)

interface ApiService {
    @Multipart
    @POST("/analyze")
    suspend fun analyzeXray(
        @Part file: MultipartBody.Part
    ): AiResponseDto
}