package com.chittortech.gramseva.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chittortech.gramseva.data.models.MandiItem
import com.chittortech.gramseva.data.models.WeatherInfo
import com.chittortech.gramseva.data.repository.GramSevaRepository
import com.chittortech.gramseva.ui.components.*
import com.chittortech.gramseva.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    repository: GramSevaRepository = remember { GramSevaRepository() }
) {
    val coroutineScope = rememberCoroutineScope()
    var weather by remember {
        mutableStateOf(
            WeatherInfo(
                city = "चित्तौड़गढ़",
                temperature = 28.5,
                condition = "साफ धूप (Clear Sky)",
                rainProbability = 10,
                windSpeed = 7.5,
                humidity = 40,
                farmerAdvisory = "✅ मौसम अनुकूल: मंडी ले जाने और फसलों की कटाई के लिए उत्तम दिन।"
            )
        )
    }

    var selectedCropFilter by remember { mutableStateOf("सभी फसलें") }
    val cropFilters = listOf("सभी फसलें", "लहसुन", "प्याज", "गेहूं", "सरसों", "सोयाबीन", "मक्का")

    val allMandiPrices = remember { repository.getMandiPrices() }
    val filteredMandiPrices = remember(selectedCropFilter) {
        if (selectedCropFilter == "सभी फसलें") allMandiPrices
        else allMandiPrices.filter { it.commodity.contains(selectedCropFilter, ignoreCase = true) }
    }

    var pincodeQuery by remember { mutableStateOf("") }
    var pincodeResults by remember { mutableStateOf(emptyList<com.chittortech.gramseva.data.models.PincodeRecord>()) }
    var isSearchingPincode by remember { mutableStateOf(false) }

    // Fetch live weather on startup
    LaunchedEffect(Unit) {
        weather = repository.fetchWeather()
    }

    Scaffold(
        topBar = {
            GramTopBar(
                title = "ग्राम-सेवा (GramSeva)",
                subtitle = "चित्तौड़गढ़ व राजस्थान • लाइव मंडी व सुविधाएं",
                onRefreshClick = {
                    coroutineScope.launch {
                        weather = repository.fetchWeather()
                    }
                }
            )
        },
        containerColor = GramBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // ─── 1. Live Weather & Farmer Advisory Card ───────────────────────
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
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
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.WbSunny, contentDescription = null, tint = GramGold, modifier = Modifier.size(24.dp))
                                Column {
                                    Text(
                                        text = "${weather.city} मौसम",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GramNavy
                                    )
                                    Text(
                                        text = weather.condition,
                                        fontSize = 12.sp,
                                        color = GramSlate
                                    )
                                }
                            }

                            Text(
                                text = "${weather.temperature.toInt()}°C",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GramPrimary
                            )
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

            // ─── 2. Key Metric Quick Action Cards ──────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCard(
                        title = "सक्रिय मंडियां",
                        value = "18 Mandis",
                        subtitle = "LIVE",
                        icon = Icons.Default.Storefront,
                        iconBg = GramPrimaryLight,
                        iconTint = GramPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    KpiCard(
                        title = "टॉप तेजी (Garlic)",
                        value = "₹18,400",
                        subtitle = "DESI BOX",
                        icon = Icons.Default.TrendingUp,
                        iconBg = GramGreenLight,
                        iconTint = GramGreen,
                        valueColor = GramGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ─── 3. Instant Pincode & Village Navigator ─────────────────────────
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(
                    title = "📮 पिनकोड व डाकघर खोज",
                    subtitle = "पूरे भारत का पिनकोड डालें व तुरंत गांव-तहसील देखें"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = GramCardWhite),
                    border = BorderStroke(1.dp, GramBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = pincodeQuery,
                                onValueChange = {
                                    if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                        pincodeQuery = it
                                        if (it.length == 6) {
                                            isSearchingPincode = true
                                            coroutineScope.launch {
                                                pincodeResults = repository.fetchPincodeDetails(it)
                                                isSearchingPincode = false
                                            }
                                        }
                                    }
                                },
                                placeholder = { Text("6-अंक पिनकोड (e.g. 312001)", fontSize = 13.sp) },
                                leadingIcon = { Icon(Icons.Default.PinDrop, contentDescription = null, tint = GramPrimary) },
                                trailingIcon = {
                                    if (isSearchingPincode) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        if (pincodeResults.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "मिले परिणाम (${pincodeResults.size} डाकघर):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GramSlate
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            pincodeResults.take(3).forEach { po ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .background(GramBackground, RoundedCornerShape(6.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${po.postOfficeName} (${po.district})",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = GramNavy
                                    )
                                    StatusBadge(status = po.deliveryStatus, isPositive = true)
                                }
                            }
                        }
                    }
                }
            }

            // ─── 4. Live Mandi Rates Section with Filters ──────────────────────
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(
                    title = "🌾 आज के ताजा मंडी भाव",
                    subtitle = "चित्तौड़गढ़, निम्बाहेड़ा, कोटा व प्रतापगढ़ मंडी"
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
                MandiPriceCard(item = item)
            }
        }
    }
}

@Composable
fun MandiPriceCard(item: MandiItem) {
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
                    Text(
                        text = item.arrivalDate,
                        fontSize = 10.sp,
                        color = GramGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
