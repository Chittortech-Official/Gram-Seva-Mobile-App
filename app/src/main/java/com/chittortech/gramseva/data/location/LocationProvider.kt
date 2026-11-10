package com.chittortech.gramseva.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import com.chittortech.gramseva.data.models.UserLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.*

object LocationProvider {

    val defaultLocation = UserLocation(
        nameHindi = "चित्तौड़गढ़",
        nameEnglish = "Chittorgarh",
        district = "Chittorgarh",
        state = "Rajasthan",
        lat = 24.8887,
        lon = 74.6269
    )

    val supportedStates = listOf(
        "सभी राज्य",
        "Rajasthan",
        "Madhya Pradesh",
        "Uttar Pradesh",
        "Gujarat",
        "Punjab",
        "Haryana",
        "Maharashtra",
        "Bihar"
    )

    val stateDisplayNameMap = mapOf(
        "सभी राज्य" to "पूरा भारत (All India)",
        "Rajasthan" to "राजस्थान",
        "Madhya Pradesh" to "मध्य प्रदेश",
        "Uttar Pradesh" to "उत्तर प्रदेश",
        "Gujarat" to "गुजरात",
        "Punjab" to "पंजाब",
        "Haryana" to "हरियाणा",
        "Maharashtra" to "महाराष्ट्र",
        "Bihar" to "बिहार"
    )

    val allLocations: List<UserLocation> = listOf(
        // Rajasthan
        UserLocation("चित्तौड़गढ़", "Chittorgarh", "Chittorgarh", "Rajasthan", 24.8887, 74.6269),
        UserLocation("निम्बाहेड़ा", "Nimbahera", "Chittorgarh", "Rajasthan", 24.6200, 74.6800),
        UserLocation("कपासन", "Kapasan", "Chittorgarh", "Rajasthan", 24.8800, 74.3200),
        UserLocation("बेंगू", "Begun", "Chittorgarh", "Rajasthan", 24.9800, 75.0200),
        UserLocation("कोटा", "Kota", "Kota", "Rajasthan", 25.1800, 75.8300),
        UserLocation("उदयपुर", "Udaipur", "Udaipur", "Rajasthan", 24.5854, 73.7125),
        UserLocation("जयपुर", "Jaipur", "Jaipur", "Rajasthan", 26.9124, 75.7873),
        UserLocation("जोधपुर", "Jodhpur", "Jodhpur", "Rajasthan", 26.2389, 73.0243),
        UserLocation("बीकानेर", "Bikaner", "Bikaner", "Rajasthan", 28.0229, 73.3119),
        UserLocation("श्रीगंगानगर", "Sri Ganganagar", "Ganganagar", "Rajasthan", 29.9038, 73.8772),

        // Madhya Pradesh
        UserLocation("नीमच", "Neemuch", "Neemuch", "Madhya Pradesh", 24.4700, 74.8700),
        UserLocation("मंदसौर", "Mandsaur", "Mandsaur", "Madhya Pradesh", 24.0722, 75.0682),
        UserLocation("रतलाम", "Ratlam", "Ratlam", "Madhya Pradesh", 23.3315, 75.0367),
        UserLocation("इंदौर", "Indore", "Indore", "Madhya Pradesh", 22.7196, 75.8577),
        UserLocation("उज्जैन", "Ujjain", "Ujjain", "Madhya Pradesh", 23.1765, 75.7885),
        UserLocation("भोपाल", "Bhopal", "Bhopal", "Madhya Pradesh", 23.2599, 77.4126),
        UserLocation("देवास", "Dewas", "Dewas", "Madhya Pradesh", 22.9676, 76.0534),
        UserLocation("जबलपुर", "Jabalpur", "Jabalpur", "Madhya Pradesh", 23.1815, 79.9864),

        // Uttar Pradesh
        UserLocation("लखनऊ", "Lucknow", "Lucknow", "Uttar Pradesh", 26.8467, 80.9462),
        UserLocation("कानपुर", "Kanpur", "Kanpur", "Uttar Pradesh", 26.4499, 80.3319),
        UserLocation("वाराणसी", "Varanasi", "Varanasi", "Uttar Pradesh", 25.3176, 82.9739),
        UserLocation("आगरा", "Agra", "Agra", "Uttar Pradesh", 27.1767, 78.0081),
        UserLocation("प्रयागराज", "Prayagraj", "Prayagraj", "Uttar Pradesh", 25.4358, 81.8463),
        UserLocation("मेरठ", "Meerut", "Meerut", "Uttar Pradesh", 28.9845, 77.7064),
        UserLocation("बांदा", "Banda", "Banda", "Uttar Pradesh", 25.4754, 80.3347),
        UserLocation("एटा", "Etah", "Etah", "Uttar Pradesh", 27.5615, 78.6651),

        // Gujarat
        UserLocation("ऊंझा", "Unjha", "Mehsana", "Gujarat", 23.8039, 72.3929),
        UserLocation("राजकोट", "Rajkot", "Rajkot", "Gujarat", 22.3039, 70.8022),
        UserLocation("अहमदाबाद", "Ahmedabad", "Ahmedabad", "Gujarat", 23.0225, 72.5714),
        UserLocation("गोंडल", "Gondal", "Rajkot", "Gujarat", 21.9619, 70.7997),
        UserLocation("सूरत", "Surat", "Surat", "Gujarat", 21.1702, 72.8311),

        // Punjab & Haryana
        UserLocation("लुधियाना", "Ludhiana", "Ludhiana", "Punjab", 30.9010, 75.8573),
        UserLocation("अमृतसर", "Amritsar", "Amritsar", "Punjab", 31.6340, 74.8723),
        UserLocation("करनाल", "Karnal", "Karnal", "Haryana", 29.6857, 76.9905),
        UserLocation("हिसar", "Hisar", "Hisar", "Haryana", 29.1492, 75.7217),
        UserLocation("सिरसा", "Sirsa", "Sirsa", "Haryana", 29.5321, 75.0318),

        // Maharashtra
        UserLocation("नासिक", "Nashik", "Nashik", "Maharashtra", 19.9975, 73.7898),
        UserLocation("पुणे", "Pune", "Pune", "Maharashtra", 18.5204, 73.8567),
        UserLocation("नागपुर", "Nagpur", "Nagpur", "Maharashtra", 21.1458, 79.0882),
        UserLocation("लातूर", "Latur", "Latur", "Maharashtra", 18.4088, 76.5604),
        UserLocation("जलगांव", "Jalgaon", "Jalgaon", "Maharashtra", 21.0077, 75.5626),

        // Bihar
        UserLocation("पटना", "Patna", "Patna", "Bihar", 25.5941, 85.1376),
        UserLocation("मुजफ्फरपुर", "Muzaffarpur", "Muzaffarpur", "Bihar", 26.1209, 85.3647),
        UserLocation("भागलपुर", "Bhagalpur", "Bhagalpur", "Bihar", 25.2425, 86.9842)
    )

