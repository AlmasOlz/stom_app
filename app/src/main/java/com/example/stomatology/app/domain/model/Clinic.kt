package com.example.stomatology.app.domain.model

// ─── Clinic ───────────────────────────────────────────────────────────────────

data class Clinic(
    val id: String = "",
    val name: String = "",
    val rating: Double = 0.0,
    val reviews: Int = 0,
    val address: String = "",
    val services: List<String> = emptyList(),
    val imageUrl: String = "",
    val priceFrom: Int = 0,
    val priceList: List<ServicePrice> = emptyList(),
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

data class ServicePrice(
    val service: String = "",
    val price: Int = 0
)

// ─── AI Analysis ──────────────────────────────────────────────────────────────

/**
 * Structural state of a tooth slot returned by the backend.
 *
 * • DETECTED — the tooth was seen in the X-ray by the detection model.
 * • UNKNOWN  — the tooth position was not visible / could not be classified;
 *              must NOT be treated as missing.
 * • MISSING  — the tooth is explicitly confirmed absent (extracted / never erupted).
 */
enum class ToothDetectionState {
    DETECTED,
    UNKNOWN,
    MISSING;

    companion object {
        fun fromString(value: String?): ToothDetectionState = when (value?.lowercase()) {
            "detected" -> DETECTED
            "missing"  -> MISSING
            else       -> UNKNOWN
        }
    }
}

/**
 * A single tooth entry in the AI analysis result.
 *
 * @param toothClass  FDI-like quadrant code, e.g. "RU1", "LL8"
 * @param state       structural detection state of this tooth slot
 * @param conditions  list of normalized dental conditions, e.g. ["caries", "filling"]
 */
data class Finding(
    val toothClass: String,
    val state: ToothDetectionState,
    val conditions: List<String>
)

data class AiAnalysisResult(
    val teethCount: Int,
    val findings: List<Finding>
)

// ─── Legacy tooth status (kept for backward compatibility) ────────────────────

enum class ToothStatus {
    HEALTHY, CARIES, PROSTHESIS, EXTRACTED
}

data class ToothState(
    val number: Int,
    val status: ToothStatus = ToothStatus.HEALTHY
)
