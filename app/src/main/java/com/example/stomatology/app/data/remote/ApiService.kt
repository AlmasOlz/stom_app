package com.example.stomatology.app.data.remote

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class AiResponseDto(
    @SerializedName("status") val status: String?,
    @SerializedName("teeth_count") val teethCount: Int?,
    @SerializedName("teeth_data") val teethData: List<ToothDataDto>? // ОСЫ ЖОЛ ӨТЕ МАҢЫЗДЫ!
)

data class ToothDataDto(
    @SerializedName("tooth_name") val toothName: String?,
    @SerializedName("conditions") val conditions: List<String>?
)

interface ApiService {
    @Multipart
    @POST("/analyze")
    suspend fun analyzeXray(
        @Part file: MultipartBody.Part
    ): AiResponseDto
}