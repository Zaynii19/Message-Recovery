package com.example.messagerecovery.presentation.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.messagerecovery.presentation.theme.AlertRed
import com.example.messagerecovery.presentation.theme.CyberObsidian
import com.example.messagerecovery.presentation.theme.CyberSurfaceDark
import com.example.messagerecovery.presentation.theme.ElectricCyan
import com.example.messagerecovery.presentation.theme.MessageRecoveryTheme
import com.example.messagerecovery.presentation.theme.SuccessGreen
import com.example.messagerecovery.presentation.theme.TextPrimary
import com.example.messagerecovery.presentation.theme.TextSecondary

data class DashboardUiState(
    val selectedTab: String = "CHATS", // "CHATS" or "ATTACHMENTS"
    val isInterceptionActive: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    state: DashboardUiState,
    onTabSelected: (String) -> Unit,
    chatsContent: @Composable () -> Unit,
    attachmentsContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onReenableInterception: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CyberObsidian,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = if (!state.isInterceptionActive) {
                            Modifier.clickable { onReenableInterception() }
                        } else Modifier
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (state.isInterceptionActive) SuccessGreen else AlertRed)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "MESSAGE RECOVERY",
                            style = MaterialTheme.typography.titleMedium.copy(
                                letterSpacing = 1.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextPrimary
                        )
                    }
                },
                actions = {
                    if (!state.isInterceptionActive) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AlertRed.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, AlertRed.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clickable { onReenableInterception() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "OFFLINE • TAP TO FIX",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = AlertRed
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CyberSurfaceDark)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = CyberSurfaceDark,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = state.selectedTab == "CHATS",
                    onClick = { onTabSelected("CHATS") },
                    icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chats") },
                    label = { Text("Chats") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ElectricCyan,
                        selectedTextColor = ElectricCyan,
                        indicatorColor = ElectricCyan.copy(alpha = 0.15f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
                NavigationBarItem(
                    selected = state.selectedTab == "ATTACHMENTS",
                    onClick = { onTabSelected("ATTACHMENTS") },
                    icon = { Icon(Icons.Default.PermMedia, contentDescription = "Media Vault") },
                    label = { Text("Attachments") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ElectricCyan,
                        selectedTextColor = ElectricCyan,
                        indicatorColor = ElectricCyan.copy(alpha = 0.15f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.selectedTab == "CHATS") {
                chatsContent()
            } else {
                attachmentsContent()
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun DashboardContentPreview() {
    MessageRecoveryTheme(darkTheme = true) {
        DashboardContent(
            state = DashboardUiState(),
            onTabSelected = {},
            chatsContent = {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Chats Tab Preview", color = TextPrimary)
                }
            },
            attachmentsContent = {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Attachments Tab Preview", color = TextPrimary)
                }
            }
        )
    }
}