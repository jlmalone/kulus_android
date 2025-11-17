package org.kulus.android.ui.screens.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun OnboardingNav(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val state by viewModel.state.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "welcome"
    ) {
        composable("welcome") {
            WelcomeScreen(
                onContinue = {
                    navController.navigate("phone_number")
                }
            )
        }

        composable("phone_number") {
            PhoneNumberScreen(
                phoneNumber = state.phoneNumber,
                onPhoneNumberChange = viewModel::updatePhoneNumber,
                onContinue = {
                    navController.navigate("profile_name")
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("profile_name") {
            ProfileNameScreen(
                profileName = state.profileName,
                onProfileNameChange = viewModel::updateProfileName,
                errorMessage = state.errorMessage,
                onContinue = {
                    if (state.profileName.isNotBlank()) {
                        navController.navigate("device_selection")
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("device_selection") {
            DeviceSelectionScreen(
                selectedDevice = state.selectedDevice,
                onDeviceSelect = viewModel::updateDeviceType,
                onContinue = {
                    navController.navigate("notification_preferences")
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("notification_preferences") {
            NotificationPreferencesScreen(
                smsAlertsEnabled = state.smsAlertsEnabled,
                onSmsAlertsChange = viewModel::updateSmsAlerts,
                onContinue = {
                    navController.navigate("completion")
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("completion") {
            CompletionScreen(
                profileName = state.profileName,
                phoneNumber = state.phoneNumber,
                isCompleting = state.isCompleting,
                errorMessage = state.errorMessage,
                onComplete = {
                    viewModel.completeOnboarding(onOnboardingComplete)
                }
            )
        }
    }
}
