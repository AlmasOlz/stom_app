package com.example.stomatology.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.example.stomatology.app.data.remote.ApiService
import com.example.stomatology.app.domain.model.AiAnalysisResult
import com.example.stomatology.app.domain.model.Clinic
import com.example.stomatology.app.domain.model.Finding
import com.example.stomatology.app.domain.repository.AppRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class AppRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val apiService: ApiService
) : AppRepository {

    override fun getClinics(): Flow<List<Clinic>> = callbackFlow {
        val listener = firestore.collection("clinics")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val clinics = snapshot?.documents?.mapNotNull { it.toObject(Clinic::class.java) } ?: emptyList()
                trySend(clinics)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun analyzeImage(file: File): AiAnalysisResult {
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        val response = apiService.analyzeXray(body)

        // Қорғаныс: Егер response.teethData null болса, emptyList() қайтарамыз
        val findings = response.teethData?.map { dto ->
            Finding(
                toothClass = dto.toothName ?: "",
                conditions = dto.conditions ?: emptyList()
            )
        } ?: emptyList()

        return AiAnalysisResult(
            teethCount = response.teethCount ?: 0,
            findings = findings
        )
    }
}