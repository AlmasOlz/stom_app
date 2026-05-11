package com.example.stomatology.app.data.repository

import android.content.Context
import com.example.stomatology.app.R
import com.example.stomatology.app.core.firebase.FirestoreCollections
import com.example.stomatology.app.core.firebase.FirestoreFields
import com.example.stomatology.app.core.util.Resource
import com.example.stomatology.app.data.local.ClinicDao
import com.example.stomatology.app.data.local.ClinicEntity
import com.example.stomatology.app.data.local.toDomain
import com.example.stomatology.app.data.remote.ApiService
import com.example.stomatology.app.domain.model.AiAnalysisResult
import com.example.stomatology.app.domain.model.Clinic
import com.example.stomatology.app.domain.model.Finding
import com.example.stomatology.app.domain.model.ServicePrice
import com.example.stomatology.app.domain.model.ToothDetectionState
import com.example.stomatology.app.domain.repository.AppRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
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
    private val apiService: ApiService,
    private val firestore: FirebaseFirestore,
    @ApplicationContext private val context: Context
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

            Resource.Success(
                AiAnalysisResult(
                    teethCount = response.teethCount ?: 0,
                    findings = findings
                )
            )
        } catch (e: Exception) {
            Resource.Error(
                message = mapAiApiError(e),
                throwable = e
            )
        }
    }

    override fun getClinics(): Flow<Resource<List<Clinic>>> {
        val remoteFlow: Flow<Resource<List<Clinic>>> = firestore.collection(FirestoreCollections.CLINICS)
            .snapshots()
            .map { snapshot ->
                val remoteClinics = snapshot.documents
                    .map { document -> document.toClinic() }
                    .filter { clinic -> clinic.name.isNotBlank() }

                if (remoteClinics.isNotEmpty()) {
                    syncLocalClinics(remoteClinics)
                    Resource.Success(remoteClinics)
                } else {
                    val localClinics = clinicDao.getClinics().first().map { entity -> entity.toDomain() }
                    Resource.Success(localClinics)
                }
            }

        val localFallbackFlow: Flow<Resource<List<Clinic>>> = clinicDao.getClinics().map { entities ->
            Resource.Success(entities.map { entity -> entity.toDomain() })
        }

        return flow {
            seedClinicsIfNeeded()
            emitAll(remoteFlow)
        }.onStart {
            emit(Resource.Loading)
        }.catch {
            emitAll(localFallbackFlow)
        }
    }

    private suspend fun seedClinicsIfNeeded() {
        if (clinicDao.count() == 0) {
            clinicDao.insertAll(loadDefaultClinics())
        }
    }

    private fun loadDefaultClinics(): List<ClinicEntity> {
        val json = context.resources.openRawResource(R.raw.default_clinics)
            .bufferedReader()
            .use { it.readText() }
        val type = object : TypeToken<List<DefaultClinicDto>>() {}.type
        val clinics = Gson().fromJson<List<DefaultClinicDto>>(json, type)

        return clinics.map { clinic ->
            ClinicEntity(
                id = clinic.id,
                name = clinic.name,
                rating = clinic.rating,
                reviews = clinic.reviews,
                address = clinic.address,
                services = clinic.services.joinToString("|"),
                imageUrl = clinic.imageUrl,
                priceFrom = clinic.priceFrom,
                description = clinic.description,
                latitude = clinic.latitude,
                longitude = clinic.longitude
            )
        }
    }

    private suspend fun syncLocalClinics(clinics: List<Clinic>) {
        val entities = clinics.map { clinic ->
            ClinicEntity(
                id = clinic.id,
                name = clinic.name,
                rating = clinic.rating,
                reviews = clinic.reviews,
                address = clinic.address,
                services = clinic.services.joinToString("|"),
                imageUrl = clinic.imageUrl,
                priceFrom = clinic.priceFrom,
                description = clinic.description,
                latitude = clinic.latitude,
                longitude = clinic.longitude
            )
        }
        clinicDao.insertAll(entities)
    }

    private fun DocumentSnapshot.toClinic(): Clinic {
        val services = (get(FirestoreFields.SERVICES) as? List<*>)
            ?.mapNotNull { value -> value as? String }
            ?: emptyList()
        val priceList = parsePriceList(get(FirestoreFields.PRICE_LIST))
        val rawPriceFrom = get(FirestoreFields.PRICE_FROM)
        val priceFrom = parseIntValue(rawPriceFrom)
            ?: priceList.map { it.price }.filter { it > 0 }.minOrNull()
            ?: 0

        return Clinic(
            id = id,
            name = getString(FirestoreFields.NAME).orEmpty(),
            rating = getDouble(FirestoreFields.RATING) ?: 0.0,
            reviews = getLong(FirestoreFields.REVIEWS)?.toInt() ?: 0,
            address = getString(FirestoreFields.ADDRESS).orEmpty(),
            phone = getString(FirestoreFields.PHONE).orEmpty(),
            services = services,
            imageUrl = getString(FirestoreFields.IMAGE_URL).orEmpty(),
            priceFrom = priceFrom,
            priceList = priceList,
            description = getString(FirestoreFields.DESCRIPTION).orEmpty(),
            latitude = getDouble(FirestoreFields.LATITUDE) ?: 0.0,
            longitude = getDouble(FirestoreFields.LONGITUDE) ?: 0.0
        )
    }
}

private data class DefaultClinicDto(
    val id: String,
    val name: String,
    val rating: Double,
    val reviews: Int,
    val address: String,
    val services: List<String>,
    val imageUrl: String,
    val priceFrom: Int,
    val description: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

private fun parseIntValue(value: Any?): Int? {
    return when (value) {
        is Int -> value
        is Long -> value.toInt()
        is Double -> value.toInt()
        is Float -> value.toInt()
        is String -> value.trim().toIntOrNull()
        else -> null
    }?.takeIf { it >= 0 }
}

private fun extractMinPriceFromPriceList(raw: Any?): Int? {
    val list = raw as? List<*> ?: return null
    val prices = list.mapNotNull { item ->
        val map = item as? Map<*, *> ?: return@mapNotNull null
        parseIntValue(map["price"])
    }
    return prices.minOrNull()
}

private fun parsePriceList(raw: Any?): List<ServicePrice> {
    val list = raw as? List<*> ?: return emptyList()
    return list.mapNotNull { item ->
        val map = item as? Map<*, *> ?: return@mapNotNull null
        val service = (map["service"] ?: map["name"] ?: map["title"])?.toString()?.trim().orEmpty()
        val price = parseIntValue(map["price"]) ?: 0
        if (service.isBlank()) null else ServicePrice(service = service, price = price)
    }
}

private fun mapAiApiError(error: Throwable): String {
    val lower = error.message.orEmpty().lowercase()
    return when {
        lower.contains("failed to connect") ||
            lower.contains("connection refused") ||
            lower.contains("unable to resolve host") -> {
            "AI сервері қолжетімсіз. Кейінірек қайталап көріңіз."
        }
        lower.contains("timeout") -> {
            "Сұраныс уақыты аяқталды. Қайталап көріңіз."
        }
        lower.contains("network") ||
            lower.contains("host") ||
            lower.contains("internet") ||
            lower.contains("socket") -> {
            "Интернет байланысын тексеріңіз."
        }
        else -> "Суретті талдау мүмкін болмады."
    }
}
