package com.chittortech.gramseva.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chittortech.gramseva.ui.theme.*

// ─── Status Badge ─────────────────────────────────────────────────────────────
@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier,
    isPositive: Boolean? = null
) {
    val (bg, fg) = when {
        isPositive == true -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        isPositive == false -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        status.contains("तेज", ignoreCase = true) || status.contains("+") || status.contains("HIGH", ignoreCase = true) -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        status.contains("मंदा", ignoreCase = true) || status.contains("-") || status.contains("LOW", ignoreCase = true) -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        status.contains("अलर्ट", ignoreCase = true) || status.contains("ALERT", ignoreCase = true) -> Color(0xFFFEF3C7) to Color(0xFFB45309)
        else -> Color(0xFFF1F5F9) to Color(0xFF475569)
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bg,
        modifier = modifier
    ) {
        Text(
            text = status,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = fg,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
        )
    }
}

// ─── KPI Metric Card ──────────────────────────────────────────────────────────
@Composable
fun KpiCard(
    title: String,
    value: String,
    subtitle: String = "",
    icon: ImageVector,
    cardBg: Color = GramCardWhite,
    borderColor: Color = GramBorder,
    iconBg: Color = GramPrimaryLight,
    iconTint: Color = GramPrimary,
    valueColor: Color = TextPrimary,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                }

                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = title,
                fontSize = 12.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ─── GramTopBar (Custom Executive Header) ─────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GramTopBar(
    title: String = "ग्राम-सेवा",
    subtitle: String = "चित्तौड़गढ़ ज़िला • किसान साथी",
    locationText: String? = null,
    onLocationClick: (() -> Unit)? = null,
    onNotificationClick: (() -> Unit)? = null,
    onRefreshClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        color = GramCardWhite,
        shadowElevation = 1.dp,
        border = BorderStroke(0.5.dp, Color(0xFFF1F5F9)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Farmer Logo Medallion
                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = Color.White,
                    shadowElevation = 1.dp,
                    modifier = Modifier.size(38.dp)
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.chittortech.gramseva.R.drawable.app_logo),
                        contentDescription = "GramSeva Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(androidx.compose.foundation.shape.CircleShape)
                    )
                }

                Column {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GramNavy,
                        maxLines = 1
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = GramSlate,
                        maxLines = 1
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onLocationClick != null && !locationText.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.clickable { onLocationClick() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = GramPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = locationText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GramNavy,
                                maxLines = 1
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = GramSlate,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                if (onRefreshClick != null) {
                    IconButton(
                        onClick = onRefreshClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = GramSlate,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─── Section Header ───────────────────────────────────────────────────────────
@Composable
fun SectionHeader(
    title: String,
    subtitle: String = "",
    actionText: String = "",
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = GramNavy
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = GramSlate
                )
            }
        }

        if (actionText.isNotEmpty() && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GramPrimary
                )
            }
        }
    }
}

