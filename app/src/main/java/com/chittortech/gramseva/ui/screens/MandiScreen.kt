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
    var selectedCropFilter by remember { mutableStateOf("सभी") }

    val cropFilters = listOf("सभी", "लहसुन", "प्याज", "गेहूं", "सरसों", "सोयाबीन", "चना", "मक्का", "तिल")
    var allMandiItems by remember { mutableStateOf(repository.getMandiPrices()) }
    var isLiveFeed by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    // Fetch live government Agmarknet prices on screen launch
    LaunchedEffect(Unit) {
        allMandiItems = repository.fetchLiveMandiPrices()
        isLiveFeed = true
    }

    // Comparison crops
    val comparisonCrops = remember { repository.getComparisonCrops() }
    var selectedComparisonCrop by remember { mutableStateOf(comparisonCrops.first()) }
    val comparisonItems = remember(selectedComparisonCrop, allMandiItems) {
        repository.getMandiComparison(selectedComparisonCrop)
    }

    val filteredItems = remember(searchQuery, selectedCropFilter, allMandiItems) {
        allMandiItems.filter { item ->
            val matchesCrop = if (selectedCropFilter == "सभी") true else item.commodity.contains(selectedCropFilter, ignoreCase = true)
            val matchesSearch = if (searchQuery.isBlank()) true else {
                item.commodity.contains(searchQuery, ignoreCase = true) ||
                        item.market.contains(searchQuery, ignoreCase = true) ||
                        item.variety.contains(searchQuery, ignoreCase = true) ||
                        item.district.contains(searchQuery, ignoreCase = true)
            }
            matchesCrop && matchesSearch
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
                                    text = if (isLiveFeed) "LIVE OGD" else "OFFLINE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLiveFeed) GramGreen else GramPrimaryDark,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Agmarknet भारत सरकार • राजस्थान व एमपी मंडियां",
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
                                    allMandiItems = repository.fetchLiveMandiPrices()
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
                contentPadding = PaddingValues(bottom = 28.dp)
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

                        // Category Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp),
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
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = GramCardWhite),
        border = BorderStroke(1.dp, GramBorder),
        elevation = CardDefaults.cardElevation(1.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Commodity + Badges
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
                        color = GramNavy
                    )
                    if (item.isHotCrop) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = GramGoldLight,
                            border = BorderStroke(0.5.dp, GramGold)
                        ) {
                            Text(
                                text = "HOT 🔥",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GramGold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                StatusBadge(status = item.trend)
            }

            Text(
                text = "${item.market} (${item.state}) • ${item.variety}",
                fontSize = 12.sp,
                color = GramSlate
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = GramBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Price Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "मॉडल भाव",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "₹${item.modalPrice}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GramPrimary
                        )
                        Text(
                            text = " /क्विंटल",
                            fontSize = 11.sp,
                            color = GramSlate,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "रेंज: ₹${item.minPrice} - ₹${item.maxPrice}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GramNavy
                    )
                    Text(
                        text = "आवक: ~${item.arrivalsTons} टन • ${item.arrivalDate}",
                        fontSize = 10.sp,
                        color = GramGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // WhatsApp Share Pill Button
            OutlinedButton(
                onClick = onShareClick,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFF25D366).copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E7E34)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.Share, contentDescription = null, tint = Color(0xFF25D366), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "यह भाव व्हाट्सएप पर शेयर करें",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
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
        border = BorderStroke(if (isBest) 1.5.dp else 1.dp, if (isBest) GramGold else GramBorder)
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
                Text(
                    text = "${item.district} (${item.state}) • आवक: ~${item.arrivalsTons} टन",
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
