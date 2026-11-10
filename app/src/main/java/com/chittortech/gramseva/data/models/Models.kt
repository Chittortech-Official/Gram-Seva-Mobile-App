package com.chittortech.gramseva.data.models

import kotlinx.serialization.Serializable

@Serializable
data class UserLocation(
    val nameHindi: String,
    val nameEnglish: String,
    val district: String,
    val state: String,
    val lat: Double,
    val lon: Double,
    val isGpsDetected: Boolean = false
)

@Serializable
data class MandiItem(
    val id: String,
    val commodity: String,       // e.g. "लहसुन (Garlic)"
    val market: String,          // e.g. "Chittorgarh Mandi"
    val district: String,        // e.g. "Chittorgarh"
    val state: String = "Rajasthan",
    val variety: String = "Desi",
    val grade: String = "FAQ",
    val minPrice: Int,           // in ₹/Quintal
    val maxPrice: Int,
    val modalPrice: Int,
    val arrivalDate: String,
    val trend: String = "स्थिर",  // "तेज (Bullish)", "मंदा (Bearish)", "स्थिर (Stable)"
    val isHotCrop: Boolean = false,
    val arrivalsTons: Double? = null
)

@Serializable
data class DailyForecast(
    val date: String,
    val dayName: String,
    val maxTemp: Double,
    val minTemp: Double,
    val rainProbability: Int,
    val condition: String,
    val weatherCode: Int = 0
)

@Serializable
data class WeatherInfo(
    val city: String = "Chittorgarh",
    val district: String = "Chittorgarh",
    val temperature: Double,
    val condition: String,
    val rainProbability: Int,
    val windSpeed: Double,
    val humidity: Int,
    val farmerAdvisory: String,
    val dailyForecast: List<DailyForecast> = emptyList(),
    val feelsLike: Double = 29.0
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
    val category: String,        // "किसान", "सब्सिडी", "महिला", "बीमा"
    val eligibility: String,
    val documentsRequired: List<String>,
    val portalName: String = "राजकिसान साथी पोर्टल / e-Mitra",
    val helpline: String = "181 / 1800-180-1551"
)


