package com.example.messagerecovery.presentation.ui.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.messagerecovery.presentation.ui.onboarding.viewmodel.OnboardingViewModel
import com.example.messagerecovery.utils.PermissionManager
import com.example.messagerecovery.utils.ServiceWatchdog

@Composable
fun OnboardingScreen(
    onNavigateToDashboard: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Automatically re-check permission status when user returns from Settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissionStates()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    OnboardingContent(
        state = state,
        onGrantNotificationClick = {
            context.startActivity(PermissionManager.getNotificationListenerSettingsIntent(context))
        },
        onGrantStorageClick = {
            context.startActivity(PermissionManager.getAllFilesAccessSettingsIntent(context))
        },
        onGrantBatteryClick = {
            context.startActivity(PermissionManager.getIgnoreBatteryOptimizationIntent(context))
        },
        onContinueClick = {
            if (state.allGranted) {
                ServiceWatchdog.startMasterWatchdog(context)
                onNavigateToDashboard()
            }
        },
        modifier = modifier
    )
}
