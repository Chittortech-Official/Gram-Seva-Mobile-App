package com.chittortech.gramseva.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chittortech.gramseva.data.repository.GramSevaRepository
import com.chittortech.gramseva.ui.components.GramTopBar
import com.chittortech.gramseva.ui.theme.*

enum class GramNavTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("गृह", Icons.Filled.Home, Icons.Outlined.Home),
    MANDI("मंडी भाव", Icons.Filled.Storefront, Icons.Outlined.Storefront),
    WEATHER("मौसम रडार", Icons.Filled.CloudQueue, Icons.Outlined.CloudQueue),
    YOJANA("सरकारी योजना", Icons.Filled.AccountBalance, Icons.Outlined.AccountBalance)
}

@Composable
fun MainScreen(
    repository: GramSevaRepository = remember { GramSevaRepository() }
) {
    var currentTab by rememberSaveable { mutableStateOf(GramNavTab.HOME) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = GramCardWhite,
                tonalElevation = 6.dp
            ) {
                GramNavTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GramPrimary,
                            selectedTextColor = GramPrimaryDark,
                            indicatorColor = GramPrimaryLight,
                            unselectedIconColor = GramSlate,
                            unselectedTextColor = GramSlate
                        )
                    )
                }
            }
        },
        containerColor = GramBackground
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = GramBackground
        ) {
            when (currentTab) {
                GramNavTab.HOME -> HomeScreen(
                    repository = repository,
                    onNavigateToTab = { targetIndex ->
                        currentTab = GramNavTab.entries.getOrElse(targetIndex) { GramNavTab.HOME }
                    }
                )
                GramNavTab.MANDI -> MandiScreen(repository = repository)
                GramNavTab.WEATHER -> WeatherScreen(repository = repository)
                GramNavTab.YOJANA -> YojanaScreen(repository = repository)
            }
        }
    }
}

