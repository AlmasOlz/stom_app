package com.example.stomatology.app.data.remote

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

data class CloudinaryUploadResponse(
    @SerializedName("secure_url")
    val secureUrl: String? = null,
    @SerializedName("url")
    val url: String? = null
)

interface CloudinaryApi {
    @Multipart
    @POST("{cloudName}/image/upload")
    suspend fun uploadImage(
        @Path("cloudName") cloudName: String,
        @Part file: MultipartBody.Part,
        @Part("upload_preset") uploadPreset: RequestBody
    ): CloudinaryUploadResponse
}
