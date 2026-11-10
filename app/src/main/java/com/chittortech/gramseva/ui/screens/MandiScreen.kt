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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chittortech.gramseva.data.location.LocationProvider
import com.chittortech.gramseva.data.models.MandiItem
import com.chittortech.gramseva.data.repository.GramSevaRepository
import com.chittortech.gramseva.ui.components.SectionHeader
import com.chittortech.gramseva.ui.components.StatusBadge
import com.chittortech.gramseva.ui.theme.*
import com.chittortech.gramseva.util.ShareUtils
import kotlinx.coroutines.launch

@Composable
fun MandiScreen(
    repository: GramSevaRepository = remember { GramSevaRepository() }
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: भाव सूची, 1: अंतर-मंडी तुलना
    var searchQuery by remember { mutableStateOf("") }
    var selectedCropFilter by remember { mutableStateOf("सभी फसलें") }
    var selectedMandiFilter by remember { mutableStateOf("सभी मंडियां") }

    val mandiFilters = listOf("सभी मंडियां", "चित्तौड़गढ़", "निम्बाहेड़ा", "कपासन", "बेगूं", "बड़ी सादड़ी", "नीमच")
    val cropFilters = listOf("सभी फसलें", "लहसुन", "सोयाबीन", "गेहूं", "मक्का", "चना", "सरसों", "अजवाइन", "कपास", "मूंगफली")
    var allMandiItems by remember { mutableStateOf(repository.getMandiPrices()) }
    var isLiveFeed by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    // Fetch live Agmarknet prices via Cloudflare Edge
    LaunchedEffect(Unit) {
        isRefreshing = true
        allMandiItems = repository.fetchLiveMandiPrices(state = "Rajasthan")
        isLiveFeed = true
        isRefreshing = false
    }

    // Comparison crops
    val comparisonCrops = remember { repository.getComparisonCrops() }
    var selectedComparisonCrop by remember { mutableStateOf(comparisonCrops.first()) }
    val comparisonItems = remember(selectedComparisonCrop, allMandiItems) {
        repository.getMandiComparison(selectedComparisonCrop)
    }

    val filteredItems = remember(searchQuery, selectedCropFilter, selectedMandiFilter, allMandiItems) {
        allMandiItems.filter { item ->
            val matchesCrop = if (selectedCropFilter == "सभी फसलें") true else item.commodity.contains(selectedCropFilter, ignoreCase = true)
            val matchesMandi = if (selectedMandiFilter == "सभी मंडियां") true else item.market.contains(selectedMandiFilter, ignoreCase = true)
            val matchesSearch = if (searchQuery.isBlank()) true else {
                item.commodity.contains(searchQuery, ignoreCase = true) ||
                        item.market.contains(searchQuery, ignoreCase = true) ||
                        item.variety.contains(searchQuery, ignoreCase = true) ||
                        item.district.contains(searchQuery, ignoreCase = true)
            }
            matchesCrop && matchesMandi && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GramBackground)
    ) {
        // ─── Header & Top Action Bar ─────────────────────────────────────────
        Surface(
            color = GramCardWhite,
            border = BorderStroke(1.dp, GramBorder),
            shadowElevation = 1.5.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "मंडी भाव रडार",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = GramNavy
                            )

                            // Compact fixed-size Live Badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isLiveFeed) GramGreenLight else GramPrimaryLight,
                                border = BorderStroke(1.dp, if (isLiveFeed) GramGreen else GramPrimary)
                            ) {
                                Text(
                                    text = if (isLiveFeed) "🟢 लाइव APMC" else "ऑफलाइन डेटा",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLiveFeed) GramGreen else GramPrimaryDark,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Agmarknet • चित्तौड़गढ़ कृषि उपज मंडियां",
                            fontSize = 11.sp,
                            color = GramSlate
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Refresh button
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    isRefreshing = true
                                    allMandiItems = repository.fetchLiveMandiPrices(state = "Rajasthan")
                                    isLiveFeed = true
                                    isRefreshing = false
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            if (isRefreshing) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = GramPrimary)
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = GramPrimary)
                            }
                        }

                        // WhatsApp Share Button
                        Button(
                            onClick = { ShareUtils.shareFullMandiBulletin(context, allMandiItems) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF25D366),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("शेयर", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sub-tabs: भाव सूची vs तुलना
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = GramBackground,
                    contentColor = GramPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = GramPrimary
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "🌾 भाव सूची (${filteredItems.size})",
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "⚖️ अंतर-मंडी तुलना",
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }
        }

        // ─── TAB 0: MANDI RATES LIST ─────────────────────────────────────────
        if (selectedTab == 0) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                // Search Input & Filter Chips
                item {
                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("फसल या मंडी खोजें (e.g. लहसुन, नीमच, गेहूं)", fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GramPrimary) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = GramSlate)
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = GramCardWhite,
                                unfocusedContainerColor = GramCardWhite,
                                focusedBorderColor = GramPrimary,
                                unfocusedBorderColor = GramBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Chittorgarh District Mandi Filter Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            mandiFilters.forEach { mkt ->
                                val isSelected = selectedMandiFilter == mkt
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) GramPrimary else GramBackground,
                                    border = BorderStroke(1.dp, if (isSelected) GramPrimary else GramBorder),
                                    modifier = Modifier.clickable { selectedMandiFilter = mkt }
                                ) {
                                    Text(
                                        text = mkt,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else GramNavy,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Chittorgarh Local Cash Crop Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            cropFilters.forEach { crop ->
                                val isSelected = selectedCropFilter == crop
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCropFilter = crop },
                                    label = { Text(crop, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GramGold,
                                        selectedLabelColor = Color.White,
                                        containerColor = GramCardWhite,
                                        labelColor = TextSecondary
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        borderColor = if (isSelected) GramGold else GramBorder,
                                        enabled = true,
                                        selected = isSelected
                                    )
                                )
                            }
                        }
                    }
                }

                // Rates Items List
                items(filteredItems) { item ->
                    MandiDetailedCard(
                        item = item,
                        onShareClick = { ShareUtils.shareMandiItem(context, item) }
                    )
                }

                if (filteredItems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "कोई परिणाम नहीं मिला। कृपया अन्य फसल या मंडी खोजें।",
                                fontSize = 13.sp,
                                color = GramSlate
                            )
                        }
                    }
                }
            }
        }

        // ─── TAB 1: CROSS-MANDI COMPARISON ───────────────────────────────────
        if (selectedTab == 1) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    Text(
                        text = "तुलना हेतु फसल चुनें:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GramNavy
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        comparisonCrops.forEach { crop ->
                            val isSelected = selectedComparisonCrop == crop
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedComparisonCrop = crop },
                                label = { Text(crop, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GramGold,
                                    selectedLabelColor = Color.White,
                                    containerColor = GramCardWhite,
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (isSelected) GramGold else GramBorder,
                                    enabled = true,
                                    selected = isSelected
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Comparison Insights Banner
                    val highest = comparisonItems.maxByOrNull { it.modalPrice }
                    val lowest = comparisonItems.minByOrNull { it.modalPrice }
                    if (highest != null && lowest != null && highest.market != lowest.market) {
                        val diff = highest.modalPrice - lowest.modalPrice
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = GramGoldLight),
                            border = BorderStroke(1.dp, GramGold.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = GramGold, modifier = Modifier.size(24.dp))
                                Column {
                                    Text(
                                        text = "सर्वोत्तम भाव: ${highest.market} (₹${highest.modalPrice}/क्विंटल)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GramNavy
                                    )
                                    Text(
                                        text = "${lowest.market} की तुलना में +₹$diff प्रति क्विंटल अधिक मुनाफा!",
                                        fontSize = 11.sp,
                                        color = GramPrimaryDark,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "सभी प्रमुख मंडियों में आज के भाव:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GramNavy
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                items(comparisonItems) { item ->
                    MandiComparisonRow(item = item, highestPrice = comparisonItems.firstOrNull()?.modalPrice ?: 0)
                }
            }
        }
    }
}

@Composable
fun MandiDetailedCard(
    item: MandiItem,
    onShareClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GramCardWhite),
        border = BorderStroke(0.5.dp, GramBorder),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Header Row: Crop Name, Badges, WhatsApp Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.commodity,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GramNavy,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.isHotCrop) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = GramGoldLight,
                            border = BorderStroke(0.5.dp, GramGold)
                        ) {
                            Text(
                                text = "तेज मांग 🔥",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GramGold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.5.dp)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StatusBadge(status = item.trend)

                    // Compact Circular WhatsApp Share
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF25D366).copy(alpha = 0.12f))
                            .clickable { onShareClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share",
                            tint = Color(0xFF25D366),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Subtitle: Market & Variety
            Text(
                text = "${item.market} • ${item.variety}",
                fontSize = 11.sp,
                color = GramSlate,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Stats Bottom Bar: Clean typography without nested box-in-a-box
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "₹${item.modalPrice}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GramPrimary
                    )
                    Text(
                        text = "/क्विंटल",
                        fontSize = 11.sp,
                        color = GramSlate,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "रेंज: ₹${item.minPrice} - ₹${item.maxPrice}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    if (item.grade.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = GramGreenLight
                        ) {
                            Text(
                                text = item.grade,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = GramGreen,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MandiComparisonRow(item: MandiItem, highestPrice: Int) {
    val isBest = item.modalPrice == highestPrice
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isBest) GramSurfaceVariant else GramCardWhite
        ),
        border = BorderStroke(if (isBest) 1.dp else 0.5.dp, if (isBest) GramGold else GramBorder),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = item.market,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = GramNavy
                    )
                    if (isBest) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = GramGold
                        ) {
                            Text(
                                "सर्वोच्च भाव",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                val infoSub = if (item.arrivalsTons != null && item.arrivalsTons > 0) {
                    "${item.district} • आवक ~${item.arrivalsTons.toInt()} टन"
                } else {
                    "${item.district} • ग्रेड ${item.grade}"
                }
                Text(
                    text = infoSub,
                    fontSize = 11.sp,
                    color = GramSlate
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${item.modalPrice}",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isBest) GramPrimaryDark else GramNavy
                )
                Text(
                    text = "रेंज: ₹${item.minPrice} - ₹${item.maxPrice}",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
