package com.example.messagerecovery.presentation.ui.onboarding.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.messagerecovery.utils.PermissionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class OnboardingUiState(
    val isNotificationGranted: Boolean = false,
    val isStorageGranted: Boolean = false,
    val isBatteryIgnored: Boolean = false
) {
    val allGranted: Boolean
        get() = isNotificationGranted && isStorageGranted && isBatteryIgnored

    val grantedCount: Int
        get() {
            var count = 0
            if (isNotificationGranted) count++
            if (isStorageGranted) count++
            if (isBatteryIgnored) count++
            return count
        }
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        refreshPermissionStates()
    }

    fun refreshPermissionStates() {
        _uiState.value = OnboardingUiState(
            isNotificationGranted = PermissionManager.isNotificationListenerEnabled(context),
            isStorageGranted = PermissionManager.isAllFilesAccessGranted(context),
            isBatteryIgnored = PermissionManager.isBatteryOptimizationIgnored(context)
        )
    }
}
