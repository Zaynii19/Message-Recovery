package com.example.messagerecovery.presentation.ui.onboarding

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