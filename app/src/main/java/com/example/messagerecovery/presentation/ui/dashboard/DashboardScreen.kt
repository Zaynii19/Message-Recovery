package com.example.messagerecovery.presentation.ui.dashboard

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.messagerecovery.presentation.ui.chatlist.ChatListScreen
import com.example.messagerecovery.presentation.ui.gallery.AttachmentGalleryScreen
import com.example.messagerecovery.utils.PermissionManager

@Composable
fun DashboardScreen(
    onNavigateToConversation: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var selectedTab by remember { mutableStateOf("CHATS") }
    var isInterceptionActive by remember {
        mutableStateOf(PermissionManager.isNotificationListenerEnabled(context))
    }

    // Automatically refresh permission interception status when returning from settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isInterceptionActive = PermissionManager.isNotificationListenerEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // If on Attachments tab, pressing back navigates to Chats tab
    BackHandler(enabled = selectedTab != "CHATS") {
        selectedTab = "CHATS"
    }

    val state = DashboardUiState(
        selectedTab = selectedTab,
        isInterceptionActive = isInterceptionActive
    )

    DashboardContent(
        state = state,
        onTabSelected = { newTab -> selectedTab = newTab },
        onReenableInterception = {
            context.startActivity(PermissionManager.getNotificationListenerSettingsIntent(context))
        },
        chatsContent = {
            ChatListScreen(onNavigateToConversation = onNavigateToConversation)
        },
        attachmentsContent = {
            AttachmentGalleryScreen()
        },
        modifier = modifier
    )
}

