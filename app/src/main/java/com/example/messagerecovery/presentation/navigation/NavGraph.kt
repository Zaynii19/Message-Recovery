package com.example.messagerecovery.presentation.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.messagerecovery.presentation.ui.conversation.ConversationScreen
import com.example.messagerecovery.presentation.ui.dashboard.DashboardScreen
import com.example.messagerecovery.presentation.ui.onboarding.OnboardingScreen
import com.example.messagerecovery.utils.PermissionManager

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val initialDestination = remember {
        if (PermissionManager.areAllRequiredPermissionsGranted(context)) {
            ScreenDestination.MainDashboard()
        } else {
            ScreenDestination.Onboarding
        }
    }

    var currentDestination by remember { mutableStateOf(initialDestination) }

    Crossfade(
        targetState = currentDestination,
        modifier = modifier.fillMaxSize(),
        label = "NavTransition"
    ) { destination ->
        when (destination) {
            is ScreenDestination.Onboarding -> {
                OnboardingScreen(
                    onNavigateToDashboard = {
                        currentDestination = ScreenDestination.MainDashboard()
                    }
                )
            }
            is ScreenDestination.MainDashboard -> {
                DashboardScreen(
                    onNavigateToConversation = { threadId ->
                        currentDestination = ScreenDestination.Conversation(threadId)
                    }
                )
            }
            is ScreenDestination.Conversation -> {
                BackHandler {
                    currentDestination = ScreenDestination.MainDashboard()
                }
                ConversationScreen(
                    threadId = destination.threadId,
                    onNavigateBack = {
                        currentDestination = ScreenDestination.MainDashboard()
                    }
                )
            }
        }
    }
}
