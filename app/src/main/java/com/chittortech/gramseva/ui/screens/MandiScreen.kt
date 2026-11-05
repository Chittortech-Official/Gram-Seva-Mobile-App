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

@Composable
fun MandiScreen(
    repository: GramSevaRepository = remember { GramSevaRepository() }
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: भाव सूची, 1: अंतर-मंडी तुलना
    var searchQuery by remember { mutableStateOf("") }
    var selectedCropFilter by remember { mutableStateOf("सभी") }

    val cropFilters = listOf("सभी", "लहसुन", "प्याज", "गेहूं", "सरसों", "सोयाबीन", "चना", "मक्का", "इसबगोल")
    val allMandiItems = remember { repository.getMandiPrices() }

    // Comparison crops
    val comparisonCrops = remember { repository.getComparisonCrops() }
    var selectedComparisonCrop by remember { mutableStateOf(comparisonCrops.first()) }
    val comparisonItems = remember(selectedComparisonCrop) {
        repository.getMandiComparison(selectedComparisonCrop)
    }

    val filteredItems = remember(searchQuery, selectedCropFilter) {
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
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "मंडी भाव रडार (Mandi Rates)",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = GramNavy
                        )
                        Text(
                            text = "राजस्थान व मध्य प्रदेश प्रमुख कृषि उपज मंडियां",
                            fontSize = 11.sp,
                            color = GramSlate
                        )
                    }

                    // Share WhatsApp Bulletin Button
                    Button(
                        onClick = { ShareUtils.shareFullMandiBulletin(context, allMandiItems) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("व्हाट्सएप शेयर", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

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
                                "🌾 मंडी भाव सूची (${filteredItems.size})",
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
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Search Input & Filter Chips
                item {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        // Search Outlined Box
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

                        Spacer(modifier = Modifier.height(10.dp))

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
                                "कोई परिणाम नहीं मिला। कृपया अन्य शब्द खोजें।",
                                fontSize = 14.sp,
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
                        text = "फसल का चयन करें:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GramNavy
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Crop Selection Horizontal Bar
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

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "सभी मंडियों में भाव तुलना:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GramNavy
                    )
                }

                // Comparison Cards for each Mandi
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
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = GramCardWhite),
        border = BorderStroke(1.dp, GramBorder),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
                                    "HOT 🔥",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GramGold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${item.market} (${item.state}) • ${item.variety}",
                        fontSize = 12.sp,
                        color = GramSlate
                    )
                }

                StatusBadge(status = item.trend)
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = GramBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "मॉडल भाव (Modal Rate)",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text(
                        text = "₹${item.modalPrice}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GramPrimary
                    )
                    Text(
                        text = "₹/क्विंटल (Per Qtl)",
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
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GramNavy
                    )
                    Text(
                        text = "दैनिक आवक: ~${item.arrivalsTons} टन",
                        fontSize = 11.sp,
                        color = GramGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // WhatsApp Share Pill Button
            OutlinedButton(
                onClick = onShareClick,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFF25D366).copy(alpha = 0.6f)),
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
            .padding(vertical = 5.dp),
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
                            color = GramGold,
                            modifier = Modifier.padding(start = 4.dp)
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
                    fontSize = 20.sp,
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

