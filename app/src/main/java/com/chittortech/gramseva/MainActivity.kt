package com.chittortech.gramseva

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.chittortech.gramseva.ui.screens.MainScreen
import com.chittortech.gramseva.ui.screens.SplashScreen
import com.chittortech.gramseva.ui.theme.GramSevaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GramSevaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showSplash by remember { mutableStateOf(true) }

                    Crossfade(
                        targetState = showSplash,
                        animationSpec = tween(500),
                        label = "SplashCrossfade"
                    ) { isSplash ->
                        if (isSplash) {
                            SplashScreen(onTimeout = { showSplash = false })
                        } else {
                            MainScreen()
                        }
                    }
                }
            }
        }
    }
}
