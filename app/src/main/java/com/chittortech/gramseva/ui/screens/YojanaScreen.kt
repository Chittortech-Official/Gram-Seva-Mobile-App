package com.chittortech.gramseva.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chittortech.gramseva.data.models.PincodeRecord
import com.chittortech.gramseva.data.models.YojanaItem
import com.chittortech.gramseva.data.repository.GramSevaRepository
import com.chittortech.gramseva.ui.components.SectionHeader
import com.chittortech.gramseva.ui.components.StatusBadge
import com.chittortech.gramseva.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun YojanaScreen(
    repository: GramSevaRepository = remember { GramSevaRepository() }
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: सरकारी योजनाएं, 1: पिनकोड व गांव

    // Yojana state
    val allYojanas = remember { repository.getSarkariYojanas() }
    var selectedCategory by remember { mutableStateOf("सभी") }
    val categories = listOf("सभी", "किसान", "सब्सिडी", "बीमा")
    var yojanaSearchQuery by remember { mutableStateOf("") }

    val filteredYojanas = remember(selectedCategory, yojanaSearchQuery) {
        allYojanas.filter { item ->
            val matchesCategory = if (selectedCategory == "सभी") true else item.category.contains(selectedCategory, ignoreCase = true)
            val matchesSearch = if (yojanaSearchQuery.isBlank()) true else {
                item.titleHindi.contains(yojanaSearchQuery, ignoreCase = true) ||
                        item.titleEnglish.contains(yojanaSearchQuery, ignoreCase = true) ||
                        item.eligibility.contains(yojanaSearchQuery, ignoreCase = true)
            }
            matchesCategory && matchesSearch
        }
    }

    // Pincode state
    var pincodeQuery by remember { mutableStateOf("312001") }
    var pincodeResults by remember { mutableStateOf(emptyList<PincodeRecord>()) }
    var isSearchingPincode by remember { mutableStateOf(false) }

    val popularPincodes = listOf(
        "312001" to "चित्तौड़गढ़",
        "312601" to "निम्बाहेड़ा",
        "312202" to "कपासन",
        "312023" to "बेंगू",
        "458441" to "नीमच (MP)",
        "324001" to "कोटा"
    )

    // Initial search for default pincode
    LaunchedEffect(Unit) {
        pincodeResults = repository.fetchPincodeDetails("312001")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GramBackground)
    ) {
        // ─── Header & Top Tab Switch ─────────────────────────────────────────
        Surface(
            color = GramCardWhite,
            border = BorderStroke(1.dp, GramBorder),
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ग्रामीण सुविधाएं व योजना रडार",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GramNavy
                )
                Text(
                    text = "सरकारी योजनाएं • पात्रता जांच • डाकघर व पिनकोड निर्देशिका",
                    fontSize = 11.sp,
                    color = GramSlate
                )

                Spacer(modifier = Modifier.height(12.dp))

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
                                "🏛️ सरकारी योजनाएं (${allYojanas.size})",
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
                                "📮 पिनकोड व गांव खोज",
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }
        }

        // ─── TAB 0: SARKARI YOJANAS ───────────────────────────────────────────
        if (selectedTab == 0) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Search bar
                        OutlinedTextField(
                            value = yojanaSearchQuery,
                            onValueChange = { yojanaSearchQuery = it },
                            placeholder = { Text("योजना खोजें (e.g. तारबंदी, सोलर, सम्मान निधि)", fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GramPrimary) },
                            trailingIcon = {
                                if (yojanaSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { yojanaSearchQuery = "" }) {
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
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Category chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categories.forEach { cat ->
                                val isSelected = selectedCategory == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
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

                items(filteredYojanas) { yojana ->
                    YojanaInteractiveCard(yojana = yojana)
                }
            }
        }

        // ─── TAB 1: PINCODE & POST OFFICE DIRECTORY ───────────────────────────
        if (selectedTab == 1) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp)
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = GramCardWhite),
                        border = BorderStroke(1.dp, GramBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "भारतीय डाक (India Post) पिनकोड सर्च",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = GramNavy
                            )
                            Text(
                                text = "6 अंकों का पिनकोड दर्ज कर गांव, तहसील व डाकघर जानें",
                                fontSize = 11.sp,
                                color = GramSlate
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = pincodeQuery,
                                onValueChange = {
                                    if (it.length <= 6 && it.all { ch -> ch.isDigit() }) {
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
                                    } else if (pincodeQuery.length == 6) {
                                        IconButton(onClick = {
                                            coroutineScope.launch {
                                                isSearchingPincode = true
                                                pincodeResults = repository.fetchPincodeDetails(pincodeQuery)
                                                isSearchingPincode = false
                                            }
                                        }) {
                                            Icon(Icons.Default.Search, contentDescription = "Search", tint = GramPrimary)
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "त्वरित पिनकोड चयन:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GramSlate
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                popularPincodes.forEach { (pin, name) ->
                                    val isCurrent = pincodeQuery == pin
                                    SuggestionChip(
                                        onClick = {
                                            pincodeQuery = pin
                                            isSearchingPincode = true
                                            coroutineScope.launch {
                                                pincodeResults = repository.fetchPincodeDetails(pin)
                                                isSearchingPincode = false
                                            }
                                        },
                                        label = { Text("$pin ($name)", fontSize = 11.sp) },
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = if (isCurrent) GramPrimaryLight else GramBackground
                                        ),
                                        border = SuggestionChipDefaults.suggestionChipBorder(
                                            borderColor = if (isCurrent) GramPrimary else GramBorder,
                                            enabled = true
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "मिले डाकघर परिणाम (${pincodeResults.size}):",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GramNavy
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (pincodeResults.isEmpty() && !isSearchingPincode) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(30.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "इस पिनकोड के लिए कोई डाकघर नहीं मिला।",
                                fontSize = 13.sp,
                                color = GramSlate
                            )
                        }
                    }
                }

                items(pincodeResults) { po ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = GramCardWhite),
                        border = BorderStroke(1.dp, GramBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = po.postOfficeName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GramNavy
                                )
                                Text(
                                    text = "${po.district}, ${po.state} • ${po.division}",
                                    fontSize = 12.sp,
                                    color = GramSlate
                                )
                                Text(
                                    text = "पिनकोड: ${po.pincode}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GramPrimary
                                )
                            }

                            StatusBadge(status = po.deliveryStatus, isPositive = true)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun YojanaInteractiveCard(yojana: YojanaItem) {
    var isExpanded by remember { mutableStateOf(false) }

    // Dynamic checklist state for interactive eligibility
    val checkedMap = remember(yojana.id) {
        mutableStateMapOf<Int, Boolean>()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = GramCardWhite),
        border = BorderStroke(1.dp, if (isExpanded) GramPrimary.copy(alpha = 0.5f) else GramBorder),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = yojana.titleHindi,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GramNavy
                    )
                    Text(
                        text = yojana.titleEnglish,
                        fontSize = 11.sp,
                        color = GramSlate
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = GramGoldLight,
                    border = BorderStroke(1.dp, GramGold.copy(alpha = 0.4f)),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = yojana.category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GramNavy,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Benefit Amount Callout Box
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = GramPrimaryLight,
                border = BorderStroke(1.dp, GramPrimary.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = GramPrimaryDark, modifier = Modifier.size(20.dp))
                    Column {
                        Text("लाभ / अनुदान राशि:", fontSize = 10.sp, color = GramPrimaryDark)
                        Text(yojana.benefitAmount, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GramPrimaryDark)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "पात्रता: ${yojana.eligibility}",
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )

            // Expandable details (Checklist & Documents)
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = GramBorder)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "📋 आवश्यक दस्तावेज चेकलिस्ट (जांचें):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GramNavy
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    yojana.documentsRequired.forEachIndexed { idx, doc ->
                        val isChecked = checkedMap[idx] ?: false
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { checkedMap[idx] = !isChecked }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checkedMap[idx] = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = GramPrimary,
                                    checkmarkColor = Color.White
                                )
                            )
                            Text(
                                text = doc,
                                fontSize = 12.sp,
                                fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal,
                                color = if (isChecked) GramGreen else GramNavy
                            )
                        }
                    }

                    // Check result banner
                    val totalDocs = yojana.documentsRequired.size
                    val checkedCount = checkedMap.values.count { it }
                    if (checkedCount == totalDocs && totalDocs > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GramGreenLight,
                            border = BorderStroke(1.dp, GramGreen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GramGreen)
                                Text(
                                    "बधाई! आपके पास सभी दस्तावेज उपलब्ध हैं। आप आवेदन के लिए तैयार हैं।",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GramNavy
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Portal & Helpline
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "आवेदन केंद्र: ${yojana.portalName}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GramSlate
                        )
                    }
                    Text(
                        text = "सहायता हेल्पलाइन: ${yojana.helpline}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GramPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Expand / Collapse indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpanded) "कम देखें ▲" else "दस्तावेज व पात्रता चेकलिस्ट देखें ▼",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GramPrimary
                )
            }
        }
    }
}

