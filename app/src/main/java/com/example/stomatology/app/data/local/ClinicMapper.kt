package com.example.stomatology.app.data.local

import com.example.stomatology.app.domain.model.Clinic

fun ClinicEntity.toDomain(): Clinic {
    return Clinic(
        id = id,
        name = name,
        rating = rating,
        reviews = reviews,
        address = address,
        services = if (services.isBlank()) emptyList() else services.split("|"),
        imageUrl = imageUrl,
        priceFrom = priceFrom,
        description = description,
        latitude = latitude,
        longitude = longitude
    )
}
