package com.example.messagerecovery.presentation.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.messagerecovery.presentation.theme.CyberSurfaceDark
import com.example.messagerecovery.presentation.theme.CyberSurfaceElevated
import com.example.messagerecovery.presentation.theme.ElectricCyan
import com.example.messagerecovery.presentation.theme.MessageRecoveryTheme
import com.example.messagerecovery.presentation.theme.SuccessGreen
import com.example.messagerecovery.presentation.theme.TextMuted
import com.example.messagerecovery.presentation.theme.TextPrimary
import com.example.messagerecovery.presentation.theme.TextSecondary
import com.example.messagerecovery.presentation.theme.WarningAmber
import com.example.messagerecovery.presentation.ui.onboarding.viewmodel.OnboardingUiState

@Composable
fun OnboardingContent(
    state: OnboardingUiState,
    onGrantNotificationClick: () -> Unit,
    onGrantStorageClick: () -> Unit,
    onGrantBatteryClick: () -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Stealth App Shield Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(CyberSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Security Shield",
                    tint = ElectricCyan,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "INTERCEPTOR INITIALIZATION",
                style = MaterialTheme.typography.titleMedium.copy(
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = ElectricCyan
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "High-Fidelity Message & Media Recovery",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Progress Indicator
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberSurfaceDark)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "System Privileges",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                    Text(
                        text = "${state.grantedCount} of 3 Activated",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (state.allGranted) SuccessGreen else WarningAmber
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { state.grantedCount / 3f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (state.allGranted) SuccessGreen else ElectricCyan,
                    trackColor = CyberSurfaceElevated
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Permission Card 1: Notification Listener
            PermissionCard(
                icon = Icons.Default.NotificationsActive,
                title = "Notification Interceptor",
                description = "Captures real-time incoming messages and unsend/deletion alerts from WhatsApp, Messenger, and Instagram.",
                isGranted = state.isNotificationGranted,
                onClick = onGrantNotificationClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Permission Card 2: All Files Access
            PermissionCard(
                icon = Icons.Default.Folder,
                title = "Media Vault Cloner (All Files)",
                description = "Allows instant zero-latency copying of images, audio, and videos to sandbox before sender unsend deletes them.",
                isGranted = state.isStorageGranted,
                onClick = onGrantStorageClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Permission Card 3: Battery Optimization
            PermissionCard(
                icon = Icons.Default.BatteryChargingFull,
                title = "Background Immortality",
                description = "Prevents OEM battery managers (Samsung, Xiaomi) from terminating background interception during deep sleep.",
                isGranted = state.isBatteryIgnored,
                onClick = onGrantBatteryClick
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Hard Gated Action Button
            Button(
                onClick = onContinueClick,
                enabled = state.allGranted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan,
                    contentColor = Color(0xFF00363F),
                    disabledContainerColor = CyberSurfaceElevated,
                    disabledContentColor = TextMuted
                )
            ) {
                Icon(
                    imageVector = if (state.allGranted) Icons.Default.LockOpen else Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (state.allGranted) "ENTER DASHBOARD" else "ACTIVATE ALL 4 PERMISSIONS TO UNLOCK",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun PermissionCard(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
        border = BorderStroke(
            1.dp,
            if (isGranted) SuccessGreen.copy(alpha = 0.5f) else CyberSurfaceElevated
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isGranted) SuccessGreen.copy(alpha = 0.15f) else ElectricCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isGranted) SuccessGreen else ElectricCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isGranted) "Active & Operational" else "Authorization Required",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isGranted) SuccessGreen else WarningAmber
                    )
                }

                if (isGranted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Granted",
                        tint = SuccessGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                lineHeight = 18.sp
            )

            if (!isGranted) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, ElectricCyan),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricCyan)
                ) {
                    Text(
                        text = "Grant Privilege",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}


@Preview(showSystemUi = true)
@Composable
fun OnboardingContentPartialPreview() {
    MessageRecoveryTheme(darkTheme = true) {
        OnboardingContent(
            state = OnboardingUiState(
                isNotificationGranted = true,
                isStorageGranted = false,
                isBatteryIgnored = false
            ),
            onGrantNotificationClick = {},
            onGrantStorageClick = {},
            onGrantBatteryClick = {},
            onContinueClick = {}
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun OnboardingContentAllGrantedPreview() {
    MessageRecoveryTheme(darkTheme = true) {
        OnboardingContent(
            state = OnboardingUiState(
                isNotificationGranted = true,
                isStorageGranted = true,
                isBatteryIgnored = true
            ),
            onGrantNotificationClick = {},
            onGrantStorageClick = {},
            onGrantBatteryClick = {},
            onContinueClick = {}
        )
    }
}
