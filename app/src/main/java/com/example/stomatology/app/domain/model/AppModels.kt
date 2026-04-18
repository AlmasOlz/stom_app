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

data class AiAnalysisResult(
    val teethCount: Int,
    val findings: List<Finding>
)

data class Finding(
    val toothClass: String,
    val confidence: Double
)