package com.chittortech.gramseva.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val coroutineScope = rememberCoroutineScope()
    val locations = remember { repository.supportedLocations }
    var selectedLocation by remember { mutableStateOf(locations.first()) }

    var isLoading by remember { mutableStateOf(false) }
    var weather by remember {
        mutableStateOf(
            WeatherInfo(
                city = selectedLocation.nameHindi,
                temperature = 30.7,
                condition = "साफ धूप (Clear Sky)",
                rainProbability = 0,
                windSpeed = 3.4,
                humidity = 28,
                farmerAdvisory = "✅ मौसम अनुकूल: रबी फसलों की बुवाई, सिंचाई व मंडी परिवहन के लिए उपयुक्त मौसम।"
            )
        )
    }

    // Load weather on location change
    LaunchedEffect(selectedLocation) {
        isLoading = true
        weather = repository.fetchWeather(
            lat = selectedLocation.lat,
            lon = selectedLocation.lon,
            cityName = selectedLocation.nameHindi
        )
        isLoading = false
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(GramBackground),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        // ─── 1. Header & District Selector ───────────────────────────────────
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
                        Column {
                            Text(
                                text = "किसान मौसम रडार",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = GramNavy
                            )
                            Text(
                                text = "Open-Meteo उपग्रह डेटा • 7 दिन पूर्वानुमान",
                                fontSize = 11.sp,
                                color = GramSlate
                            )
                        }

                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    isLoading = true
                                    weather = repository.fetchWeather(
                                        lat = selectedLocation.lat,
                                        lon = selectedLocation.lon,
                                        cityName = selectedLocation.nameHindi
                                    )
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
                        text = "तहसील व जिला चुनें:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GramSlate
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Location Horizontal Chip Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        locations.forEach { loc ->
                            val isSelected = selectedLocation.nameHindi == loc.nameHindi
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedLocation = loc },
                                label = { Text(loc.nameHindi, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GramPrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = GramCardWhite,
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (isSelected) GramPrimary else GramBorder,
                                    enabled = true,
                                    selected = isSelected
                                )
                            )
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
                    border = BorderStroke(1.dp, GramBorder),
                    elevation = CardDefaults.cardElevation(2.5.dp),
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
                    value = "${weather.windSpeed} km/h",
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
            Spacer(modifier = Modifier.height(18.dp))
            SectionHeader(
                title = "📅 7 दिवसीय मौसम व वर्षा पूर्वानुमान",
                subtitle = "फसल कटाई, दवा छिड़काव व सिंचाई की योजना बनाएं"
            )
        }

        items(weather.dailyForecast) { day ->
            DailyForecastRow(day = day)
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
                border = BorderStroke(1.dp, GramBorder)
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
        border = BorderStroke(1.dp, GramBorder),
        elevation = CardDefaults.cardElevation(1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = GramNavy,
                maxLines = 1
            )
            Text(
                text = title,
                fontSize = 10.sp,
                color = GramSlate,
                maxLines = 1
            )
        }
    }
}

@Composable
fun DailyForecastRow(day: DailyForecast) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GramCardWhite),
        border = BorderStroke(1.dp, GramBorder),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Day & Date (Flexible weight)
            Column(modifier = Modifier.weight(1.1f)) {
                Text(
                    text = day.dayName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = GramNavy
                )
                Text(
                    text = day.date,
                    fontSize = 11.sp,
                    color = GramSlate
                )
            }

            // Condition & Rain Indicator (Center column)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1.5f)
            ) {
                Icon(
                    imageVector = if (day.rainProbability > 30) Icons.Default.WaterDrop else Icons.Default.WbSunny,
                    contentDescription = null,
                    tint = if (day.rainProbability > 30) GramBlue else GramGold,
                    modifier = Modifier.size(18.dp)
                )
                Column {
                    Text(
                        text = day.condition,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = GramNavy,
                        maxLines = 1
                    )
                    Text(
                        text = "वर्षा: ${day.rainProbability}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (day.rainProbability > 30) GramBlue else GramSlate
                    )
                }
            }

            // Min & Max Temp (Right column)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "${day.maxTemp.toInt()}°",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = GramPrimaryDark
                )
                Text(
                    text = "/",
                    fontSize = 13.sp,
                    color = GramBorder
                )
                Text(
                    text = "${day.minTemp.toInt()}°",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GramSlate
                )
            }
        }
    }
}
