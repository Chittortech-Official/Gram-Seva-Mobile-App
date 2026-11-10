package com.chittortech.gramseva.data.repository

import com.chittortech.gramseva.data.models.*
import com.chittortech.gramseva.util.HindiTranslator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GramSevaRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    data class LocationCoord(
        val nameHindi: String,
        val lat: Double,
        val lon: Double,
        val district: String
    )

    val chittorgarhTehsils: List<LocationCoord>
        get() = com.chittortech.gramseva.data.location.LocationProvider.chittorgarhTehsils.map {
            LocationCoord("${it.nameHindi} (तहसील)", it.lat, it.lon, it.district)
        }

    val supportedLocations: List<LocationCoord>
        get() = com.chittortech.gramseva.data.location.LocationProvider.allLocations.map {
            LocationCoord("${it.nameHindi} (${it.nameEnglish})", it.lat, it.lon, it.district)
        }

    // ─── 1. Live Weather & 7-Day Forecast from Open-Meteo ───────────────────────
    suspend fun fetchWeather(
        lat: Double = 24.8887,
        lon: Double = 74.6269,
        cityName: String = "चित्तौड़गढ़"
    ): WeatherInfo = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max,wind_speed_10m_max&timezone=Asia%2FKolkata"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: "{}"
            val json = JSONObject(body)

            val current = json.optJSONObject("current")
            val temp = current?.optDouble("temperature_2m", 26.0) ?: 26.0
            val humidity = current?.optInt("relative_humidity_2m", 35) ?: 35
            val feelsLike = current?.optDouble("apparent_temperature", temp + 0.5) ?: (temp + 0.5)
            val wind = current?.optDouble("wind_speed_10m", 3.8) ?: 3.8
            val weatherCode = current?.optInt("weather_code", 0) ?: 0

            val condition = getWeatherCondition(weatherCode)
            val advisory = generateFarmerAdvisory(weatherCode, temp)

            val dailyJson = json.optJSONObject("daily")
            val dailyForecasts = mutableListOf<DailyForecast>()

            if (dailyJson != null) {
                val times = dailyJson.optJSONArray("time") ?: JSONArray()
                val codes = dailyJson.optJSONArray("weather_code") ?: JSONArray()
                val maxTemps = dailyJson.optJSONArray("temperature_2m_max") ?: JSONArray()
                val minTemps = dailyJson.optJSONArray("temperature_2m_min") ?: JSONArray()
                val rainProbs = dailyJson.optJSONArray("precipitation_probability_max") ?: JSONArray()

                val dayNamesHindi = mapOf(
                    java.util.Calendar.SUNDAY to "रविवार",
                    java.util.Calendar.MONDAY to "सोमवार",
                    java.util.Calendar.TUESDAY to "मंगलवार",
                    java.util.Calendar.WEDNESDAY to "बुधवार",
                    java.util.Calendar.THURSDAY to "गुरुवार",
                    java.util.Calendar.FRIDAY to "शुक्रवार",
                    java.util.Calendar.SATURDAY to "शनिवार"
                )
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                val cal = java.util.Calendar.getInstance()

                for (i in 0 until times.length().coerceAtMost(7)) {
                    val code = codes.optInt(i, 0)
                    val maxT = maxTemps.optDouble(i, temp + 1.2)
                    val minT = minTemps.optDouble(i, temp - 13.5)
                    val rainP = rainProbs.optInt(i, 0)
                    val dateStr = times.optString(i)

                    val label = when (i) {
                        0 -> "आज"
                        1 -> "कल"
                        else -> {
                            try {
                                val d = sdf.parse(dateStr)
                                if (d != null) {
                                    cal.time = d
                                    dayNamesHindi[cal.get(java.util.Calendar.DAY_OF_WEEK)] ?: "${i + 1}वें दिन"
                                } else "${i + 1}वें दिन"
                            } catch (e: Exception) {
                                "${i + 1}वें दिन"
                            }
                        }
                    }

                    dailyForecasts.add(
                        DailyForecast(
                            date = dateStr,
                            dayName = label,
                            maxTemp = maxT,
                            minTemp = minT,
                            rainProbability = rainP,
                            condition = getWeatherCondition(code),
                            weatherCode = code
                        )
                    )
                }
            }

            if (dailyForecasts.isEmpty()) {
                dailyForecasts.addAll(getFallbackDailyForecast(temp))
            }

            val rainProb = dailyForecasts.firstOrNull()?.rainProbability ?: if (weatherCode in listOf(51, 53, 61, 63, 65)) 85 else 0

            WeatherInfo(
                city = cityName,
                district = cityName,
                temperature = temp,
                condition = condition,
                rainProbability = rainProb,
                windSpeed = wind,
                humidity = humidity,
                farmerAdvisory = advisory,
                dailyForecast = dailyForecasts,
                feelsLike = feelsLike
            )
        } catch (e: Exception) {
            val fallbackTemp = 26.0
            WeatherInfo(
                city = cityName,
                district = "Chittorgarh",
                temperature = fallbackTemp,
                condition = "साफ धूप (Clear Sky)",
                rainProbability = 0,
                windSpeed = 3.5,
                humidity = 35,
                farmerAdvisory = "✅ मौसम अनुकूल: रबी फसलों की बुवाई, सिंचाई व मंडी परिवहन के लिए उपयुक्त मौसम।",
                dailyForecast = getFallbackDailyForecast(fallbackTemp),
                feelsLike = fallbackTemp + 0.5
            )
        }
    }

    private fun getWeatherCondition(code: Int): String = when (code) {
        0 -> "साफ धूप"
        1, 2, 3 -> "हल्के बादल"
        45, 48 -> "धुंध व कोहरा"
        51, 53, 55 -> "हल्की बूंदाबांदी"
        61, 63, 65 -> "बारिश"
        71, 73, 75 -> "शीतलहर / ओलावृष्टि"
        80, 81, 82 -> "तेज बौछारें"
        95, 96, 99 -> "गरज-चमक के साथ बारिश"
        else -> "मौसम सामान्य"
    }

    private fun generateFarmerAdvisory(code: Int, temp: Double): String = when {
        code in listOf(61, 63, 65, 80, 81, 82, 95, 96, 99) ->
            "🌧️ बारिश की चेतावनी: कटी हुई फसल, लहसुन व उपज को तुरंत तिरपाल या गोदाम में सुरक्षित रखें। जलभराव से बचें।"
        code in listOf(51, 53, 55) ->
            "🌦️ बूंदाबांदी संभव: कीटनाशक या यूरिया का छिड़काव आज टालें ताकि दवा पानी में न बहे।"
        temp > 36.0 ->
            "☀️ तेज धूप व गर्मी: फसलों में शाम के समय हल्की सिंचाई करें ताकि मिट्टी की नमी सुरक्षित रहे।"
        temp < 15.0 ->
            "❄️ शीतलहर की संभावना: पाला (Frost) से बचाव हेतु शाम को खेत की मेड़ों पर हल्का धुआं करें।"
        else ->
            "✅ मौसम अनुकूल: मंडियों में आवाजाही, फसल कटाई व नई बुवाई के लिए आज का दिन अत्यंत उत्तम है।"
    }

    private fun getFallbackDailyForecast(baseTemp: Double): List<DailyForecast> {
        val days = listOf("आज", "कल", "बुधवार", "गुरुवार", "शुक्रवार", "शनिवार", "रविवार")
        val conditions = listOf(
            "साफ धूप", "हल्के बादल", "साफ धूप",
            "हल्की हवा", "साफ मौसम", "धूप खिली", "साफ आसमान"
        )
        return days.mapIndexed { idx, day ->
            DailyForecast(
                date = "2026-11-${String.format("%02d", 10 + idx)}",
                dayName = day,
                maxTemp = baseTemp + (idx % 3) - 1,
                minTemp = baseTemp - 11.0 + (idx % 2),
                rainProbability = if (idx == 1) 35 else 0,
                condition = conditions[idx % conditions.size],
                weatherCode = if (idx == 1) 2 else 0
            )
        }
    }

    // ─── 2. India Post Real-Time Pincode Directory ──────────────────────────────
    suspend fun fetchPincodeDetails(pincode: String): List<PincodeRecord> = withContext(Dispatchers.IO) {
        val cleanPin = pincode.trim()
        if (cleanPin.length != 6) return@withContext emptyList()

        try {
            val url = "https://api.postalpincode.in/pincode/$cleanPin"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: "[]"
            val array = JSONArray(body)
            if (array.length() == 0) return@withContext getOfflinePincodeFallback(cleanPin)

            val rootObj = array.getJSONObject(0)
            if (rootObj.optString("Status") != "Success") return@withContext getOfflinePincodeFallback(cleanPin)

            val postOffices = rootObj.optJSONArray("PostOffice") ?: return@withContext getOfflinePincodeFallback(cleanPin)
            val list = mutableListOf<PincodeRecord>()

            for (i in 0 until postOffices.length()) {
                val po = postOffices.getJSONObject(i)
                val rawStatus = po.optString("DeliveryStatus", "Delivery")
                val cleanStatus = if (rawStatus.equals("Delivery", ignoreCase = true)) "वितरण उपलब्ध" else "वितरण रहित"
                list.add(
                    PincodeRecord(
                        pincode = cleanPin,
                        postOfficeName = po.optString("Name"),
                        district = po.optString("District"),
                        state = po.optString("State"),
                        division = po.optString("Division"),
                        deliveryStatus = cleanStatus
                    )
                )
            }
            if (list.isEmpty()) getOfflinePincodeFallback(cleanPin) else list
        } catch (e: Exception) {
            getOfflinePincodeFallback(cleanPin)
        }
    }

    private fun getOfflinePincodeFallback(pin: String): List<PincodeRecord> {
        val master = mapOf(
            "312001" to listOf(
                PincodeRecord("312001", "Chittorgarh H.O", "Chittorgarh", "Rajasthan", "Chittorgarh Division", "वितरण उपलब्ध"),
                PincodeRecord("312001", "Collectorate Chittorgarh S.O", "Chittorgarh", "Rajasthan", "Chittorgarh Division", "वितरण उपलब्ध"),
                PincodeRecord("312001", "Senthi B.O", "Chittorgarh", "Rajasthan", "Chittorgarh Division", "वितरण उपलब्ध"),
                PincodeRecord("312001", "Chanderiya S.O", "Chittorgarh", "Rajasthan", "Chittorgarh Division", "वितरण उपलब्ध")
            ),
            "312601" to listOf(
                PincodeRecord("312601", "Nimbahera H.O", "Chittorgarh", "Rajasthan", "Chittorgarh Division", "वितरण उपलब्ध"),
                PincodeRecord("312601", "Mandi Yard Nimbahera S.O", "Chittorgarh", "Rajasthan", "Chittorgarh Division", "वितरण उपलब्ध"),
                PincodeRecord("312601", "Kailash Nagar S.O", "Chittorgarh", "Rajasthan", "Chittorgarh Division", "वितरण उपलब्ध")
            ),
            "312202" to listOf(
                PincodeRecord("312202", "Kapasan S.O", "Chittorgarh", "Rajasthan", "Chittorgarh Division", "वितरण उपलब्ध"),
                PincodeRecord("312202", "Dhamana B.O", "Chittorgarh", "Rajasthan", "Chittorgarh Division", "वितरण उपलब्ध")
            ),
            "312023" to listOf(
                PincodeRecord("312023", "Begun S.O", "Chittorgarh", "Rajasthan", "Chittorgarh Division", "वितरण उपलब्ध"),
                PincodeRecord("312023", "Chechi B.O", "Chittorgarh", "Rajasthan", "Chittorgarh Division", "वितरण उपलब्ध")
            ),
            "458441" to listOf(
                PincodeRecord("458441", "Neemuch H.O", "Neemuch", "Madhya Pradesh", "Mandsaur Division", "वितरण उपलब्ध"),
                PincodeRecord("458441", "Krishi Upaj Mandi Neemuch S.O", "Neemuch", "Madhya Pradesh", "Mandsaur Division", "वितरण उपलब्ध")
            ),
            "324001" to listOf(
                PincodeRecord("324001", "Kota H.O", "Kota", "Rajasthan", "Kota Division", "वितरण उपलब्ध"),
                PincodeRecord("324001", "Bhamashah Mandi Kota S.O", "Kota", "Rajasthan", "Kota Division", "वितरण उपलब्ध")
            )
        )
        return master[pin] ?: emptyList()
    }

    companion object {
        const val CLOUDFLARE_EDGE_URL = "https://gramseva-api.chittortech.workers.dev"
        const val OGD_API_KEY = "579b464db66ec23bdd0000015e0681c20b34487b4f20fc7ff279002e"
    }

    // ─── 3. Live Agmarknet Mandi API via High-Performance Cloudflare Edge ───
    suspend fun fetchLiveMandiPrices(
        state: String? = null,
        apiKey: String = OGD_API_KEY
    ): List<MandiItem> = withContext(Dispatchers.IO) {
        val fallback = getMandiPrices(state)
        try {
            val stateParam = if (!state.isNullOrBlank() && state != "सभी राज्य" && state != "All") {
                "?state=" + java.net.URLEncoder.encode(state, "UTF-8")
            } else ""

            // Primary: Cloudflare Edge Cache (Super-fast ~20ms, cached for 30 mins)
            val primaryUrl = if (stateParam.isNotBlank()) "$CLOUDFLARE_EDGE_URL$stateParam" else "$CLOUDFLARE_EDGE_URL/"
            var request = Request.Builder().url(primaryUrl).build()
            var response = client.newCall(request).execute()

            // Failover to direct Government API if edge worker is unreachable
            if (!response.isSuccessful && apiKey.isNotBlank()) {
                val stateFilter = if (!state.isNullOrBlank() && state != "सभी राज्य" && state != "All") {
                    "&filters%5Bstate%5D=" + java.net.URLEncoder.encode(state, "UTF-8")
                } else ""
                val directUrl = "https://api.data.gov.in/resource/9ef84268-d588-465a-a308-a864a43d0070?api-key=$apiKey&format=json&limit=100$stateFilter"
                request = Request.Builder().url(directUrl).build()
                response = client.newCall(request).execute()
            }

            if (!response.isSuccessful) return@withContext fallback

            val body = response.body?.string() ?: "{}"
            val json = JSONObject(body)
            val records = json.optJSONArray("records") ?: return@withContext fallback
            if (records.length() == 0) return@withContext fallback

            val liveList = mutableListOf<MandiItem>()
            for (i in 0 until records.length()) {
                val r = records.getJSONObject(i)
                val rawComm = r.optString("commodity", "").trim()
                val mkt = r.optString("market", "मंडी").trim()
                val dist = r.optString("district", "India").trim()
                val recState = r.optString("state", state ?: "India").trim()
                val variety = r.optString("variety", "General").trim()
                val modal = r.optString("modal_price", "0").toDoubleOrNull()?.toInt() ?: 0
                val minP = r.optString("min_price", "0").toDoubleOrNull()?.toInt() ?: (modal * 0.92).toInt()
                val maxP = r.optString("max_price", "0").toDoubleOrNull()?.toInt() ?: (modal * 1.08).toInt()
                val arrival = r.optString("arrival_date", "आज").trim()

                if (modal <= 0) continue

                val rawGrade = r.optString("grade", "FAQ").trim()
                val translatedComm = HindiTranslator.translateCommodity(rawComm)
                val translatedMarket = HindiTranslator.translateMarket(mkt)
                val translatedDist = HindiTranslator.translateDistrict(dist)
                val translatedState = HindiTranslator.translateState(recState)
                val translatedVariety = HindiTranslator.translateVariety(variety)
                val translatedGrade = HindiTranslator.translateGrade(rawGrade)
                val trend = if (modal > 6000) "तेज (लाइव)" else "स्थिर (लाइव)"

                liveList.add(
                    MandiItem(
                        id = "ogd_${i}_${dist}_${mkt}",
                        commodity = translatedComm,
                        market = translatedMarket,
                        district = translatedDist,
                        state = translatedState,
                        variety = translatedVariety,
                        grade = translatedGrade,
                        minPrice = minP,
                        maxPrice = maxP,
                        modalPrice = modal,
                        arrivalDate = arrival,
                        trend = trend,
                        isHotCrop = modal > 10000,
                        arrivalsTons = null
                    )
                )
            }

            if (liveList.isNotEmpty()) {
                liveList
            } else {
                fallback
            }
        } catch (e: Exception) {
            fallback
        }
    }

    // ─── 4. Full Mandi Rates Dataset (All-India Inter-State Hub) ───────────
    fun getMandiPrices(stateFilter: String? = null): List<MandiItem> {
        val list = listOf(
            // Garlic (लहसुन) - Golden Cash Crop of Mewar-Malwa
            MandiItem(
                id = "mandi_garlic_chittor",
                commodity = "लहसुन (Garlic)",
                market = "चित्तौड़गढ़ मंडी",
                district = "Chittorgarh",
                variety = "देसी बॉक्स",
                minPrice = 11500,
                maxPrice = 18400,
                modalPrice = 15200,
                arrivalDate = "आज (05 Nov)",
                trend = "तेज (+₹400)",
                isHotCrop = true,
                arrivalsTons = 62.0
            ),
            MandiItem(
                id = "mandi_garlic_nimbahera",
                commodity = "लहसुन (Garlic)",
                market = "निम्बाहेड़ा मंडी",
                district = "Chittorgarh",
                variety = "देसी सुपर",
                minPrice = 12000,
                maxPrice = 19100,
                modalPrice = 15800,
                arrivalDate = "आज (05 Nov)",
                trend = "तेज (+₹350)",
                isHotCrop = true,
                arrivalsTons = 85.0
            ),
            MandiItem(
                id = "mandi_garlic_neemuch",
                commodity = "लहसुन (Garlic)",
                market = "नीमच मंडी (MP)",
                district = "Neemuch",
                state = "Madhya Pradesh",
                variety = "रियावन / ऊटी स्पेशल",
                minPrice = 13200,
                maxPrice = 21500,
                modalPrice = 17400,
                arrivalDate = "आज (05 Nov)",
                trend = "तेज (+₹600)",
                isHotCrop = true,
                arrivalsTons = 190.0
            ),
            MandiItem(
                id = "mandi_garlic_kota",
                commodity = "लहसुन (Garlic)",
                market = "कोटा भामाशाह मंडी",
                district = "Kota",
                variety = "देसी लड्डू",
                minPrice = 10800,
                maxPrice = 17500,
                modalPrice = 14600,
                arrivalDate = "आज (05 Nov)",
                trend = "स्थिर",
                isHotCrop = false,
                arrivalsTons = 40.0
            ),

            // Onion (प्याज)
            MandiItem(
                id = "mandi_onion_chittor",
                commodity = "प्याज (Onion)",
                market = "चित्तौड़गढ़ मंडी",
                district = "Chittorgarh",
                variety = "लाल नासिक",
                minPrice = 1800,
                maxPrice = 2450,
                modalPrice = 2150,
                arrivalDate = "आज (05 Nov)",
                trend = "तेज (+₹150)",
                isHotCrop = true,
                arrivalsTons = 35.0
            ),
            MandiItem(
                id = "mandi_onion_nimbahera",
                commodity = "प्याज (Onion)",
                market = "निम्बाहेड़ा मंडी",
                district = "Chittorgarh",
                variety = "लाल मीडियम",
                minPrice = 1750,
                maxPrice = 2380,
                modalPrice = 2080,
                arrivalDate = "आज (05 Nov)",
                trend = "स्थिर",
                isHotCrop = false,
                arrivalsTons = 28.0
            ),
            MandiItem(
                id = "mandi_onion_neemuch",
                commodity = "प्याज (Onion)",
                market = "नीमच मंडी (MP)",
                district = "Neemuch",
                state = "Madhya Pradesh",
                variety = "सुपर लाल",
                minPrice = 1900,
                maxPrice = 2600,
                modalPrice = 2280,
                arrivalDate = "आज (05 Nov)",
                trend = "तेज (+₹100)",
                isHotCrop = false,
                arrivalsTons = 70.0
            ),

            // Wheat (गेहूं)
            MandiItem(
                id = "mandi_wheat_nimbahera",
                commodity = "गेहूं (Wheat)",
                market = "निम्बाहेड़ा मंडी",
                district = "Chittorgarh",
                variety = "लोकवन (Sharbati)",
                minPrice = 2500,
                maxPrice = 3150,
                modalPrice = 2850,
                arrivalDate = "आज (05 Nov)",
                trend = "स्थिर",
                isHotCrop = false,
                arrivalsTons = 120.0
            ),
            MandiItem(
                id = "mandi_wheat_chittor",
                commodity = "गेहूं (Wheat)",
                market = "चित्तौड़गढ़ मंडी",
                district = "Chittorgarh",
                variety = "टुकड़ी शरबती",
                minPrice = 2480,
                maxPrice = 3050,
                modalPrice = 2800,
                arrivalDate = "आज (05 Nov)",
                trend = "स्थिर",
                isHotCrop = false,
                arrivalsTons = 90.0
            ),
            MandiItem(
                id = "mandi_wheat_kota",
                commodity = "गेहूं (Wheat)",
                market = "कोटा भामाशाह मंडी",
                district = "Kota",
                variety = "मिल क्वालिटी",
                minPrice = 2550,
                maxPrice = 3200,
                modalPrice = 2920,
                arrivalDate = "आज (05 Nov)",
                trend = "तेज (+₹40)",
                isHotCrop = false,
                arrivalsTons = 350.0
            ),

            // Mustard (सरसों)
            MandiItem(
                id = "mandi_mustard_pratapgarh",
                commodity = "सरसों (Mustard)",
                market = "प्रतापगढ़ मंडी",
                district = "Pratapgarh",
                variety = "काली 42% तेल",
                minPrice = 5200,
                maxPrice = 5950,
                modalPrice = 5620,
                arrivalDate = "आज (05 Nov)",
                trend = "तेज (+₹80)",
                isHotCrop = true,
                arrivalsTons = 48.0
            ),
            MandiItem(
                id = "mandi_mustard_kota",
                commodity = "सरसों (Mustard)",
                market = "कोटा भामाशाह मंडी",
                district = "Kota",
                variety = "कंडीशन 42% लैब",
                minPrice = 5350,
                maxPrice = 6120,
                modalPrice = 5780,
                arrivalDate = "आज (05 Nov)",
                trend = "तेज (+₹110)",
                isHotCrop = true,
                arrivalsTons = 140.0
            ),
            MandiItem(
                id = "mandi_mustard_chittor",
                commodity = "सरसों (Mustard)",
                market = "चित्तौड़गढ़ मंडी",
                district = "Chittorgarh",
                variety = "पीली / काली देसी",
                minPrice = 5100,
                maxPrice = 5850,
                modalPrice = 5540,
                arrivalDate = "आज (05 Nov)",
                trend = "स्थिर",
                isHotCrop = false,
                arrivalsTons = 32.0
            ),

            // Soybean (सोयाबीन)
            MandiItem(
                id = "mandi_soybean_kota",
                commodity = "सोयाबीन (Soybean)",
                market = "कोटा भामाशाह मंडी",
                district = "Kota",
                variety = "पीला बेस्ट",
                minPrice = 4100,
                maxPrice = 4780,
                modalPrice = 4480,
                arrivalDate = "आज (05 Nov)",
                trend = "मंदा (-₹70)",
                isHotCrop = false,
                arrivalsTons = 220.0
            ),
            MandiItem(
                id = "mandi_soybean_neemuch",
                commodity = "सोयाबीन (Soybean)",
                market = "नीमच मंडी (MP)",
                district = "Neemuch",
                state = "Madhya Pradesh",
                variety = "येलो गोल्ड",
                minPrice = 4150,
                maxPrice = 4850,
                modalPrice = 4520,
                arrivalDate = "आज (05 Nov)",
                trend = "स्थिर",
                isHotCrop = false,
                arrivalsTons = 180.0
            ),
            MandiItem(
                id = "mandi_soybean_chittor",
                commodity = "सोयाबीन (Soybean)",
                market = "चित्तौड़गढ़ मंडी",
                district = "Chittorgarh",
                variety = "देसी पीला",
                minPrice = 4050,
                maxPrice = 4680,
                modalPrice = 4410,
                arrivalDate = "आज (05 Nov)",
                trend = "मंदा (-₹40)",
                isHotCrop = false,
                arrivalsTons = 45.0
            ),

            // Maize (मक्का)
            MandiItem(
                id = "mandi_maize_kapasan",
                commodity = "मक्का (Maize)",
                market = "कपासन मंडी",
                district = "Chittorgarh",
                variety = "हाइब्रिड पीला",
                minPrice = 1950,
                maxPrice = 2380,
                modalPrice = 2210,
                arrivalDate = "आज (05 Nov)",
                trend = "स्थिर",
                isHotCrop = false,
                arrivalsTons = 75.0
            ),
            MandiItem(
                id = "mandi_maize_chittor",
                commodity = "मक्का (Maize)",
                market = "चित्तौड़गढ़ मंडी",
                district = "Chittorgarh",
                variety = "देसी सफेद व पीला",
                minPrice = 1900,
                maxPrice = 2320,
                modalPrice = 2180,
                arrivalDate = "आज (05 Nov)",
                trend = "स्थिर",
                isHotCrop = false,
                arrivalsTons = 50.0
            ),

            // Gram / Chana (चना)
            MandiItem(
                id = "mandi_chana_nimbahera",
                commodity = "चना (Gram)",
                market = "निम्बाहेड़ा मंडी",
                district = "Chittorgarh",
                variety = "विशाल / काबुली",
                minPrice = 6200,
                maxPrice = 7100,
                modalPrice = 6650,
                arrivalDate = "आज (05 Nov)",
                trend = "तेज (+₹90)",
                isHotCrop = true,
                arrivalsTons = 38.0
            ),
            MandiItem(
                id = "mandi_chana_neemuch",
                commodity = "चना (Gram)",
                market = "नीमच मंडी (MP)",
                district = "Neemuch",
                state = "Madhya Pradesh",
                variety = "डॉलर चना",
                minPrice = 8500,
                maxPrice = 11800,
                modalPrice = 10400,
                arrivalDate = "आज (05 Nov)",
                trend = "तेज (+₹250)",
                isHotCrop = true,
                arrivalsTons = 60.0
            ),

            // Isabgol & Methi (स्पेशल मेवाड़-मालवा मसाला)
            MandiItem(
                id = "mandi_isabgol_neemuch",
                commodity = "इसबगोल (Psyllium)",
                market = "नीमच मंडी (MP)",
                district = "Neemuch",
                state = "Madhya Pradesh",
                variety = "पैकेट क्वालिटी",
                minPrice = 14500,
                maxPrice = 18200,
                modalPrice = 16800,
                arrivalDate = "आज (05 Nov)",
                trend = "तेज (+₹300)",
                isHotCrop = true,
                arrivalsTons = 30.0
            ),
            MandiItem(
                id = "mandi_methi_chittor",
                commodity = "मेथी (Fenugreek)",
                market = "चित्तौड़गढ़ मंडी",
                district = "Chittorgarh",
                variety = "बारीक हरी",
                minPrice = 5400,
                maxPrice = 6600,
                modalPrice = 6100,
                arrivalDate = "आज (05 Nov)",
                trend = "स्थिर",
                isHotCrop = false,
                arrivalsTons = 15.0
            ),

            // Uttar Pradesh Mandis
            MandiItem(
                id = "mandi_wheat_banda",
                commodity = "गेहूं (Wheat)",
                market = "बांदा मंडी (UP)",
                district = "Banda",
                state = "Uttar Pradesh",
                variety = "कनक देसी",
                minPrice = 2450,
                maxPrice = 2820,
                modalPrice = 2700,
                arrivalDate = "आज (06 Nov)",
                trend = "तेज (+₹50)",
                isHotCrop = false,
                arrivalsTons = 110.0
            ),
            MandiItem(
                id = "mandi_paddy_etah",
                commodity = "धान / चावल (Paddy)",
                market = "एटा मंडी (UP)",
                district = "Etah",
                state = "Uttar Pradesh",
                variety = "बासमती 1509",
                minPrice = 3600,
                maxPrice = 4300,
                modalPrice = 4060,
                arrivalDate = "आज (06 Nov)",
                trend = "तेज (+₹120)",
                isHotCrop = true,
                arrivalsTons = 145.0
            ),

            // Gujarat Mandis
            MandiItem(
                id = "mandi_jeera_unjha",
                commodity = "जीरा (Cumin Seed)",
                market = "ऊंझा मंडी (Gujarat)",
                district = "Mehsana",
                state = "Gujarat",
                variety = "सुपर क्वालिटी",
                minPrice = 24000,
                maxPrice = 29500,
                modalPrice = 26800,
                arrivalDate = "आज (06 Nov)",
                trend = "तेज (+₹700)",
                isHotCrop = true,
                arrivalsTons = 310.0
            ),
            MandiItem(
                id = "mandi_cotton_rajkot",
                commodity = "कपास (Cotton)",
                market = "राजकोट मंडी (Gujarat)",
                district = "Rajkot",
                state = "Gujarat",
                variety = "शंकर 6",
                minPrice = 7100,
                maxPrice = 8250,
                modalPrice = 7700,
                arrivalDate = "आज (06 Nov)",
                trend = "स्थिर",
                isHotCrop = true,
                arrivalsTons = 210.0
            ),

            // Maharashtra Mandis
            MandiItem(
                id = "mandi_onion_lasalgaon",
                commodity = "प्याज (Onion)",
                market = "लासलगांव / नासिक मंडी (MH)",
                district = "Nashik",
                state = "Maharashtra",
                variety = "लाल समर",
                minPrice = 2200,
                maxPrice = 3150,
                modalPrice = 2750,
                arrivalDate = "आज (06 Nov)",
                trend = "तेज (+₹200)",
                isHotCrop = true,
                arrivalsTons = 420.0
            ),

            // Punjab & Haryana Mandis
            MandiItem(
                id = "mandi_paddy_karnal",
                commodity = "धान / चावल (Paddy)",
                market = "करनाल मंडी (Haryana)",
                district = "Karnal",
                state = "Haryana",
                variety = "1121 बासमती",
                minPrice = 4200,
                maxPrice = 4850,
                modalPrice = 4550,
                arrivalDate = "आज (06 Nov)",
                trend = "तेज (+₹90)",
                isHotCrop = true,
                arrivalsTons = 280.0
            )
        )

        if (stateFilter.isNullOrBlank() || stateFilter == "सभी राज्य" || stateFilter == "All") {
            return list
        }
        return list.filter { it.state.equals(stateFilter, ignoreCase = true) }
    }

    // Crops available for cross-mandi comparison in Chittorgarh & Mewar Belt
    fun getComparisonCrops(): List<String> = listOf(
        "लहसुन (Garlic)",
        "सोयाबीन (Soybean)",
        "गेहूं (Wheat)",
        "मक्का (Maize)",
        "सरसों (Mustard)",
        "चना (Gram)",
        "प्याज (Onion)"
    )

    fun getMandiComparison(commodityName: String): List<MandiItem> {
        val all = getMandiPrices()
        val keyword = commodityName.substringBefore(" ").trim()
        return all.filter { it.commodity.contains(keyword, ignoreCase = true) }
            .sortedByDescending { it.modalPrice }
    }

    // ─── 4. Comprehensive Government Schemes (Sarkari Yojanas) ───────────────────
    fun getSarkariYojanas(): List<YojanaItem> {
        return listOf(
            YojanaItem(
                id = "pm_kisan",
                titleHindi = "प्रधानमंत्री किसान सम्मान निधि",
                titleEnglish = "PM-Kisan Samman Nidhi",
                benefitAmount = "₹6,000 प्रति वर्ष (₹2,000 की 3 किश्तें)",
                category = "किसान",
                eligibility = "सभी भूमिधारक किसान परिवार जिनके नाम कृषि योग्य भूमि दर्ज है और e-KYC पूर्ण है।",
                documentsRequired = listOf(
                    "आधार कार्ड (Aadhaar Card)",
                    "जमाबंदी खतौनी नकल (Land Record)",
                    "बैंक खाता (Aadhaar NPCI Linked)",
                    "जन आधार कार्ड (राजस्थान हेतु)"
                ),
                portalName = "pmkisan.gov.in / e-Mitra",
                helpline = "155261 / 1800-115-526"
            ),
            YojanaItem(
                id = "tarbandi_rajasthan",
                titleHindi = "राजस्थान खेत तारबंदी योजना",
                titleEnglish = "Rajasthan Khet Tarbandi Scheme",
                benefitAmount = "लागत का 50% या अधिकतम ₹40,000 अनुदान (छोटे किसानों को ₹48,000)",
                category = "सब्सिडी",
                eligibility = "न्यूनतम 1.5 हेक्टेयर कृषि भूमि वाले किसान (अथवा 2 या अधिक किसान मिलकर समूह बना सकते हैं)।",
                documentsRequired = listOf(
                    "जन आधार कार्ड (Jan Aadhaar)",
                    "जमाबंदी नकल (6 माह से पुरानी न हो)",
                    "खेत का जीपीएस नक्शा / ट्रेस नक्शा",
                    "बैंक पासबुक व शपथ पत्र"
                ),
                portalName = "राजकिसान साथी पोर्टल (rajkisan.rajasthan.gov.in)",
                helpline = "181 / कृषि पर्यवेक्षक (Agriculture Supervisor)"
            ),
            YojanaItem(
                id = "kisan_solar_pump",
                titleHindi = "पीएम कुसुम सोलर पंप योजना (कंपोनेंट-बी)",
                titleEnglish = "PM KUSUM Solar Pump Scheme",
                benefitAmount = "60% तक सरकारी अनुदान (3HP से 10HP सोलर पंप)",
                category = "सब्सिडी",
                eligibility = "जिनके पास बिजली का कृषि कनेक्शन नहीं है तथा बोरवेल, कुआं या फार्म पौंड उपलब्ध है।",
                documentsRequired = listOf(
                    "आधार कार्ड",
                    "जन आधार कार्ड",
                    "जमाबंदी नकल",
                    "जल स्त्रोत प्रमाण पत्र (कुआं/बोरवेल)",
                    "विद्युत डिस्कॉम अनापत्ति प्रमाण (NOC)"
                ),
                portalName = "राजकिसान साथी पोर्टल / e-Mitra",
                helpline = "0141-2227613 / 181"
            ),
            YojanaItem(
                id = "kisan_credit_card",
                titleHindi = "किसान क्रेडिट कार्ड (KCC) रियायती ऋण",
                titleEnglish = "Kisan Credit Card (KCC) Scheme",
                benefitAmount = "₹3 लाख तक का कृषि ऋण मात्र 4% ब्याज दर पर",
                category = "किसान",
                eligibility = "सभी कृषक, काश्तकार, पट्टेदार किसान एवं पशुपालक व डेयरी संचालक।",
                documentsRequired = listOf(
                    "आधार कार्ड एवं पैन कार्ड",
                    "जमाबंदी / गिरदावरी रिपोर्ट",
                    "पटवारी द्वारा सत्यापित लैंड रिकॉर्ड",
                    "2 पासपोर्ट साइज फोटो"
                ),
                portalName = "निकटतम ग्रामीण बैंक / SBI / सहकारिता बैंक",
                helpline = "1800-180-1551"
            ),
            YojanaItem(
                id = "fasal_bima_pmfby",
                titleHindi = "प्रधानमंत्री फसल बीमा योजना (PMFBY)",
                titleEnglish = "Pradhan Mantri Fasal Bima Yojana",
                benefitAmount = "सूखा, ओलावृष्टि व प्राकृतिक आपदा पर 100% नुकसान भरपाई",
                category = "बीमा",
                eligibility = "अधिसूचित क्षेत्र में अधिसूचित फसल (रबी/खरीफ) उगाने वाले सभी ऋणी व गैर-ऋणी कृषक।",
                documentsRequired = listOf(
                    "पटवारी द्वारा जारी बोवनी प्रमाण पत्र / गिरदावरी",
                    "जमाबंदी नकल",
                    "जन आधार व आधार कार्ड",
                    "बैंक खाता विवरण"
                ),
                portalName = "pmfby.gov.in / e-Mitra / बैंक",
                helpline = "1800-180-1551 / 14447"
            ),
            YojanaItem(
                id = "drip_sprinkler_subsidy",
                titleHindi = "ड्रिप व फव्वारा सिंचाई संयंत्र अनुदान",
                titleEnglish = "Micro Irrigation Drip & Sprinkler Subsidy",
                benefitAmount = "लघु/सीमांत कृषकों को 75% तथा अन्य को 70% तक अनुदान",
                category = "सब्सिडी",
                eligibility = "न्यूनतम 0.2 हेक्टेयर सिंचित कृषि भूमि वाले सभी किसान।",
                documentsRequired = listOf(
                    "जन आधार कार्ड",
                    "जमाबंदी नकल",
                    "पंप सेट / कुएं का बिजली बिल या स्रोत",
                    "मृदा व जल परीक्षण रिपोर्ट (यदि उपलब्ध हो)"
                ),
                portalName = "राजकिसान साथी पोर्टल",
                helpline = "181 (राजस्थान संपर्क)"
            )
        )
    }
}
