package com.chittortech.gramseva.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chittortech.gramseva.data.cache.WeatherCache
import com.chittortech.gramseva.data.location.LocationProvider
import com.chittortech.gramseva.data.models.DailyForecast
import com.chittortech.gramseva.data.models.WeatherInfo
import com.chittortech.gramseva.data.repository.GramSevaRepository
import com.chittortech.gramseva.ui.components.SectionHeader
import com.chittortech.gramseva.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun WeatherScreen(
    repository: GramSevaRepository = remember { GramSevaRepository() }
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Focused 100% on Chittorgarh District's 11 Tehsils & border trading hubs
    val tehsils = remember {
        LocationProvider.chittorgarhTehsils.map {
            GramSevaRepository.LocationCoord("${it.nameHindi} तहसील", it.lat, it.lon, it.district)
        } + listOf(
            GramSevaRepository.LocationCoord("नीमच (सीमावर्ती)", 24.4700, 74.8700, "Neemuch"),
            GramSevaRepository.LocationCoord("मंदसौर (मंडी हब)", 24.0722, 75.0682, "Mandsaur")
        )
    }

    var selectedLocation by remember { mutableStateOf(tehsils.first()) }
    var isDetectingGps by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            coroutineScope.launch {
                isDetectingGps = true
                val detected = LocationProvider.detectCurrentLocation(context)
                if (detected != null) {
                    selectedLocation = GramSevaRepository.LocationCoord(
                        nameHindi = detected.nameHindi,
                        lat = detected.lat,
                        lon = detected.lon,
                        district = detected.district
                    )
                }
                isDetectingGps = false
            }
        }
    }

    var isLoading by remember { mutableStateOf(false) }
    // Initialized from persistent cache to completely eliminate 31°C to 26°C jump
    var weather by remember {
        mutableStateOf(WeatherCache.getCachedWeather(context, selectedLocation.nameHindi))
    }

    // Load weather on location change
    LaunchedEffect(selectedLocation) {
        isLoading = true
        val liveW = repository.fetchWeather(
            lat = selectedLocation.lat,
            lon = selectedLocation.lon,
            cityName = selectedLocation.nameHindi
        )
        weather = liveW
        WeatherCache.saveWeather(context, liveW)
        isLoading = false
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(GramBackground),
        contentPadding = PaddingValues(bottom = 12.dp)
    ) {
        // ─── 1. Header & Tehsil Selector ──────────────────────────────────────
        item {
            Surface(
                color = GramCardWhite,
                border = BorderStroke(1.dp, GramBorder),
                shadowElevation = 1.5.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = androidx.compose.foundation.shape.CircleShape,
                                color = Color.White,
                                border = BorderStroke(1.dp, GramBorder),
                                shadowElevation = 1.dp,
                                modifier = Modifier.size(38.dp)
                            ) {
                                androidx.compose.foundation.Image(
                                    painter = androidx.compose.ui.res.painterResource(id = com.chittortech.gramseva.R.drawable.app_logo),
                                    contentDescription = "GramSeva Logo",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                )
                            }

                            Column {
                                Text(
                                    text = "चित्तौड़गढ़ मौसम रडार",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GramNavy
                                )
                                Text(
                                    text = "उपग्रह आधारित लाइव मौसम व 7-दिन सलाह",
                                    fontSize = 11.sp,
                                    color = GramSlate
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    isLoading = true
                                    val liveW = repository.fetchWeather(
                                        lat = selectedLocation.lat,
                                        lon = selectedLocation.lon,
                                        cityName = selectedLocation.nameHindi
                                    )
                                    weather = liveW
                                    WeatherCache.saveWeather(context, liveW)
                                    isLoading = false
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = GramPrimary)
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = GramPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "📍 तहसील चुनें (1-टैप लाइव मौसम):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GramSlate
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Clean Tehsil Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // GPS Button
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = GramGreenLight,
                            border = BorderStroke(1.dp, GramGreen),
                            modifier = Modifier.clickable {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                if (isDetectingGps) {
                                    CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp, color = GramGreen)
                                } else {
                                    Icon(Icons.Default.MyLocation, contentDescription = null, tint = GramGreen, modifier = Modifier.size(14.dp))
                                }
                                Text("GPS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GramGreen)
                            }
                        }

                        tehsils.forEach { loc ->
                            val isSelected = selectedLocation.nameHindi == loc.nameHindi
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) GramPrimary else GramBackground,
                                border = BorderStroke(1.dp, if (isSelected) GramPrimary else GramBorder),
                                modifier = Modifier.clickable {
                                    selectedLocation = loc
                                    weather = WeatherCache.getCachedWeather(context, loc.nameHindi)
                                }
                            ) {
                                Text(
                                    text = loc.nameHindi,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else GramNavy,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ─── 2. Main Executive Weather Hero Card ──────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GramCardWhite),
                    border = BorderStroke(0.5.dp, GramBorder),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFFFF7ED),
                                        Color.White
                                    )
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = GramPrimary, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = selectedLocation.nameHindi,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GramNavy
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color.White,
                                        border = BorderStroke(0.5.dp, GramBorder)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            androidx.compose.foundation.Image(
                                                painter = androidx.compose.ui.res.painterResource(id = com.chittortech.gramseva.R.drawable.app_logo),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                            )
                                            Text("ग्राम-सेवा", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GramPrimaryDark)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = weather.condition,
                                    fontSize = 13.sp,
                                    color = GramSlate,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "महसूस: ~${weather.feelsLike.toInt()}°C",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }

                            // Temperature Display without wrapping
                            Row(
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text(
                                    text = "${weather.temperature.toInt()}",
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.Black,
                                    color = GramPrimaryDark
                                )
                                Text(
                                    text = "°C",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GramPrimaryDark,
                                    modifier = Modifier.padding(top = 4.dp, start = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Dynamic Farmer Advisory Box
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = GramSurfaceVariant,
                            border = BorderStroke(1.dp, GramGold.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.TipsAndUpdates, contentDescription = null, tint = GramGold, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "किसान कृषि मौसम सलाह:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GramGoldGradStart
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = weather.farmerAdvisory,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GramNavy,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // ─── 3. Weather Metrics Grid (Clean 3-Item Row) ────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WeatherMetricPill(
                    icon = Icons.Default.WaterDrop,
                    title = "वर्षा संभावना",
                    value = "${weather.rainProbability}%",
                    iconColor = GramBlue,
                    bgColor = GramBlueLight,
                    modifier = Modifier.weight(1f)
                )

                WeatherMetricPill(
                    icon = Icons.Default.Air,
                    title = "हवा की गति",
                    value = "${String.format(java.util.Locale.US, "%.1f", weather.windSpeed)} km/h",
                    iconColor = GramAmber,
                    bgColor = GramAmberLight,
                    modifier = Modifier.weight(1f)
                )

                WeatherMetricPill(
                    icon = Icons.Default.Grain,
                    title = "हवा में नमी",
                    value = "${weather.humidity}%",
                    iconColor = GramGreen,
                    bgColor = GramGreenLight,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ─── 4. 7-Day Extended Farmer Forecast ────────────────────────────────
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(
                title = "📅 7 दिवसीय मौसम व वर्षा पूर्वानुमान",
                subtitle = "फसल कटाई, दवा छिड़काव व सिंचाई की योजना बनाएं"
            )
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = GramCardWhite),
                border = BorderStroke(0.5.dp, GramBorder),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    weather.dailyForecast.forEachIndexed { index, day ->
                        DailyForecastRow(day = day)
                        if (index < weather.dailyForecast.size - 1) {
                            HorizontalDivider(
                                color = GramBorder.copy(alpha = 0.45f),
                                modifier = Modifier.padding(horizontal = 14.dp)
                            )
                        }
                    }
                }
            }
        }

        // ─── 5. Kisan Weather Helpline Banner ─────────────────────────────────
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = GramCardWhite),
                border = BorderStroke(0.5.dp, GramBorder),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(GramGreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = GramGreen, modifier = Modifier.size(20.dp))
                    }

                    Column {
                        Text(
                            text = "किसान कॉल सेन्टर (टोल-फ्री मौसम सलाह)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = GramNavy
                        )
                        Text(
                            text = "1800-180-1551 (सुबह 6:00 से रात 10:00 तक)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GramGreen
                        )
                    }
                }
            }
        }

        // ─── 6. Official GramSeva Footer ──────────────────────────────────────
        item {
            com.chittortech.gramseva.ui.components.GramSevaFooter()
        }
    }
}

