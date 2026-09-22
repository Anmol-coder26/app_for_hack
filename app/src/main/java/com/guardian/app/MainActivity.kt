package com.guardian.app

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    private val protectionPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GuardianTheme {
                GuardianApp(
                    onPhoneProtectionToggle = { enabled ->
                        if (enabled) {
                            protectionPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.READ_PHONE_STATE,
                                    Manifest.permission.POST_NOTIFICATIONS
                                )
                            )
                            startService(Intent(this, CallMonitorService::class.java))
                        } else {
                            stopService(Intent(this, CallMonitorService::class.java))
                        }
                    },
                    onMessageProtectionToggle = { enabled ->
                        if (enabled) {
                            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                        } else {
                            stopService(Intent(this, MessageListenerService::class.java))
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun GuardianTheme(content: @Composable () -> Unit) {
    val lightColors = lightColorScheme(
        primary = Color(0xFF2F7D5A),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFC5EBD5),
        onPrimaryContainer = Color(0xFF062016),
        secondary = Color(0xFF526B5B),
        background = Color(0xFFF7FAF8),
        surface = Color(0xFFF7FAF8),
        surfaceVariant = Color(0xFFE1E9E3),
        onSurface = Color(0xFF17201A)
    )
    val darkColors = darkColorScheme(
        primary = Color(0xFF9FD5B6),
        onPrimary = Color(0xFF07371D),
        primaryContainer = Color(0xFF185333),
        onPrimaryContainer = Color(0xFFC5EBD5)
    )

    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) darkColors else lightColors,
        content = content
    )
}