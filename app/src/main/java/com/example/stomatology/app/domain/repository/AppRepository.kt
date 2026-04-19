package com.example.stomatology.app.domain.repository

import com.example.stomatology.app.core.util.Resource
import com.example.stomatology.app.domain.model.AiAnalysisResult
import com.example.stomatology.app.domain.model.Clinic
import kotlinx.coroutines.flow.Flow
import java.io.File

interface AppRepository {
    fun getClinics(): Flow<Resource<List<Clinic>>>
    suspend fun analyzeImage(file: File): Resource<AiAnalysisResult>
}