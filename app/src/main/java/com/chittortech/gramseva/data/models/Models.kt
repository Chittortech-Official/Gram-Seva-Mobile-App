package com.chittortech.gramseva.data.models

import kotlinx.serialization.Serializable

@Serializable
data class MandiItem(
    val id: String,
    val commodity: String,       // e.g. "लहसुन (Garlic)"
    val market: String,          // e.g. "Chittorgarh Mandi"
    val district: String,        // e.g. "Chittorgarh"
    val state: String = "Rajasthan",
    val variety: String = "Desi",
    val minPrice: Int,           // in ₹/Quintal
    val maxPrice: Int,
    val modalPrice: Int,
    val arrivalDate: String,
    val trend: String = "स्थिर",  // "तेज (Bullish)", "मंदा (Bearish)", "स्थिर (Stable)"
    val isHotCrop: Boolean = false
)

@Serializable
data class WeatherInfo(
    val city: String = "Chittorgarh",
    val temperature: Double,
    val condition: String,
    val rainProbability: Int,
    val windSpeed: Double,
    val humidity: Int,
    val farmerAdvisory: String
)

@Serializable
data class PincodeRecord(
    val pincode: String,
    val postOfficeName: String,
    val district: String,
    val state: String,
    val division: String,
    val deliveryStatus: String = "Delivery"
)

@Serializable
data class YojanaItem(
    val id: String,
    val titleHindi: String,
    val titleEnglish: String,
    val benefitAmount: String,
    val category: String,        // "किसान", "महिला", "छात्र", "व्यापारी"
    val eligibility: String,
    val documentsRequired: List<String>
)
