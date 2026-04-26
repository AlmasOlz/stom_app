package com.example.stomatology.app.data.repository

import com.example.stomatology.app.core.util.Resource
import com.example.stomatology.app.data.local.ClinicDao
import com.example.stomatology.app.data.local.ClinicEntity
import com.example.stomatology.app.data.local.toDomain
import com.example.stomatology.app.data.remote.ApiService
import com.example.stomatology.app.domain.model.AiAnalysisResult
import com.example.stomatology.app.domain.model.Clinic
import com.example.stomatology.app.domain.model.Finding
import com.example.stomatology.app.domain.model.ToothDetectionState
import com.example.stomatology.app.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

private val ALL_TEETH = listOf(
    "LL1", "LL2", "LL3", "LL4", "LL5", "LL6", "LL7", "LL8",
    "LU1", "LU2", "LU3", "LU4", "LU5", "LU6", "LU7", "LU8",
    "RL1", "RL2", "RL3", "RL4", "RL5", "RL6", "RL7", "RL8",
    "RU1", "RU2", "RU3", "RU4", "RU5", "RU6", "RU7", "RU8",
)

class AppRepositoryImpl @Inject constructor(
    private val clinicDao: ClinicDao,
    private val apiService: ApiService
) : AppRepository {

    override suspend fun analyzeImage(file: File): Resource<AiAnalysisResult> {
        return try {
            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            val multipart = MultipartBody.Part.createFormData("file", file.name, requestBody)

            val response = apiService.analyzeXray(multipart)

            val toothMap: Map<String, Finding> = response.teethData
                ?.mapNotNull { dto ->
                    val name = dto.toothName ?: return@mapNotNull null
                    name to Finding(
                        toothClass = name,
                        state = ToothDetectionState.fromString(dto.state),
                        conditions = dto.conditions.orEmpty()
                    )
                }
                ?.toMap()
                ?: emptyMap()

            val findings: List<Finding> = ALL_TEETH.map { toothCode ->
                toothMap[toothCode] ?: Finding(
                    toothClass = toothCode,
                    state = ToothDetectionState.UNKNOWN,
                    conditions = emptyList()
                )
            }

            val result = AiAnalysisResult(
                teethCount = response.teethCount ?: 0,
                findings = findings
            )

            Resource.Success(result)
        } catch (e: Exception) {
            Resource.Error(
                message = e.localizedMessage ?: "Failed to analyze image",
                throwable = e
            )
        }
    }

    override fun getClinics(): Flow<Resource<List<Clinic>>> = flow {
        emit(Resource.Loading)

        seedClinicsIfNeeded()

        clinicDao.getClinics().collect { entities ->
            emit(Resource.Success(entities.map { entity -> entity.toDomain() }))
        }
    }

    private suspend fun seedClinicsIfNeeded() {
        if (clinicDao.count() == 0) {
            clinicDao.insertAll(
                listOf(
                    ClinicEntity(
                        id = "1",
                        name = "OneDent",
                        rating = 4.7,
                        reviews = 120,
                        address = "Астана, Жиембет жырау 2",
                        services = "Удаление зуба|Пломба / Канал|Брекеты",
                        priceFrom = 12000,
                        description = "Современная стоматология с терапией, удалением и ортодонтией."
                    ),
                    ClinicEntity(
                        id = "2",
                        name = "Dent Lux",
                        rating = 4.5,
                        reviews = 98,
                        address = "Астана, Абая 15",
                        services = "Протезирование|Имплант",
                        priceFrom = 35000,
                        description = "Клиника с упором на имплантацию и протезирование."
                    ),
                    ClinicEntity(
                        id = "3",
                        name = "Astana Stom",
                        rating = 4.6,
                        reviews = 76,
                        address = "Астана, Мангилик Ел 30",
                        services = "Удаление зуба|Имплант",
                        priceFrom = 18000,
                        description = "Хирургические услуги и имплантация."
                    ),
                    ClinicEntity(
                        id = "4",
                        name = "Agzamov Clinic",
                        rating = 4.8,
                        reviews = 154,
                        address = "Астана, Кабанбай батыр 44",
                        services = "Пломба / Канал|Протезирование|Брекеты",
                        priceFrom = 15000,
                        description = "Широкий спектр услуг от лечения каналов до брекетов."
                    ),
                    ClinicEntity(
                        id = "5",
                        name = "Dent Love",
                        rating = 4.4,
                        reviews = 63,
                        address = "Астана, Сарайшик 10",
                        services = "Брекеты|Пломба / Канал",
                        priceFrom = 14000,
                        description = "Уютная клиника для лечения и ортодонтии."
                    )
                )
            )
        }
    }
}