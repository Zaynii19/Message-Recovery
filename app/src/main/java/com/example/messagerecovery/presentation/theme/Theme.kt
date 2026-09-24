package com.example.messagerecovery.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val CyberDarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = Color(0xFF00363F),
    primaryContainer = Color(0xFF004E5B),
    onPrimaryContainer = Color(0xFFA5EEFF),
    secondary = NeonIndigo,
    onSecondary = Color(0xFF1E1B4B),
    secondaryContainer = Color(0xFF312E81),
    onSecondaryContainer = Color(0xFFC7D2FE),
    tertiary = NeonViolet,
    onTertiary = Color(0xFF2E1065),
    background = CyberObsidian,
    onBackground = TextPrimary,
    surface = CyberSurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    surfaceContainer = CyberSurfaceElevated,
    error = DeletionCrimson,
    onError = Color.White,
    errorContainer = DeletionCrimsonDark,
    onErrorContainer = Color(0xFFFFD9DF)
)

private val CyberLightColorScheme = lightColorScheme(
    primary = ElectricCyanDim,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC7F3FF),
    onPrimaryContainer = Color(0xFF001F26),
    secondary = NeonIndigo,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0E7FF),
    onSecondaryContainer = Color(0xFF1E1B4B),
    tertiary = NeonViolet,
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    surfaceContainer = Color(0xFFE2E8F0),
    error = DeletionCrimson,
    onError = Color.White,
    errorContainer = Color(0xFFFFD9DF),
    onErrorContainer = Color(0xFF410011)
)

@Composable
fun MessageRecoveryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Default to custom Cyber/Stealth palette
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> CyberDarkColorScheme
        else -> CyberLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}