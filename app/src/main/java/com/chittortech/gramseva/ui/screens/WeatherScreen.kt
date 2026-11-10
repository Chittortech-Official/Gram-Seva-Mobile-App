package com.chittortech.gramseva.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.chittortech.gramseva.data.repository.GramSevaRepository
import com.chittortech.gramseva.ui.components.GramSevaFooter
import com.chittortech.gramseva.ui.components.SectionHeader
import com.chittortech.gramseva.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun WeatherScreen(
    repository: GramSevaRepository = remember { GramSevaRepository() }
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 11 Tehsils of Chittorgarh district + border trading hubs (compact clean names)
    val tehsils = remember {
        LocationProvider.chittorgarhTehsils.map {
            GramSevaRepository.LocationCoord(it.nameHindi, it.lat, it.lon, it.district)
        } + listOf(
            GramSevaRepository.LocationCoord("नीमच", 24.4700, 74.8700, "Neemuch"),
            GramSevaRepository.LocationCoord("मंदसौर", 24.0722, 75.0682, "Mandsaur")
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
            .background(Color(0xFFF8FAFC)),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // ─── 1. Header & Tehsil Selector ──────────────────────────────────────
        item {
            Surface(
                color = GramCardWhite,
                border = BorderStroke(0.5.dp, Color(0xFFE2E8F0)),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
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
                                shape = CircleShape,
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                shadowElevation = 1.dp,
                                modifier = Modifier.size(38.dp)
                            ) {
                                androidx.compose.foundation.Image(
                                    painter = androidx.compose.ui.res.painterResource(id = com.chittortech.gramseva.R.drawable.app_logo),
                                    contentDescription = "GramSeva Logo",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
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
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Compact Tehsil Pills
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // GPS Button
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFE8F5E9),
                            border = BorderStroke(1.dp, Color(0xFF81C784)),
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
                                    CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp, color = Color(0xFF2E7D32))
                                } else {
                                    Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                                }
                                Text("GPS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            }
                        }

                        tehsils.forEach { loc ->
                            val isSelected = selectedLocation.nameHindi == loc.nameHindi
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) Color(0xFF1B5E20) else Color.White,
                                border = BorderStroke(1.dp, if (isSelected) Color(0xFF1B5E20) else Color(0xFFE2E8F0)),
                                shadowElevation = if (isSelected) 1.5.dp else 0.dp,
                                modifier = Modifier.clickable {
                                    selectedLocation = loc
                                    weather = WeatherCache.getCachedWeather(context, loc.nameHindi)
                                }
                            ) {
                                Text(
                                    text = loc.nameHindi,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF1E293B),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ─── 2. Atmospheric Modern Weather Hero Card ──────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF0284C7), // Vibrant Sky 600
                                        Color(0xFF0369A1)  // Deep Sky 700
                                    )
                                )
                            )
                            .padding(18.dp)
                    ) {
                        // Top row inside card: Location on left, Live satellite badge on right
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "${selectedLocation.nameHindi} तहसील",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            // Frosted Satellite Live Badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.22f),
                                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF4ADE80))
                                    )
                                    Text(
                                        text = "उपग्रह लाइव",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Middle Row: Temperature & Large Weather Graphic
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.Top) {
                                    Text(
                                        text = "${weather.temperature.toInt()}",
                                        fontSize = 56.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        lineHeight = 56.sp
                                    )
                                    Text(
                                        text = "°C",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFBAE6FD),
                                        modifier = Modifier.padding(top = 4.dp, start = 2.dp)
                                    )
                                }

                                Text(
                                    text = weather.condition,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                Text(
                                    text = "महसूस ~${weather.feelsLike.toInt()}°C • हवा ${String.format(java.util.Locale.US, "%.1f", weather.windSpeed)} km/h",
                                    fontSize = 12.sp,
                                    color = Color(0xFFE0F2FE)
                                )
                            }

                            // Glowing circular weather emblem
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                val weatherIcon = when {
                                    weather.rainProbability > 40 -> Icons.Default.WaterDrop
                                    weather.condition.contains("बादल") -> Icons.Default.Cloud
                                    else -> Icons.Default.WbSunny
                                }
                                val iconTint = when {
                                    weather.rainProbability > 40 -> Color(0xFF93C5FD)
                                    weather.condition.contains("बादल") -> Color(0xFFE2E8F0)
                                    else -> Color(0xFFFDE047)
                                }
                                Icon(
                                    imageVector = weatherIcon,
                                    contentDescription = null,
                                    tint = iconTint,
                                    modifier = Modifier.size(42.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // High / Low Range Pill
                        val maxT = weather.dailyForecast.firstOrNull()?.maxTemp?.toInt() ?: (weather.temperature.toInt() + 4)
                        val minT = weather.dailyForecast.firstOrNull()?.minTemp?.toInt() ?: (weather.temperature.toInt() - 6)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.16f),
                            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 7.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🔺 अधिकतम: $maxT°C", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("•", color = Color.White.copy(alpha = 0.6f))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("🔻 न्यूनतम: $minT°C", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE0F2FE))
                            }
                        }
                    }
                }
            }
        }

        // ─── 3. 3 Themed Weather Metric Cards ─────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Rain Card (Blue)
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDBEAFE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(17.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${weather.rainProbability}%",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E3A8A)
                        )
                        Text(
                            text = "वर्षा संभावना",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF3B82F6)
                        )
                    }
                }

                // Wind Card (Amber)
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Air, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(17.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${String.format(java.util.Locale.US, "%.1f", weather.windSpeed)} km/h",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF78350F),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "हवा की गति",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFD97706)
                        )
                    }
                }

                // Humidity Card (Emerald)
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                    border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD1FAE5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Grain, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(17.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${weather.humidity}%",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF064E3B)
                        )
                        Text(
                            text = "हवा में नमी",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF059669)
                        )
                    }
                }
            }
        }

        // ─── 4. Kisan Agri Advisory Bulletin Card ─────────────────────────────
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.TipsAndUpdates,
                                contentDescription = null,
                                tint = Color(0xFF15803D),
                                modifier = Modifier.size(19.dp)
                            )
                            Text(
                                text = "कृषि मौसम परामर्श (KVK चित्तौड़गढ़)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFDCFCE7)
                        ) {
                            Text(
                                text = "आज की सलाह",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = weather.farmerAdvisory,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF14532D),
                        lineHeight = 19.sp
                    )
                }
            }
        }

        // ─── 5. 7-Day Extended Farmer Forecast ────────────────────────────────
        item {
            Spacer(modifier = Modifier.height(18.dp))
            SectionHeader(
                title = "📅 7 दिवसीय मौसम व वर्षा पूर्वानुमान",
                subtitle = "चित्तौड़गढ़ ज़िला • कटाई, छिड़काव व सिंचाई योजना"
            )
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GramCardWhite),
                border = BorderStroke(0.5.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    weather.dailyForecast.forEachIndexed { index, day ->
                        DailyForecastRow(day = day)
                        if (index < weather.dailyForecast.size - 1) {
                            HorizontalDivider(
                                color = Color(0xFFF1F5F9),
                                thickness = 1.dp,
                                modifier = Modifier.padding(horizontal = 14.dp)
                            )
                        }
                    }
                }
            }
        }

        // ─── 6. Kisan Toll-Free Weather Helpline Card ─────────────────────────
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable {
                        try {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:18001801551"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Ignored if dialer not present
                        }
                    },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDCFCE7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(20.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "किसान कॉल सेन्टर (निःशुल्क मौसम सलाह)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF14532D)
                        )
                        Text(
                            text = "1800-180-1551 • सुबह 6:00 से रात 10:00 तक",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF16A34A)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF15803D)
                    ) {
                        Text(
                            text = "कॉल करें",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }

        // ─── 7. Official GramSeva Footer ──────────────────────────────────────
        item {
            GramSevaFooter()
        }
    }
}

