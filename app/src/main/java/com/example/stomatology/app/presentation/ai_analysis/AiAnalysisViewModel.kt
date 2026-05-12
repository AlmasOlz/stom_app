package com.example.stomatology.app.presentation.ai_analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stomatology.app.core.firebase.FirestoreCollections
import com.example.stomatology.app.core.firebase.FirestoreFields
import com.example.stomatology.app.core.util.Resource
import com.example.stomatology.app.domain.model.AiAnalysisResult
import com.example.stomatology.app.domain.model.Clinic
import com.example.stomatology.app.domain.model.Finding
import com.example.stomatology.app.domain.model.ServicePrice
import com.example.stomatology.app.domain.model.ToothDetectionState
import com.example.stomatology.app.domain.repository.AppRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class AiAnalysisViewModel @Inject constructor(
    private val repository: AppRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow<AiState>(AiState.Idle)
    val uiState: StateFlow<AiState> = _uiState

    fun analyzeImage(file: File) {
        viewModelScope.launch {
            _uiState.value = AiState.Loading

            when (val result = repository.analyzeImage(file)) {
                is Resource.Success -> {
                    val clinics = loadClinicsForReport()
                    val report = buildAnalysisReport(result.data, clinics)
                    _uiState.value = AiState.Success(
                        result = result.data,
                        report = report
                    )
                }

                is Resource.Error -> {
                    _uiState.value = AiState.Error(
                        result.message.ifBlank { "Талдауды орындау мүмкін болмады" }
                    )
                }

                is Resource.Loading -> {
                    _uiState.value = AiState.Loading
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = AiState.Idle
    }


    fun analyzeComplaint(raw: String) {
        val complaint = raw.trim()
        if (complaint.isEmpty()) return
        viewModelScope.launch {
            _uiState.value = AiState.ComplaintSuccess(
                complaint = complaint,
                recommendation = buildComplaintRecommendation(complaint)
            )
        }
    }

    private fun buildComplaintRecommendation(text: String): String {
        val t = text.lowercase()
        return when {
            t.contains("ауыр") || t.contains("ауыру") || t.contains("болып тұр") ->
                "Ауырсу кариес тереңдеуі, пульпит немесе периодонтит сияқты жағдайлармен байланысты болуы мүмкін. Мүмкіндігінше тез стоматологқа көрініңіз."

            t.contains("қан") || t.contains("ісін") || t.contains("қабыну") ->
                "Қабыну немесе қан кету тіс немесе түбір аймағының зақымдануын көрсетуі мүмкін. Рентген және дәрігер тексерісі ұсынылады."

            t.contains("суық") || t.contains("ыстық") || t.contains("сезімтал") ->
                "Температуралық сезімталдық эмаль зақымы немесе терең кариес белгісі болуы мүмкін. Кәсіби тексеру керек."

            t.contains("ретген") || t.contains("рентген") || t.contains("сурет жүк") ->
                "Мәтін бойынша нақты диагноз қою мүмкін емес. «Рентген суретін таңдау» арқылы сурет жүктеп, AI талдауын алыңыз."

            else ->
                "Бұл тек алдын ала ақпарат, медициналық диагноз емес. Нақты диагноз бен ем жоспарын стоматолог айқындайды."
        }
    }

    private suspend fun loadClinicsForReport(): List<Clinic> {
    val fromFirestore = loadClinicsDirectlyFromFirestore()
    if (fromFirestore.isNotEmpty()) {
        return fromFirestore
    }

    return runCatching {
        when (val result = repository.getClinics().first { it !is Resource.Loading }) {
            is Resource.Success -> result.data
            is Resource.Error -> emptyList()
            is Resource.Loading -> emptyList()
        }
    }.getOrDefault(emptyList())
}

    private suspend fun loadClinicsDirectlyFromFirestore(): List<Clinic> {
        return runCatching {
            val snapshot = firestore.collection(FirestoreCollections.CLINICS)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                val clinic = doc.toClinicForAi()
                clinic.takeIf { it.name.isNotBlank() }
            }
        }.getOrDefault(emptyList())
    }
}

sealed class AiState {
    object Idle : AiState()
    object Loading : AiState()

    data class Success(
        val result: AiAnalysisResult,
        val report: AiAnalysisReport
    ) : AiState()

    data class ComplaintSuccess(
        val complaint: String,
        val recommendation: String
    ) : AiState()

    data class Error(val message: String) : AiState()
}

data class AiAnalysisReport(
    val detectedTeeth: Int,
    val healthyTeeth: Int,
    val unknownTeeth: Int,
    val missingTeeth: Int,
    val problemTeeth: List<ToothReportItem>,
    val activeTreatmentTeeth: List<ToothReportItem>,
    val checklist: List<AnalysisChecklistItem>,
    val priceSummary: TreatmentPriceSummary
) {
    val cariesCount: Int = problemTeeth.count { item ->
        item.conditions.any { it == "caries" }
    }

    val urgentCount: Int = problemTeeth.count { item ->
        item.conditions.any { it == "abscess" || it == "periapical_lesion" }
    }

    val restorationCount: Int = problemTeeth.count { item ->
        item.conditions.any { it in RESTORATION_CONDITIONS }
    }
}

data class ToothReportItem(
    val displayNumber: Int,
    val toothCode: String,
    val conditions: List<String>,
    val conditionText: String,
    val recommendedService: String,
    val severity: TreatmentSeverity
)

data class AnalysisChecklistItem(
    val title: String,
    val description: String,
    val severity: TreatmentSeverity
)

data class TreatmentPriceSummary(
    val affectedTeethCount: Int,
    val averagePerTooth: Int,
    val averageTotal: Int,
    val minTotal: Int,
    val maxTotal: Int,
    val clinicEstimates: List<ClinicTreatmentEstimate>
) {
    val hasPriceData: Boolean = averageTotal > 0
}

data class ClinicTreatmentEstimate(
    val clinicId: String,
    val clinicName: String,
    val matchedServices: List<String>,
    val averagePerTooth: Int,
    val totalEstimate: Int
)

enum class TreatmentSeverity {
    NORMAL,
    ATTENTION,
    IMPORTANT,
    URGENT
}

private val ACTIVE_TREATMENT_CONDITIONS = setOf(
    "abscess",
    "periapical_lesion",
    "caries",
    "residual_root",
    "impacted_tooth"
)

private val RESTORATION_CONDITIONS = setOf(
    "filling",
    "implant",
    "crown",
    "root_canal_treatment",
    "post",
    "endocrown",
    "veneer"
)

private fun buildAnalysisReport(
    result: AiAnalysisResult,
    clinics: List<Clinic>
): AiAnalysisReport {
    val detectedFindings = result.findings.filter { it.state == ToothDetectionState.DETECTED }
    val problemTeeth = detectedFindings
        .filter { it.conditions.isNotEmpty() }
        .map { it.toReportItem() }
        .sortedWith(
            compareByDescending<ToothReportItem> { it.severity.ordinal }
                .thenBy { it.displayNumber }
        )

    val activeTreatmentTeeth = problemTeeth.filter { item ->
        item.conditions.any { it in ACTIVE_TREATMENT_CONDITIONS }
    }

    val teethForPriceEstimate = if (activeTreatmentTeeth.isNotEmpty()) {
        activeTreatmentTeeth
    } else {
        problemTeeth
    }

    val priceSummary = buildTreatmentPriceSummary(
        clinics = clinics,
        treatmentTeeth = teethForPriceEstimate
    )

    return AiAnalysisReport(
        detectedTeeth = result.teethCount,
        healthyTeeth = detectedFindings.count { it.conditions.isEmpty() },
        unknownTeeth = result.findings.count { it.state == ToothDetectionState.UNKNOWN },
        missingTeeth = result.findings.count { it.state == ToothDetectionState.MISSING },
        problemTeeth = problemTeeth,
        activeTreatmentTeeth = activeTreatmentTeeth,
        checklist = buildChecklist(problemTeeth, activeTreatmentTeeth, priceSummary),
        priceSummary = priceSummary
    )
}

private fun Finding.toReportItem(): ToothReportItem {
    val normalizedConditions = conditions.map { it.lowercase().trim() }.filter { it.isNotBlank() }
    val mainCondition = normalizedConditions.maxByOrNull { conditionPriority(it) }.orEmpty()

    return ToothReportItem(
        displayNumber = mapToothCodeToDisplayNumber(toothClass),
        toothCode = toothClass,
        conditions = normalizedConditions,
        conditionText = normalizedConditions
            .map { conditionLabelForReport(it) }
            .joinToString(", "),
        recommendedService = recommendedServiceForCondition(mainCondition),
        severity = severityForConditions(normalizedConditions)
    )
}

private fun buildChecklist(
    problemTeeth: List<ToothReportItem>,
    activeTreatmentTeeth: List<ToothReportItem>,
    priceSummary: TreatmentPriceSummary
): List<AnalysisChecklistItem> {
    val checklist = mutableListOf<AnalysisChecklistItem>()

    val urgentTeeth = problemTeeth.filter { it.severity == TreatmentSeverity.URGENT }
    if (urgentTeeth.isNotEmpty()) {
        checklist += AnalysisChecklistItem(
            title = "Шұғыл түрде стоматологқа жазылыңыз",
            description = "Тексерілетін тістер: ${urgentTeeth.toDisplayNumbersText()}. Қабыну немесе абсцесс белгісі анықталды.",
            severity = TreatmentSeverity.URGENT
        )
    }

    val cariesTeeth = problemTeeth.filter { item -> item.conditions.any { it == "caries" } }
    if (cariesTeeth.isNotEmpty()) {
        checklist += AnalysisChecklistItem(
            title = "Кариесті емдеуді жоспарлау",
            description = "Кариес белгісі бар тістер: ${cariesTeeth.toDisplayNumbersText()}. Ерте емделсе, канал емдеу қаупі төмендейді.",
            severity = TreatmentSeverity.IMPORTANT
        )
    }

    val surgeryTeeth = activeTreatmentTeeth.filter { item ->
        item.conditions.any { it == "residual_root" || it == "impacted_tooth" }
    }
    if (surgeryTeeth.isNotEmpty()) {
        checklist += AnalysisChecklistItem(
            title = "Хирургиялық емді талқылау",
            description = "Жұлуды немесе хирургиялық бақылауды қажет етуі мүмкін тістер/түбірлер: ${surgeryTeeth.toDisplayNumbersText()}.",
            severity = TreatmentSeverity.IMPORTANT
        )
    }

    val restorationTeeth = problemTeeth.filter { item ->
        item.conditions.any { it in RESTORATION_CONDITIONS }
    }
    if (restorationTeeth.isNotEmpty()) {
        checklist += AnalysisChecklistItem(
            title = "Бұрынғы реставрацияларды тексеру",
            description = "Пломба, сауыт, имплант немесе канал емі бар тістер: ${restorationTeeth.toDisplayNumbersText()}.",
            severity = TreatmentSeverity.ATTENTION
        )
    }

    if (priceSummary.hasPriceData) {
        checklist += AnalysisChecklistItem(
            title = "Клиникалар бағасын салыстыру",
            description = "Емдеудің орташа бағасы: ${formatTenge(priceSummary.averageTotal)}. Төменде тіркелген клиникалар бойынша есеп көрсетілген.",
            severity = TreatmentSeverity.NORMAL
        )
    }

    if (checklist.isEmpty()) {
        checklist += AnalysisChecklistItem(
            title = "Профилактикалық тексеріс",
            description = "Суретте белсенді мәселе анықталмады. Дәрігердің жоспарлы тексерісі және кәсіби тазалау ұсынылады.",
            severity = TreatmentSeverity.NORMAL
        )
    }

    return checklist
}

private fun buildTreatmentPriceSummary(
    clinics: List<Clinic>,
    treatmentTeeth: List<ToothReportItem>
): TreatmentPriceSummary {
    if (treatmentTeeth.isEmpty()) {
        return TreatmentPriceSummary(
            affectedTeethCount = 0,
            averagePerTooth = 0,
            averageTotal = 0,
            minTotal = 0,
            maxTotal = 0,
            clinicEstimates = emptyList()
        )
    }

    if (clinics.isEmpty()) {
        val fallbackPerTooth = treatmentTeeth.map { tooth ->
            estimateToothTreatmentPrice(
                clinicBasePrice = 15000,
                clinic = Clinic(),
                conditions = tooth.conditions
            )
        }.average().roundToInt()
        val fallbackTotal = fallbackPerTooth * treatmentTeeth.size

        return TreatmentPriceSummary(
            affectedTeethCount = treatmentTeeth.size,
            averagePerTooth = fallbackPerTooth,
            averageTotal = fallbackTotal,
            minTotal = fallbackTotal,
            maxTotal = fallbackTotal,
            clinicEstimates = emptyList()
        )
    }

    val estimates = clinics
        .mapNotNull { clinic ->
            val clinicBasePrice = clinic.baseTreatmentPrice()
            if (clinicBasePrice <= 0) {
                null
            } else {
                val total = treatmentTeeth.sumOf { tooth ->
                    estimateToothTreatmentPrice(
                        clinicBasePrice = clinicBasePrice,
                        clinic = clinic,
                        conditions = tooth.conditions
                    )
                }
                val matchedServices = collectMatchedServicesForClinic(clinic, treatmentTeeth)

                ClinicTreatmentEstimate(
                    clinicId = clinic.id,
                    clinicName = clinic.name,
                    matchedServices = matchedServices,
                    averagePerTooth = total / treatmentTeeth.size,
                    totalEstimate = total
                )
            }
        }
        .sortedBy { it.totalEstimate }

    if (estimates.isEmpty()) {
        return TreatmentPriceSummary(
            affectedTeethCount = treatmentTeeth.size,
            averagePerTooth = 0,
            averageTotal = 0,
            minTotal = 0,
            maxTotal = 0,
            clinicEstimates = emptyList()
        )
    }

    val averageTotal = estimates.map { it.totalEstimate }.average().roundToInt()
    val averagePerTooth = estimates.map { it.averagePerTooth }.average().roundToInt()

    return TreatmentPriceSummary(
        affectedTeethCount = treatmentTeeth.size,
        averagePerTooth = averagePerTooth,
        averageTotal = averageTotal,
        minTotal = estimates.minOf { it.totalEstimate },
        maxTotal = estimates.maxOf { it.totalEstimate },
        clinicEstimates = estimates
    )
}

private fun estimateToothTreatmentPrice(
    clinicBasePrice: Int,
    clinic: Clinic,
    conditions: List<String>
): Int {
    val priceByService = priceByMatchedService(clinic, conditions)
    if (priceByService > 0) {
        return priceByService
    }

    val multiplier = conditions.maxOfOrNull { condition ->
        when (condition) {
            "abscess" -> 2.2
            "periapical_lesion" -> 1.9
            "impacted_tooth" -> 1.7
            "residual_root" -> 1.5
            "caries" -> 1.0
            else -> 0.8
        }
    } ?: 1.0

    return (clinicBasePrice * multiplier).roundToInt()
}

private fun collectMatchedServicesForClinic(
    clinic: Clinic,
    treatmentTeeth: List<ToothReportItem>
): List<String> {
    if (clinic.priceList.isEmpty()) {
        return emptyList()
    }

    return treatmentTeeth
        .flatMap { tooth ->
            serviceKeywordsForConditions(tooth.conditions)
                .flatMap { keyword ->
                    clinic.priceList
                        .filter { entry -> entry.service.normalizeForMatch().contains(keyword) }
                        .map { entry -> entry.service }
                }
        }
        .distinct()
        .take(4)
}

private fun priceByMatchedService(
    clinic: Clinic,
    conditions: List<String>
): Int {
    val positivePrices = clinic.priceList.filter { it.price > 0 }
    if (positivePrices.isEmpty()) return 0

    val keywords = serviceKeywordsForConditions(conditions)
    if (keywords.isEmpty()) return 0

    return positivePrices
        .filter { entry ->
            val normalized = entry.service.normalizeForMatch()
            keywords.any { keyword -> normalized.contains(keyword) }
        }
        .minOfOrNull { entry -> entry.price }
        ?: 0
}

private fun severityForConditions(conditions: List<String>): TreatmentSeverity {
    return when {
        conditions.any { it == "abscess" || it == "periapical_lesion" } -> TreatmentSeverity.URGENT
        conditions.any { it == "residual_root" || it == "impacted_tooth" } -> TreatmentSeverity.IMPORTANT
        conditions.any { it == "caries" } -> TreatmentSeverity.IMPORTANT
        conditions.any { it in RESTORATION_CONDITIONS } -> TreatmentSeverity.ATTENTION
        else -> TreatmentSeverity.NORMAL
    }
}

private fun conditionPriority(condition: String): Int {
    return when (condition) {
        "abscess" -> 100
        "periapical_lesion" -> 90
        "caries" -> 80
        "residual_root" -> 70
        "impacted_tooth" -> 60
        "root_canal_treatment" -> 50
        "crown", "endocrown", "post", "veneer" -> 40
        "implant" -> 30
        "filling" -> 20
        else -> 10
    }
}

private fun recommendedServiceForCondition(condition: String): String {
    return when (condition) {
        "abscess",
        "periapical_lesion",
        "caries",
        "root_canal_treatment",
        "filling" -> "Пломба / канал"

        "residual_root",
        "impacted_tooth" -> "Тісті жұлу"

        "implant" -> "Имплант"

        "crown",
        "endocrown",
        "post",
        "veneer" -> "Протездеу"

        else -> "Кеңес алу"
    }
}

private fun Clinic.baseTreatmentPrice(): Int {
    val minFromPriceList = priceList
        .map { it.price }
        .filter { it > 0 }
        .minOrNull()
    return minFromPriceList ?: priceFrom
}

private fun serviceKeywordsForConditions(conditions: List<String>): Set<String> {
    return conditions.flatMap { condition ->
        when (condition) {
            "abscess", "periapical_lesion" -> listOf("канал", "эндо", "пульпит", "ериапик")
            "caries", "filling", "root_canal_treatment" -> listOf("кариес", "пломб", "канал", "емдеу")
            "residual_root", "impacted_tooth" -> listOf("жулу", "удален", "хирург")
            "implant" -> listOf("имплант")
            "crown", "endocrown", "post", "veneer" -> listOf("корон", "винир", "протез", "ортопед", "реставрац")
            else -> emptyList()
        }
    }.map { it.normalizeForMatch() }.toSet()
}

private fun String.normalizeForMatch(): String {
    return lowercase()
        .replace("ё", "е")
        .replace("қ", "к")
        .replace("ғ", "г")
        .replace("ә", "а")
        .replace("ө", "о")
        .replace("ү", "у")
        .replace("ұ", "у")
        .replace("ң", "н")
}

private fun DocumentSnapshot.toClinicForAi(): Clinic {
    val services = (get(FirestoreFields.SERVICES) as? List<*>)
        ?.mapNotNull { value -> value as? String }
        ?: emptyList()
    val priceList = parsePriceListForAi(get(FirestoreFields.PRICE_LIST))
    val priceFrom = parseIntValueForAi(get(FirestoreFields.PRICE_FROM))
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

private fun parsePriceListForAi(raw: Any?): List<ServicePrice> {
    val list = raw as? List<*> ?: return emptyList()
    return list.mapNotNull { item ->
        val map = item as? Map<*, *> ?: return@mapNotNull null
        val service = (map["service"] ?: map["name"] ?: map["title"])?.toString()?.trim().orEmpty()
        val price = parseIntValueForAi(map["price"]) ?: 0
        if (service.isBlank()) null else ServicePrice(service = service, price = price)
    }
}

private fun parseIntValueForAi(value: Any?): Int? {
    return when (value) {
        is Int -> value
        is Long -> value.toInt()
        is Double -> value.toInt()
        is Float -> value.toInt()
        is String -> value.trim().toIntOrNull()
        else -> null
    }?.takeIf { it >= 0 }
}

private fun serviceAliasesForConditions(conditions: List<String>): List<String> {
    return conditions.flatMap { condition ->
        when (condition) {
            "abscess",
            "periapical_lesion",
            "caries",
            "root_canal_treatment",
            "filling" -> listOf("Пломба / Канал")

            "residual_root",
            "impacted_tooth" -> listOf("Удаление зуба")

            "implant" -> listOf("Имплант")

            "crown",
            "endocrown",
            "post",
            "veneer" -> listOf("Протезирование")

            else -> emptyList()
        }
    }
}

private fun serviceLabelForUi(service: String): String {
    return when {
        service.equals("Пломба / Канал", ignoreCase = true) -> "Пломба / канал"
        service.equals("Удаление зуба", ignoreCase = true) -> "Тісті жұлу"
        service.equals("Протезирование", ignoreCase = true) -> "Протездеу"
        service.equals("Имплант", ignoreCase = true) -> "Имплант"
        service.equals("Брекеты", ignoreCase = true) -> "Брекет"
        else -> service
    }
}

private fun conditionLabelForReport(condition: String): String {
    return when (condition) {
        "abscess" -> "Абсцесс"
        "caries" -> "Кариес"
        "filling" -> "Пломба"
        "implant" -> "Имплант"
        "crown" -> "Сауыт"
        "root_canal_treatment" -> "Канал емі"
        "post" -> "Штифт"
        "periapical_lesion" -> "Периапикалды өзгеріс"
        "residual_root" -> "Қалған түбір"
        "impacted_tooth" -> "Ретенцияланған тіс"
        "endocrown" -> "Эндосауыт"
        "veneer" -> "Винир"
        else -> condition
    }
}

private fun List<ToothReportItem>.toDisplayNumbersText(): String {
    return joinToString(", ") { "№${it.displayNumber}" }
}

private fun mapToothCodeToDisplayNumber(toothCode: String): Int {
    val clean = toothCode.trim().uppercase()
    if (clean.length < 3) return -1

    val quadrant = clean.substring(0, 2)
    val toothIdx = clean.substring(2).toIntOrNull() ?: return -1

    return when (quadrant) {
        "RU" -> 9 - toothIdx
        "LU" -> 8 + toothIdx
        "LL" -> 25 - toothIdx
        "RL" -> 24 + toothIdx
        else -> -1
    }
}

fun formatTenge(value: Int): String {
    if (value <= 0) {
        return "дерек жоқ"
    }
    return "%,d ₸".format(value).replace(',', ' ')
}
