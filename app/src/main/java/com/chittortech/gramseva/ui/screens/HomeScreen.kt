package com.chittortech.gramseva.ui.screens

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
import com.chittortech.gramseva.data.models.MandiItem
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
    val cropFilters = listOf("सभी फसलें", "लहसुन", "प्याज", "गेहूं", "सरसों", "सोयाबीन", "मक्का", "चना")

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
                subtitle = "चित्तौड़गढ़ व राजस्थान • स्मार्ट ग्रामीण सूचना तंत्र",
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
                        .padding(horizontal = 16.dp, vertical = 12.dp)
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
                                        text = "${weather.condition} • 7 दिन पूर्वानुमान →",
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
                        title = "सक्रिय मंडियां",
                        value = "18 Mandis",
                        subtitle = "LIVE",
                        icon = Icons.Default.Storefront,
                        iconBg = GramPrimaryLight,
                        iconTint = GramPrimary,
                        onClick = { onNavigateToTab(1) },
                        modifier = Modifier.weight(1f)
                    )

                    KpiCard(
                        title = "लहसुन सर्वोच्च भाव",
                        value = "₹21,500",
                        subtitle = "NEEMUCH",
                        icon = Icons.Default.TrendingUp,
                        iconBg = GramGreenLight,
                        iconTint = GramGreen,
                        valueColor = GramGreen,
                        onClick = { onNavigateToTab(1) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ─── 4. Quick Pincode Search ──────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(
                    title = "📮 पिनकोड व डाकघर खोज",
                    subtitle = "पूरे भारत का पिनकोड डालें व तुरंत गांव-तहसील देखें",
                    actionText = "विस्तृत खोज →",
                    onActionClick = { onNavigateToTab(3) }
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

            // ─── 5. Live Mandi Rates Section ──────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(
                    title = "🌾 आज के ताजा मंडी भाव",
                    subtitle = "चित्तौड़गढ़, निम्बाहेड़ा, कोटा, नीमच व प्रतापगढ़",
                    actionText = "सभी तुलना देखें →",
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

            // ─── 6. Mandi Price Cards List ─────────────────────────────────────
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
