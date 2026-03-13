package org.kulus.android.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * DashboardScreen - iOS-style main app screen with tab navigation
 *
 * Matches iOS MainAppView tab structure with:
 * - Dashboard (Today): Latest reading and recent readings
 * - Add Reading: Form to add new readings
 * - History: Full list with filters and stats
 * - Settings: App preferences
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onAddClick: () -> Unit = {},
    onReadingClick: (String) -> Unit = {},
    onSignedOut: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    onApiLogClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(DashboardTab.TODAY) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                tonalElevation = 8.dp
            ) {
                DashboardTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == tab) tab.selectedIcon else tab.icon,
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
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp)
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
                TrendsScreen(Modifier.padding(paddingValues))
            }

            DashboardTab.SETTINGS -> {
                SettingsScreen(
                    onBackClick = { selectedTab = DashboardTab.TODAY },
                    onSignedOut = onSignedOut,
                    onHelpClick = onHelpClick,
                    onApiLogClick = onApiLogClick,
                    hideTopBar = true
                )
            }
        }
    }
}

enum class DashboardTab(
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    TODAY("Dashboard", Icons.Outlined.Home, Icons.Filled.Home),
    HISTORY("History", Icons.Outlined.List, Icons.Filled.List),
    TRENDS("Trends", Icons.Outlined.ShowChart, Icons.Filled.ShowChart),
    SETTINGS("Settings", Icons.Outlined.Settings, Icons.Filled.Settings)
}