@Composable
fun WeatherMetricPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    iconColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GramCardWhite),
        border = BorderStroke(0.5.dp, GramBorder),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = GramNavy,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = title,
                fontSize = 10.sp,
                color = GramSlate,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun DailyForecastRow(day: DailyForecast) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Day & Short Date (Fixed width prevents shifting on narrow screens)
        Column(modifier = Modifier.width(74.dp)) {
            Text(
                text = day.dayName,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = GramNavy,
                maxLines = 1
            )
            val shortDate = if (day.date.length >= 10) day.date.substring(5) else day.date
            Text(
                text = shortDate,
                fontSize = 11.sp,
                color = GramSlate,
                maxLines = 1
            )
        }

        // Condition & Rain Indicator (Center column flexes to fill available space)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 6.dp)
        ) {
            Icon(
                imageVector = if (day.rainProbability > 30) Icons.Default.WaterDrop else Icons.Default.WbSunny,
                contentDescription = null,
                tint = if (day.rainProbability > 30) GramBlue else GramGold,
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = day.condition,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = GramNavy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "वर्षा: ${day.rainProbability}%",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (day.rainProbability > 30) GramBlue else GramSlate,
                    maxLines = 1
                )
            }
        }

        // Min & Max Temp Pill (Neat right-aligned capsule)
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = GramBackground,
            border = BorderStroke(0.5.dp, GramBorder)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${day.maxTemp.toInt()}°",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = GramPrimaryDark
                )
                Text(
                    text = "/",
                    fontSize = 11.sp,
                    color = GramSlate.copy(alpha = 0.5f)
                )
                Text(
                    text = "${day.minTemp.toInt()}°",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GramSlate
                )
            }
        }
    }
}
