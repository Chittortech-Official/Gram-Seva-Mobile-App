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

            // ─── 2. Key Metric Cards ──────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCard(
                        title = "चित्तौड़गढ़ मंडियां",
                        value = "6 मुख्य मंडियां",
                        subtitle = "ताजा भाव",
                        icon = Icons.Default.Storefront,
                        iconBg = Color(0xFFE8F5E9),
                        iconTint = Color(0xFF2E7D32),
                        onClick = { onNavigateToTab(1) },
                        modifier = Modifier.weight(1f)
                    )

                    KpiCard(
                        title = "लहसुन सर्वोच्च भाव",
                        value = "₹19,100",
                        subtitle = "निम्बाहेड़ा मंडी",
                        icon = Icons.Default.TrendingUp,
                        iconBg = Color(0xFFFEF3C7),
                        iconTint = Color(0xFFB45309),
                        valueColor = Color(0xFFB45309),
                        onClick = { onNavigateToTab(1) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ─── 3. Live Mandi Rates Section ──────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(
                    title = "🌾 आज के मंडी भाव",
                    subtitle = "चित्तौड़गढ़, निम्बाहेड़ा, कपासन, बेगूं, बड़ी सादड़ी व नीमच",
                    actionText = "मंडी तुलना →",
                    onActionClick = { onNavigateToTab(1) }
                )

                // Filter Chip Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    cropFilters.forEach { crop ->
                        val isSelected = selectedCropFilter == crop
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCropFilter = crop },
                            label = { Text(crop, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF1B5E20),
                                selectedLabelColor = Color.White,
                                containerColor = GramCardWhite,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = if (isSelected) Color(0xFF1B5E20) else Color(0xFFE2E8F0),
                                enabled = true,
                                selected = isSelected
                            )
                        )
                    }
                }
            }

            // ─── 4. Mandi Price Cards List ─────────────────────────────────────
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
fun MandiPriceCard(
    item: MandiItem,
    onShareClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GramCardWhite),
        border = BorderStroke(0.5.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            // Header Row: Crop Name, Trend & Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.commodity.substringBefore(" ("),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GramNavy
                    )
                    Text(
                        text = "${item.market} • वैरायटी: ${item.variety}",
                        fontSize = 11.sp,
                        color = GramSlate,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StatusBadge(status = item.trend)

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9))
                            .clickable { onShareClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Price Row: Modal Price on left, Range on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "₹${item.modalPrice}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1B5E20)
                    )
                    Text(
                        text = "/क्विंटल",
                        fontSize = 11.sp,
                        color = GramSlate,
                        modifier = Modifier.padding(bottom = 1.5.dp)
                    )
                }

                Text(
                    text = "रेंज: ₹${item.minPrice} - ₹${item.maxPrice}",
                    fontSize = 11.sp,
                    color = GramSlate,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
