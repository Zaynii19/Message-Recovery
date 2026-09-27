package com.example.messagerecovery.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface ScreenDestination : NavKey {
    @Serializable
    data object Onboarding : ScreenDestination

    @Serializable
    data class MainDashboard(val defaultTab: String = "CHATS") : ScreenDestination

    @Serializable
    data class Conversation(val threadId: String) : ScreenDestination
}
