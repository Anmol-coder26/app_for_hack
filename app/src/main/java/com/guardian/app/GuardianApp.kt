package com.guardian.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

enum class AppScreen {
    Home, Events, Incidents, Settings
}

data class GuardianEvent(
    val title: String,
    val detail: String,
    val time: String,
    val safe: Boolean
)

data class GuardianIncident(
    val title: String,
    val detail: String,
    val time: String,
    val risk: String
)

data class GuardianState(
    val name: String = "",
    val signedIn: Boolean = false,
    val onboardingComplete: Boolean = false,
    val protectionEnabled: Boolean = true,
    val phoneProtection: Boolean = true,
    val messageProtection: Boolean = true,
    val linkProtection: Boolean = true,
    val events: List<GuardianEvent> = listOf(
        GuardianEvent("Protection enabled", "Guardian is watching for risky activity", "Just now", true),
        GuardianEvent("Phone check ready", "Incoming calls can be reviewed", "Today", true)
    ),
    val incidents: List<GuardianIncident> = emptyList()
)

@Composable
fun GuardianApp(
    onOpenCallRisk: () -> Unit = {},
    onPhoneProtectionToggle: (Boolean) -> Unit = {},
    onMessageProtectionToggle: (Boolean) -> Unit = {}
) {
    var state by remember { mutableStateOf(GuardianState()) }
    var screen by remember { mutableStateOf(AppScreen.Home) }

    when {
        !state.signedIn -> AuthScreen(
            onContinue = { name ->
                state = state.copy(name = name, signedIn = true)
            }
        )

        !state.onboardingComplete -> OnboardingScreen(
            name = state.name,
            onComplete = { state = state.copy(onboardingComplete = true) }
        )

        else -> Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = screen == AppScreen.Home,
                        onClick = { screen = AppScreen.Home },
                        icon = { androidx.compose.material3.Icon(Icons.Default.Home, null) },
                        label = { androidx.compose.material3.Text("Home") }
                    )
                    NavigationBarItem(
                        selected = screen == AppScreen.Events,
                        onClick = { screen = AppScreen.Events },
                        icon = { androidx.compose.material3.Icon(Icons.Default.EventNote, null) },
                        label = { androidx.compose.material3.Text("Events") }
                    )
                    NavigationBarItem(
                        selected = screen == AppScreen.Incidents,
                        onClick = { screen = AppScreen.Incidents },
                        icon = { androidx.compose.material3.Icon(Icons.Default.Warning, null) },
                        label = { androidx.compose.material3.Text("Incidents") }
                    )
                    NavigationBarItem(
                        selected = screen == AppScreen.Settings,
                        onClick = { screen = AppScreen.Settings },
                        icon = { androidx.compose.material3.Icon(Icons.Default.Settings, null) },
                        label = { androidx.compose.material3.Text("Settings") }
                    )
                }
            }
        ) { padding ->
            AnimatedContent(
                targetState = screen,
                label = "guardian-screen",
                modifier = Modifier.padding(padding)
            ) { target ->
                val contentPadding = PaddingValues()
                when (target) {
                    AppScreen.Home -> HomeScreen(
                        state = state,
                        contentPadding = contentPadding,
                        onOpenCallRisk = onOpenCallRisk,
                        onProtectionChange = { enabled ->
                            state = state.copy(
                                protectionEnabled = enabled,
                                events = listOf(
                                    GuardianEvent(
                                        if (enabled) "Protection enabled" else "Protection paused",
                                        if (enabled) "Guardian is watching for risky activity"
                                        else "Only on-phone checks are running",
                                        "Just now",
                                        enabled
                                    )
                                ) + state.events
                            )
                        },
                        onAddIncident = { incident ->
                            state = state.copy(
                                incidents = listOf(incident) + state.incidents,
                                events = listOf(
                                    GuardianEvent("Incident reported", incident.title, "Just now", false)
                                ) + state.events
                            )
                            screen = AppScreen.Incidents
                        }
                    )

                    AppScreen.Events -> EventsScreen(
                        events = state.events,
                        contentPadding = contentPadding
                    )

                    AppScreen.Incidents -> IncidentsScreen(
                        incidents = state.incidents,
                        contentPadding = contentPadding,
                        onAddIncident = { incident ->
                            state = state.copy(incidents = listOf(incident) + state.incidents)
                        }
                    )

                    AppScreen.Settings -> SettingsScreen(
                        state = state,
                        contentPadding = contentPadding,
                        onToggle = { type, enabled ->
                            state = when (type) {
                                SettingType.All -> state.copy(
                                    protectionEnabled = enabled,
                                    phoneProtection = enabled,
                                    messageProtection = enabled
                                )
                                SettingType.Phone -> state.copy(phoneProtection = enabled)
                                SettingType.Messages -> state.copy(messageProtection = enabled)
                                SettingType.Links -> state.copy(linkProtection = enabled)
                            }
                            when (type) {
                                SettingType.All -> {
                                    onPhoneProtectionToggle(enabled)
                                    onMessageProtectionToggle(enabled)
                                }
                                SettingType.Phone -> onPhoneProtectionToggle(enabled)
                                SettingType.Messages -> onMessageProtectionToggle(enabled)
                                SettingType.Links -> Unit
                            }
                        },
                        onDeleteData = {
                            state = GuardianState(name = state.name, signedIn = true, onboardingComplete = true)
                            screen = AppScreen.Home
                        }
                    )
                }
            }
        }
    }
}