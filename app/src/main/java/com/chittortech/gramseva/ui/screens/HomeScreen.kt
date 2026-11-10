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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chittortech.gramseva.data.cache.WeatherCache
import com.chittortech.gramseva.data.location.LocationProvider
import com.chittortech.gramseva.data.models.MandiItem
import com.chittortech.gramseva.data.models.UserLocation
import com.chittortech.gramseva.data.models.WeatherInfo
import com.chittortech.gramseva.data.repository.GramSevaRepository
import com.chittortech.gramseva.ui.components.*
import com.chittortech.gramseva.ui.theme.*
import com.chittortech.gramseva.util.ShareUtils
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    repository: GramSevaRepository = remember { GramSevaRepository() },
    onNavigateToTab: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var currentLocation by remember { mutableStateOf(LocationProvider.defaultLocation) }
    var showLocationSheet by remember { mutableStateOf(false) }
    var isDetectingGps by remember { mutableStateOf(false) }

    // Initialized from persistent cache to completely eliminate 31°C to 26°C jump
    var weather by remember {
        mutableStateOf(WeatherCache.getCachedWeather(context, currentLocation.nameHindi))
    }

    var selectedCropFilter by remember { mutableStateOf("सभी फसलें") }
    val cropFilters = listOf("सभी फसलें", "लहसुन", "सोयाबीन", "गेहूं", "मक्का", "चना", "सरसों", "अजवाइन", "कपास")

    var allMandiPrices by remember { mutableStateOf(repository.getMandiPrices(currentLocation.state)) }
    val filteredMandiPrices = remember(selectedCropFilter, allMandiPrices) {
        if (selectedCropFilter == "सभी फसलें") allMandiPrices
        else allMandiPrices.filter { it.commodity.contains(selectedCropFilter, ignoreCase = true) }
    }

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
                    currentLocation = detected
                    val liveW = repository.fetchWeather(detected.lat, detected.lon, detected.nameHindi)
                    weather = liveW
                    WeatherCache.saveWeather(context, liveW)
                    allMandiPrices = repository.fetchLiveMandiPrices(state = detected.state)
                }
                isDetectingGps = false
                showLocationSheet = false
            }
        }
    }

    fun triggerGpsDetection() {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // Fetch live weather and live government mandi feed on startup or location change
    LaunchedEffect(currentLocation) {
        val liveW = repository.fetchWeather(
            lat = currentLocation.lat,
            lon = currentLocation.lon,
            cityName = currentLocation.nameHindi
        )
        weather = liveW
        WeatherCache.saveWeather(context, liveW)
        allMandiPrices = repository.fetchLiveMandiPrices(state = currentLocation.state)
    }

    if (showLocationSheet) {
        LocationSelectorSheet(
            currentLocation = currentLocation,
            onLocationSelected = { newLoc ->
                currentLocation = newLoc
                showLocationSheet = false
            },
            onDetectGpsClick = { triggerGpsDetection() },
            isDetectingGps = isDetectingGps,
            onDismissRequest = { showLocationSheet = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GramBackground)
    ) {
        GramTopBar(
            title = "ग्राम-सेवा (GramSeva)",
            subtitle = "चित्तौड़गढ़ ज़िला • किसान डिजिटल साथी",
            locationText = currentLocation.nameHindi,
            onLocationClick = { showLocationSheet = true },
            onRefreshClick = {
                coroutineScope.launch {
                    val liveW = repository.fetchWeather(currentLocation.lat, currentLocation.lon, currentLocation.nameHindi)
                    weather = liveW
                    WeatherCache.saveWeather(context, liveW)
                    allMandiPrices = repository.fetchLiveMandiPrices(state = currentLocation.state)
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            // ─── 0. Chittorgarh Tehsils 1-Tap Quick Switcher ───────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GramCardWhite)
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📍 चित्तौड़गढ़ तहसील चुनें:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GramNavy
                        )
                        Text(
                            text = "11 तहसीलें",
                            fontSize = 11.sp,
                            color = GramSlate
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LocationProvider.chittorgarhTehsils.forEach { tehsil ->
                            val isSelected = currentLocation.nameHindi == tehsil.nameHindi
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) GramPrimary else GramBackground,
                                border = BorderStroke(1.dp, if (isSelected) GramPrimary else GramBorder),
                                modifier = Modifier.clickable {
                                    currentLocation = tehsil
                                    weather = WeatherCache.getCachedWeather(context, tehsil.nameHindi)
                                }
                            ) {
                                Text(
                                    text = tehsil.nameHindi,
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

            // ─── 1. Live Weather & Farmer Advisory Card ───────────────────────
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .clickable { onNavigateToTab(2) }, // Navigate to Weather Radar
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GramCardWhite),
                    border = BorderStroke(1.dp, GramBorder),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.WbSunny, contentDescription = null, tint = GramGold, modifier = Modifier.size(24.dp))
                                Column {
                                    Text(
                                        text = "${weather.city} तहसील लाइव मौसम",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GramNavy
                                    )
                                    Text(
                                        text = "${weather.condition} • 7 दिन पूर्वानुमान →",
                                        fontSize = 12.sp,
                                        color = GramSlate
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.Top) {
                                Text(
                                    text = "${weather.temperature.toInt()}",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = GramPrimary
                                )
                                Text(
                                    text = "°C",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GramPrimary,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Advisory Banner
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GramPrimaryLight,
                            border = BorderStroke(1.dp, GramPrimary.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = weather.farmerAdvisory,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GramPrimaryDark,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }

            // ─── 2. Quick Feature Navigation Strip ─────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionTile(
                        icon = Icons.Default.Storefront,
                        title = "मंडी भाव",
                        subtitle = "ताजा रेट",
                        color = GramPrimary,
                        bgColor = GramPrimaryLight,
                        onClick = { onNavigateToTab(1) },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionTile(
                        icon = Icons.Default.CloudQueue,
                        title = "मौसम रडार",
                        subtitle = "7-दिन अलर्ट",
                        color = GramBlue,
                        bgColor = GramBlueLight,
                        onClick = { onNavigateToTab(2) },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionTile(
                        icon = Icons.Default.AccountBalance,
                        title = "योजनाएं",
                        subtitle = "पात्रता जांचें",
                        color = GramGold,
                        bgColor = GramGoldLight,
                        onClick = { onNavigateToTab(3) },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionTile(
                        icon = Icons.Default.PinDrop,
                        title = "डाकघर",
                        subtitle = "पिनकोड खोज",
                        color = GramGreen,
                        bgColor = GramGreenLight,
                        onClick = { onNavigateToTab(3) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ─── 3. Key Metric Cards ──────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCard(
                        title = "चित्तौड़गढ़ मंडियां",
                        value = "6 मुख्य मंडियां",
                        subtitle = "लाइव भाव",
                        icon = Icons.Default.Storefront,
                        iconBg = GramPrimaryLight,
                        iconTint = GramPrimary,
                        onClick = { onNavigateToTab(1) },
                        modifier = Modifier.weight(1f)
                    )

                    KpiCard(
                        title = "लहसुन सर्वोच्च भाव",
                        value = "₹19,100",
                        subtitle = "निम्बाहेड़ा मंडी",
                        icon = Icons.Default.TrendingUp,
                        iconBg = GramGreenLight,
                        iconTint = GramGreen,
                        valueColor = GramGreen,
                        onClick = { onNavigateToTab(1) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ─── 4. Live Mandi Rates Section ──────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(
                    title = "🌾 चित्तौड़गढ़ ज़िला आज के मंडी भाव",
                    subtitle = "चित्तौड़गढ़, निम्बाहेड़ा, कपासन, बेगूं, बड़ी सादड़ी व नीमच",
                    actionText = "मंडी तुलना देखें →",
                    onActionClick = { onNavigateToTab(1) }
                )

                // Filter Chip Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    cropFilters.forEach { crop ->
                        val isSelected = selectedCropFilter == crop
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCropFilter = crop },
                            label = { Text(crop, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
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

            // ─── 5. Mandi Price Cards List ─────────────────────────────────────
            items(filteredMandiPrices) { item ->
                MandiPriceCard(
                    item = item,
                    onShareClick = { ShareUtils.shareMandiItem(context, item) }
                )
            }
        }
    }
}

@Composable
fun QuickActionTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    bgColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GramCardWhite),
        border = BorderStroke(1.dp, GramBorder),
        elevation = CardDefaults.cardElevation(1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = GramNavy,
                maxLines = 1
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = GramSlate,
                maxLines = 1
            )
        }
    }
}

@Composable
fun MandiPriceCard(
    item: MandiItem,
    onShareClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = GramCardWhite),
        border = BorderStroke(1.dp, GramBorder),
        elevation = CardDefaults.cardElevation(1.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = item.commodity,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GramNavy
                    )
                    Text(
                        text = "${item.market} • वैरायटी: ${item.variety}",
                        fontSize = 12.sp,
                        color = GramSlate
                    )
                }

                StatusBadge(status = item.trend)
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = GramBorder.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "मॉडल भाव (Modal Rate)",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text(
                        text = "₹${item.modalPrice}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GramPrimary
                    )
                    Text(
                        text = "प्रति क्विंटल (Per Quintal)",
                        fontSize = 10.sp,
                        color = GramSlate
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "न्यूनतम - अधिकतम",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text(
                        text = "₹${item.minPrice} - ₹${item.maxPrice}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GramNavy
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = item.arrivalDate,
                            fontSize = 10.sp,
                            color = GramGreen,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = onShareClick,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Share,
                                contentDescription = "Share",
                                tint = Color(0xFF25D366),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
