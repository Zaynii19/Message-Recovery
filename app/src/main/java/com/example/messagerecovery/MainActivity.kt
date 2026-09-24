package com.example.messagerecovery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.messagerecovery.presentation.navigation.AppNavGraph
import com.example.messagerecovery.presentation.theme.MessageRecoveryTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MessageRecoveryTheme {
                AppNavGraph()
            }
        }
    }
}