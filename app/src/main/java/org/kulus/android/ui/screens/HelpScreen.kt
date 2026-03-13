package org.kulus.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Help & FAQ screen matching iOS HelpView.swift.
 * Provides glucose education content, getting started guide, and app information.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onBackClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help & FAQ") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Getting Started
            HelpSection(title = "Getting Started") {
                HelpItem(
                    title = "Adding Readings",
                    content = "There are three ways to add glucose readings:\n\n" +
                            "1. Manual Entry: Tap the + button and enter your glucose value directly.\n\n" +
                            "2. Photo Capture: Use the camera to photograph your meter display. The app uses OCR to extract the glucose value automatically.\n\n" +
                            "3. Bluetooth: If you have a Contour Next One meter, the app can receive readings directly via Bluetooth."
                )
                HelpItem(
                    title = "Connecting Your Meter",
                    content = "To connect a Contour Next One meter:\n\n" +
                            "1. Ensure Bluetooth is enabled on your phone.\n" +
                            "2. Turn on your Contour Next One meter.\n" +
                            "3. Go to Settings > Connect Meter.\n" +
                            "4. Select your meter from the discovered devices.\n" +
                            "5. Readings will sync automatically when connected."
                )
                HelpItem(
                    title = "Photo Capture Tips",
                    content = "For best OCR results:\n\n" +
                            "- Hold your phone steady and level with the meter display.\n" +
                            "- Ensure good lighting (avoid glare).\n" +
                            "- Make sure the glucose number is clearly visible.\n" +
                            "- The app will detect the value and unit automatically."
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Understanding Your Data
            HelpSection(title = "Understanding Your Data") {
                HelpItem(
                    title = "Glucose Ranges",
                    content = "Your glucose readings are classified into ranges:\n\n" +
                            "Low (Purple): Below 3.9 mmol/L (70 mg/dL)\n" +
                            "Readings in this range may indicate hypoglycemia. If you feel symptoms like shakiness, sweating, or confusion, treat immediately with fast-acting sugar.\n\n" +
                            "Normal (Green): 3.9 - 7.8 mmol/L (70 - 140 mg/dL)\n" +
                            "This is the target range for most people with diabetes.\n\n" +
                            "Elevated (Orange): 7.8 - 11.1 mmol/L (140 - 200 mg/dL)\n" +
                            "Readings here are above target. This may occur after meals. Consult your care team if readings are frequently in this range.\n\n" +
                            "High (Red): Above 11.1 mmol/L (200 mg/dL)\n" +
                            "Consistently high readings require attention. Contact your healthcare provider."
                )
                HelpItem(
                    title = "Statistics",
                    content = "The Trends tab shows statistics for different time periods:\n\n" +
                            "- Average: Your mean glucose over the selected period.\n" +
                            "- Min/Max: Your lowest and highest readings.\n" +
                            "- Std Dev: How much your readings vary. Lower is better.\n" +
                            "- CV (Coefficient of Variation): Standard deviation as a percentage of the mean. Below 36% is considered stable.\n" +
                            "- Estimated A1C: An approximation of your 3-month average glucose.\n" +
                            "- Time in Range: Percentage of readings in each glucose category."
                )
                HelpItem(
                    title = "Units",
                    content = "The app supports two glucose measurement units:\n\n" +
                            "- mmol/L (millimoles per litre): Used in Canada, UK, Australia, and most of the world.\n" +
                            "- mg/dL (milligrams per decilitre): Used in the United States and some other countries.\n\n" +
                            "Conversion: 1 mmol/L = 18.02 mg/dL\n\n" +
                            "You can change your preferred unit in Settings."
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cloud Sync
            HelpSection(title = "Cloud Sync") {
                HelpItem(
                    title = "How Sync Works",
                    content = "Kulus uses an offline-first approach:\n\n" +
                            "1. Readings are saved locally on your device immediately.\n" +
                            "2. When you have an internet connection, readings sync to the Kulus cloud.\n" +
                            "3. If sync fails, the app retries with exponential backoff (up to 10 attempts).\n" +
                            "4. Pending readings are shown in the sync status bar.\n\n" +
                            "To use sync, you must have a phone number configured in Settings."
                )
                HelpItem(
                    title = "Cross-Device Sync",
                    content = "Use the same phone number on both iOS and Android to sync readings across devices. " +
                            "All readings are associated with your phone number (E.164 format, e.g., +16465551234)."
                )
                HelpItem(
                    title = "Troubleshooting Sync",
                    content = "If sync isn't working:\n\n" +
                            "1. Check that your phone number is set correctly in Settings.\n" +
                            "2. Ensure you have an internet connection.\n" +
                            "3. Try the 'Force Sync' option in Settings > Debug.\n" +
                            "4. Check the API Log in Settings > Debug for error details.\n" +
                            "5. Contact support if issues persist."
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Privacy & Data
            HelpSection(title = "Privacy & Data") {
                HelpItem(
                    title = "Your Data is Protected",
                    content = "- All data is stored locally on your device first.\n" +
                            "- Cloud sync uses HTTPS encryption in transit.\n" +
                            "- Your phone number is used only for data identification, not for marketing.\n" +
                            "- You can export all your data at any time (CSV, JSON, or text report).\n" +
                            "- You can delete all local data from Settings."
                )
                HelpItem(
                    title = "Snack Pass",
                    content = "The 'Snack Pass' tag is for readings taken after intentional snacking or treats. " +
                            "When enabled on a reading:\n\n" +
                            "- The reading won't trigger glucose alerts.\n" +
                            "- Backend SMS alerts are also suppressed.\n" +
                            "- The reading is still recorded and included in your history.\n\n" +
                            "This prevents false alarms when you know a temporary spike is expected."
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // About
            HelpSection(title = "About Kulus") {
                HelpItem(
                    title = "The Kulus Story",
                    content = "Kulus is an Indigenous-focused diabetes management app created to serve First Nations communities. " +
                            "The name connects to the Thunderbird — a symbol of strength and protection in Indigenous cultures.\n\n" +
                            "Our mission is to make glucose monitoring accessible, culturally respectful, and easy to use."
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun HelpSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
private fun HelpItem(
    title: String,
    content: String
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (expanded) {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (!expanded) {
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        }
    }
}
