package com.example.messagerecovery.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.messagerecovery.presentation.ui.conversation.ConversationScreen
import com.example.messagerecovery.presentation.ui.dashboard.DashboardScreen
import com.example.messagerecovery.presentation.ui.onboarding.OnboardingScreen
import com.example.messagerecovery.utils.PermissionManager

@Composable
fun AppNavGraph() {
    val context = LocalContext.current
    val initialDestination = remember {
        if (PermissionManager.areAllRequiredPermissionsGranted(context)) {
            ScreenDestination.MainDashboard()
        } else {
            ScreenDestination.Onboarding
        }
    }

    val backStack = rememberNavBackStack(initialDestination)

    NavDisplay(
        backStack = backStack,
        modifier = Modifier.fillMaxSize(),
        onBack = {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        },
        transitionSpec = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300)) togetherWith
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth / 3 },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
        },
        popTransitionSpec = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth / 3 },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300)) togetherWith
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
        },
        entryProvider = entryProvider {
            entry<ScreenDestination.Onboarding> {
                OnboardingScreen(
                    onNavigateToDashboard = {
                        backStack.clear()
                        backStack.add(ScreenDestination.MainDashboard())
                    }
                )
            }
            entry<ScreenDestination.MainDashboard> {
                DashboardScreen(
                    onNavigateToConversation = { threadId ->
                        backStack.add(ScreenDestination.Conversation(threadId))
                    }
                )
            }
            entry<ScreenDestination.Conversation> { key ->
                ConversationScreen(
                    threadId = key.threadId,
                    onNavigateBack = {
                        if (backStack.size > 1) {
                            backStack.removeLastOrNull()
                        }
                    }
                )
            }
        }
    )
}
