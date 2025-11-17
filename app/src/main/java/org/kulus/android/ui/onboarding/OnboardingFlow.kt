package org.kulus.android.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

/**
 * Main onboarding flow coordinator
 * Manages navigation between onboarding screens
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingFlow(
    onComplete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf(0) }
    var userName by remember { mutableStateOf("") }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            if (targetState > initialState) {
                // Forward navigation - slide left
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300)) with
                        slideOutHorizontally(
                            targetOffsetX = { -it },
                            animationSpec = tween(300)
                        ) + fadeOut(animationSpec = tween(300))
            } else {
                // Backward navigation - slide right
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300)) with
                        slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = tween(300)
                        ) + fadeOut(animationSpec = tween(300))
            }
        },
        label = "onboarding_screen_transition"
    ) { screen ->
        when (screen) {
            0 -> WelcomeScreen(
                onNext = { currentScreen = 1 },
                modifier = modifier
            )

            1 -> FeaturesScreen(
                onNext = { currentScreen = 2 },
                onBack = { currentScreen = 0 },
                modifier = modifier
            )

            2 -> PrivacyScreen(
                onNext = { currentScreen = 3 },
                onBack = { currentScreen = 1 },
                modifier = modifier
            )

            3 -> AccountSetupScreen(
                onComplete = { name ->
                    userName = name
                    currentScreen = 4
                },
                onBack = { currentScreen = 2 },
                modifier = modifier
            )

            4 -> CompletionScreen(
                userName = userName,
                onFinish = { onComplete(userName) },
                modifier = modifier
            )
        }
    }
}
