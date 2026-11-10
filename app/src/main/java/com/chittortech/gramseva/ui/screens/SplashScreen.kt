package com.chittortech.gramseva.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chittortech.gramseva.R
import com.chittortech.gramseva.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    val scale = remember { Animatable(0.9f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
        // Keep splash screen visible for 3 seconds so farmer reads everything comfortably
        delay(3000)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // ─── Center Hero Content ─────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                // 1. Pure Circular Farmer Logo
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(2.dp, Color(0xFFF1F5F9)),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .size(145.dp)
                        .scale(scale.value)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "GramSeva Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp)
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 2. Clear App Title
                Text(
                    text = "GramSeva",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GramNavy,
                    letterSpacing = 0.5.sp
                )

                Text(
                    text = "ग्राम-सेवा",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = GramPrimary,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "स्मार्ट ग्रामीण सूचना तंत्र",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = GramSlate,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Dignified Badge
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = GramBackground,
                    border = BorderStroke(1.dp, GramBorder)
                ) {
                    Text(
                        text = "🌾 अन्नदाता का सच्चा डिजिटल साथी 🌾",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GramNavy,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 4. Feature Service Badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SplashCleanPill(text = "लाइव मंडी भाव")
                    Text("•", color = GramBorder, fontSize = 12.sp)
                    SplashCleanPill(text = "मौसम रडार")
                    Text("•", color = GramBorder, fontSize = 12.sp)
                    SplashCleanPill(text = "सरकारी योजनाएं")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 5. Authentic Slogan
                Text(
                    text = "मेहनत किसान की • सही दाम और सुरक्षा का विश्वास ग्राम-सेवा का",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2E7D32), // Dignified Forest Green
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }

            // ─── Bottom Footer (Safe above navigation bar) ────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = "🇮🇳 भारत के किसानों को समर्पित • 100% निःशुल्क",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GramNavy,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "Agmarknet APMC • उपग्रह मौसम डेटा • आधिकारिक सूचना तंत्र",
                    fontSize = 10.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SplashCleanPill(text: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = GramBackground,
        border = BorderStroke(1.dp, GramBorder)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = GramNavy,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
