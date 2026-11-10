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

    // Complete coverage of all 11 Tehsils in Chittorgarh District
    val chittorgarhTehsils: List<UserLocation> = listOf(
        UserLocation("चित्तौड़गढ़", "Chittorgarh", "Chittorgarh", "Rajasthan", 24.8887, 74.6269),
        UserLocation("निम्बाहेड़ा", "Nimbahera", "Chittorgarh", "Rajasthan", 24.6200, 74.6800),
        UserLocation("कपासन", "Kapasan", "Chittorgarh", "Rajasthan", 24.8800, 74.3200),
        UserLocation("बेगूं", "Begun", "Chittorgarh", "Rajasthan", 24.9800, 75.0200),
        UserLocation("बड़ी सादड़ी", "Bari Sadri", "Chittorgarh", "Rajasthan", 24.4200, 74.4700),
        UserLocation("गंगरार", "Gangrar", "Chittorgarh", "Rajasthan", 25.0500, 74.6100),
        UserLocation("राशमी", "Rashmi", "Chittorgarh", "Rajasthan", 25.0600, 74.3500),
        UserLocation("भदेसर", "Bhadesar", "Chittorgarh", "Rajasthan", 24.7100, 74.5200),
        UserLocation("डूंगला", "Dungla", "Chittorgarh", "Rajasthan", 24.5100, 74.3300),
        UserLocation("भूपालसागर", "Bhupalsagar", "Chittorgarh", "Rajasthan", 24.8900, 74.1900),
        UserLocation("रावतभाटा", "Rawatbhata", "Chittorgarh", "Rajasthan", 24.9300, 75.5900)
    )

    val supportedStates = listOf(
        "Rajasthan",
        "Madhya Pradesh"
    )

    val stateDisplayNameMap = mapOf(
        "Rajasthan" to "राजस्थान",
        "Madhya Pradesh" to "मध्य प्रदेश"
    )

    val allLocations: List<UserLocation> = chittorgarhTehsils + listOf(
        // Border & Major Trade Hubs
        UserLocation("नीमच", "Neemuch", "Neemuch", "Madhya Pradesh", 24.4700, 74.8700),
        UserLocation("मंदसौर", "Mandsaur", "Mandsaur", "Madhya Pradesh", 24.0722, 75.0682),
        UserLocation("कोटा", "Kota", "Kota", "Rajasthan", 25.1800, 75.8300),
        UserLocation("उदयपुर", "Udaipur", "Udaipur", "Rajasthan", 24.5854, 73.7125),
        UserLocation("भीलवाड़ा", "Bhilwara", "Bhilwara", "Rajasthan", 25.3407, 74.6313)
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