    fun getLocationsForState(state: String): List<UserLocation> {
        if (state == "सभी राज्य" || state.isBlank()) return allLocations
        return allLocations.filter { it.state.equals(state, ignoreCase = true) }
    }

    /**
     * Attempts to read the last known GPS or Network location from LocationManager,
     * and reverse geocodes it to district and state.
     */
    @SuppressLint("MissingPermission")
    suspend fun detectCurrentLocation(context: Context): UserLocation? = withContext(Dispatchers.IO) {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                ?: return@withContext null

            val providers = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
            )

            var bestLocation: Location? = null
            for (provider in providers) {
                if (locationManager.isProviderEnabled(provider)) {
                    val loc = locationManager.getLastKnownLocation(provider)
                    if (loc != null) {
                        if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                            bestLocation = loc
                        }
                    }
                }
            }

            val loc = bestLocation ?: return@withContext null
            val lat = loc.latitude
            val lon = loc.longitude

            // Try reverse geocoding with Android Geocoder
            var city = ""
            var district = ""
            var state = ""

            try {
                val geocoder = Geocoder(context, Locale("hi", "IN"))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val addresses = geocoder.getFromLocation(lat, lon, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val addr = addresses[0]
                        city = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "स्थानीय क्षेत्र"
                        district = addr.subAdminArea ?: city
                        state = addr.adminArea ?: "Rajasthan"
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(lat, lon, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val addr = addresses[0]
                        city = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "स्थानीय क्षेत्र"
                        district = addr.subAdminArea ?: city
                        state = addr.adminArea ?: "Rajasthan"
                    }
                }
            } catch (e: Exception) {
                // Geocoder service may not be available on some ROMs
            }

            // If Geocoder did not yield a name, find the closest city from our national directory
            if (city.isBlank() || district.isBlank()) {
                val nearest = findNearestLocation(lat, lon)
                return@withContext nearest.copy(
                    lat = lat,
                    lon = lon,
                    isGpsDetected = true
                )
            }

            UserLocation(
                nameHindi = city,
                nameEnglish = district,
                district = district,
                state = state,
                lat = lat,
                lon = lon,
                isGpsDetected = true
            )
        } catch (e: Exception) {
            null
        }
    }

    fun findNearestLocation(lat: Double, lon: Double): UserLocation {
        var minDistance = Double.MAX_VALUE
        var nearest = defaultLocation

        for (loc in allLocations) {
            val dist = calculateDistanceKm(lat, lon, loc.lat, loc.lon)
            if (dist < minDistance) {
                minDistance = dist
                nearest = loc
            }
        }
        return nearest
    }

    private fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}