@Composable
fun DailyForecastRow(day: DailyForecast) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Day Name & Short Date
        Column(modifier = Modifier.width(82.dp)) {
            Text(
                text = day.dayName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                maxLines = 1
            )
            val shortDate = if (day.date.length >= 10) {
                val parts = day.date.split("-")
                if (parts.size == 3) "${parts[2]} नवं" else day.date
            } else day.date
            Text(
                text = shortDate,
                fontSize = 11.sp,
                color = Color(0xFF64748B),
                maxLines = 1
            )
        }

        // Center Condition Icon & Rain Status Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 6.dp)
        ) {
            val isRain = day.rainProbability > 30 || day.condition.contains("बारिश")
            val isCloud = day.condition.contains("बादल")
            val icon = when {
                isRain -> Icons.Default.WaterDrop
                isCloud -> Icons.Default.Cloud
                else -> Icons.Default.WbSunny
            }
            val tint = when {
                isRain -> Color(0xFF2563EB)
                isCloud -> Color(0xFF64748B)
                else -> Color(0xFFEAB308)
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )

            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = day.condition,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1E293B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (day.rainProbability > 20) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFDBEAFE)
                    ) {
                        Text(
                            text = "🌧️ ${day.rainProbability}% वर्षा",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D4ED8),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                } else {
                    Text(
                        text = "वर्षा मुक्त",
                        fontSize = 10.sp,
                        color = Color(0xFF15803D),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // High & Low Temperature Capsule
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF8FAFC),
            border = BorderStroke(0.5.dp, Color(0xFFE2E8F0))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "${day.maxTemp.toInt()}°",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFDC2626) // Amber/Red for high
                )
                Text(
                    text = "/",
                    fontSize = 11.sp,
                    color = Color(0xFFCBD5E1)
                )
                Text(
                    text = "${day.minTemp.toInt()}°",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2563EB) // Blue for low
                )
            }
        }
    }
}
