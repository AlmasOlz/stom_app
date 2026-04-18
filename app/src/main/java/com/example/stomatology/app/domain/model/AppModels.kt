package com.example.stomatology.app.domain.model

data class Clinic(
    val id: String = "",
    val name: String = "",
    val rating: Double = 0.0,
    val reviews: Int = 0,
    val address: String = "",
    val services: List<String> = emptyList(),
    val imageUrl: String = ""
)

// --- УАҚЫТША AI МОДЕЛЬДЕРІ ---
data class AiAnalysisResult(
    val teethCount: Int,
    val findings: List<Finding>
)

data class Finding(
    val toothClass: String, // 'RU1', 'LL3' сияқты
    val conditions: List<String> // ['Caries', 'Filling'] сияқты аурулар тізімі
)

// Тістерге арналған статус кластары (Ui үшін керек)
enum class ToothStatus {
    HEALTHY, CARIES, PROSTHESIS, EXTRACTED
}

data class ToothState(
    val number: Int,
    val status: ToothStatus = ToothStatus.HEALTHY
)