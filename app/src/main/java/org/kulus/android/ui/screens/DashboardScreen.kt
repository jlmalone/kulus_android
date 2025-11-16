package org.kulus.android.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * DashboardScreen - Main app screen with tab navigation
 *
 * Provides bottom navigation between:
 * - Today: Latest reading and recent readings
 * - History: Full list of all readings
 * - Trends: Charts and analytics (placeholder for Phase 2)
 * - Settings: App preferences and configuration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onAddClick: () -> Unit = {},
    onReadingClick: (String) -> Unit = {},
    onSignedOut: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(DashboardTab.TODAY) }

    Scaffold(
        topBar = {
            when (selectedTab) {
                DashboardTab.TODAY, DashboardTab.HISTORY, DashboardTab.TRENDS -> {
                    TopAppBar(
                        title = { Text("Kulus") },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
                DashboardTab.SETTINGS -> {
                    // Settings screen has its own top bar
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                DashboardTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title
                            )
                        },
                        label = { Text(tab.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            // Show FAB only on Today and History tabs
            if (selectedTab == DashboardTab.TODAY || selectedTab == DashboardTab.HISTORY) {
                FloatingActionButton(
                    onClick = onAddClick,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Reading",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            DashboardTab.TODAY -> {
                TodayScreen(
                    modifier = Modifier.padding(paddingValues),
                    onAddClick = onAddClick,
                    onReadingClick = onReadingClick
                )
            }

            DashboardTab.HISTORY -> {
                ReadingsListScreen(
                    modifier = Modifier.padding(paddingValues),
                    onAddClick = onAddClick,
                    onReadingClick = onReadingClick,
                    onSettingsClick = { selectedTab = DashboardTab.SETTINGS },
                    hideTopBar = true,
                    hideFab = true
                )
            }

            DashboardTab.TRENDS -> {
                TrendsScreen(modifier = Modifier.padding(paddingValues))
            }

            DashboardTab.SETTINGS -> {
                SettingsScreen(
                    onBackClick = { selectedTab = DashboardTab.TODAY },
                    onSignedOut = onSignedOut,
                    hideTopBar = true
                )
            }
        }
    }
}

enum class DashboardTab(val title: String, val icon: ImageVector) {
    TODAY("Today", Icons.Default.Home),
    HISTORY("History", Icons.Default.List),
    TRENDS("Trends", Icons.Default.ShowChart),
    SETTINGS("Settings", Icons.Default.Settings)
}
