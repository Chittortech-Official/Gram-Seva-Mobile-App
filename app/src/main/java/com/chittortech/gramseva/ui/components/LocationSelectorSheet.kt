package com.chittortech.gramseva.ui.components

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chittortech.gramseva.data.location.LocationProvider
import com.chittortech.gramseva.data.models.UserLocation
import com.chittortech.gramseva.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSelectorSheet(
    currentLocation: UserLocation,
    onLocationSelected: (UserLocation) -> Unit,
    onDetectGpsClick: () -> Unit,
    isDetectingGps: Boolean = false,
    onDismissRequest: () -> Unit
) {
    var selectedState by remember { mutableStateOf(currentLocation.state) }
    val displayLocations = remember(selectedState) {
        LocationProvider.getLocationsForState(selectedState)
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = GramCardWhite,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GramPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = GramPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "स्थान व जिला चुनें",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = GramNavy
                        )
                        Text(
                            text = "पूरे भारत की मंडियां व मौसम देखने हेतु चुनें",
                            fontSize = 11.sp,
                            color = GramSlate
                        )
                    }
                }

                IconButton(onClick = onDismissRequest) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = GramSlate)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ─── Auto-Detect GPS Button ───────────────────────────────────────
            Button(
                onClick = onDetectGpsClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GramGreen,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                if (isDetectingGps) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "लाइव GPS खोज रहे हैं...", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "मेरी लाइव लोकेशन डिटेक्ट करें (Auto GPS)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ─── State Selector Chips ─────────────────────────────────────────
            Text(
                text = "1. राज्य चुनें (Select State):",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = GramNavy
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LocationProvider.supportedStates.forEach { stateKey ->
                    val isSelected = selectedState.equals(stateKey, ignoreCase = true)
                    val label = LocationProvider.stateDisplayNameMap[stateKey] ?: stateKey

                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedState = stateKey },
                        label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GramPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = GramBackground,
                            labelColor = GramNavy
                        ),
                        border = BorderStroke(1.dp, if (isSelected) GramPrimary else GramBorder)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ─── District & City Chips ────────────────────────────────────────
            Text(
                text = "2. जिला व कृषि केंद्र चुनें:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = GramNavy
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(displayLocations) { loc ->
                    val isCurrent = currentLocation.nameHindi == loc.nameHindi && currentLocation.state == loc.state

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onLocationSelected(loc)
                                onDismissRequest()
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isCurrent) GramPrimary.copy(alpha = 0.08f) else GramBackground,
                        border = BorderStroke(1.dp, if (isCurrent) GramPrimary else GramBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 11.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "${loc.nameHindi} (${loc.nameEnglish})",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrent) GramPrimary else GramNavy
                                    )
                                    if (isCurrent) {
                                        Surface(
                                            color = GramPrimary,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "सक्रिय",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "जिला: ${loc.district} • राज्य: ${LocationProvider.stateDisplayNameMap[loc.state] ?: loc.state}",
                                    fontSize = 11.sp,
                                    color = GramSlate
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.GpsFixed,
                                contentDescription = null,
                                tint = if (isCurrent) GramPrimary else GramSlate.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

