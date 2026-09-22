package com.guardian.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AuthScreen(onContinue: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(28.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(54.dp)
        )
        Spacer(Modifier.height(18.dp))
        Text("Guardian", fontSize = 36.sp, fontWeight = FontWeight.Bold)
        Text(
            "A calmer way to stay ahead of risky calls, links, messages, and payments.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 28.dp)
        )
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Your name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { onContinue(name.trim().ifBlank { "Friend" }) },
            modifier = Modifier.fillMaxWidth(),
            enabled = email.isNotBlank() && password.length >= 4
        ) {
            Text("Create account")
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null)
        }
        Text(
            "This reconstructed build stores demo state locally. Server authentication will be added when the original API is available.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
fun OnboardingScreen(name: String, onComplete: () -> Unit) {
    var step by remember { mutableStateOf(0) }
    val steps = listOf(
        "Guardian watches the signals that are easy to miss.",
        "When something looks risky, you get a clear explanation before you act.",
        "You stay in control. Turn each protection layer on or off in Settings."
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(58.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text("Welcome, $name", fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(14.dp))
        Text(steps[step], fontSize = 20.sp, lineHeight = 28.sp)
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(steps.size) { index ->
                BoxDot(active = index == step)
            }
        }
        Spacer(Modifier.height(30.dp))
        Button(
            onClick = {
                if (step == steps.lastIndex) onComplete() else step++
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (step == steps.lastIndex) "Open Guardian" else "Next")
        }
    }
}

@Composable
private fun BoxDot(active: Boolean) {
    Spacer(
        modifier = Modifier
            .size(if (active) 28.dp else 10.dp, 10.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (active) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
    )
}

@Composable
fun HomeScreen(
    state: GuardianState,
    contentPadding: PaddingValues,
    onOpenCallRisk: () -> Unit,
    onProtectionChange: (Boolean) -> Unit,
    onAddIncident: (GuardianIncident) -> Unit
) {
    var showIncidentDialog by remember { mutableStateOf(false) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 24.dp,
            bottom = 28.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Good to see you, ${state.name}", fontSize = 25.sp, fontWeight = FontWeight.Bold)
            Text(
                "Your personal safety overview",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            ProtectionCard(
                enabled = state.protectionEnabled,
                onToggle = onProtectionChange
            )
        }
        item {
            Text("Protection layers", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MiniAction(
                    title = "Scan QR",
                    icon = Icons.Default.QrCodeScanner,
                    modifier = Modifier.weight(1f)
                )
                MiniAction(
                    title = "Check link",
                    icon = Icons.Default.Link,
                    modifier = Modifier.weight(1f)
                )
                MiniAction(
                    title = "Phone guard",
                    icon = Icons.Default.Phone,
                    onClick = onOpenCallRisk,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recent activity", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                Spacer(Modifier.weight(1f))
                Text("${state.events.size} checks", color = MaterialTheme.colorScheme.primary)
            }
        }
        items(state.events.take(3)) { event ->
            EventRow(event)
        }
        item {
            OutlinedButton(
                onClick = { showIncidentDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.WarningAmber, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Report an incident")
            }
        }
    }

    if (showIncidentDialog) {
        IncidentDialog(
            onDismiss = { showIncidentDialog = false },
            onSave = {
                onAddIncident(it)
                showIncidentDialog = false
            }
        )
    }
}

@Composable
private fun ProtectionCard(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (enabled) Icons.Default.Shield else Icons.Default.WarningAmber,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(36.dp)
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (enabled) "Protection is on" else "Protection is paused",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    if (enabled) "Guardian is watching for risky activity"
                    else "Only on-phone checks are running",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
            Switch(checked = enabled, onCheckedChange = onToggle)
        }
    }
}

@Composable
private fun MiniAction(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit = {},
    modifier: Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(title, fontSize = 12.sp)
        }
    }
}

@Composable
fun EventsScreen(events: List<GuardianEvent>, contentPadding: PaddingValues) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Events", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(
                "A timeline of checks and protection changes",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
        }
        items(events) { event -> EventRow(event) }
    }
}

@Composable
private fun EventRow(event: GuardianEvent) {
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (event.safe) Icons.Default.CheckCircle else Icons.Default.WarningAmber,
                contentDescription = null,
                tint = if (event.safe) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(event.title, fontWeight = FontWeight.SemiBold)
                Text(event.detail, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            }
            Text(event.time, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
    }
}

@Composable
fun IncidentsScreen(
    incidents: List<GuardianIncident>,
    contentPadding: PaddingValues,
    onAddIncident: (GuardianIncident) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Incidents", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "Review anything that needs your attention",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { showDialog = true }) {
                    Icon(Icons.Default.WarningAmber, contentDescription = "Report incident")
                }
            }
        }
        if (incidents.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(Modifier.padding(22.dp)) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(Modifier.height(10.dp))
                        Text("No incidents yet", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Guardian will keep this space ready if something needs review.")
                    }
                }
            }
        } else {
            items(incidents) { incident ->
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(incident.title, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            Text(incident.risk, color = MaterialTheme.colorScheme.error)
                        }
                        Text(incident.detail, modifier = Modifier.padding(top = 6.dp))
                        Text(
                            incident.time,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
    if (showDialog) {
        IncidentDialog(
            onDismiss = { showDialog = false },
            onSave = {
                onAddIncident(it)
                showDialog = false
            }
        )
    }
}

enum class SettingType { All, Phone, Messages, Links }

@Composable
fun SettingsScreen(
    state: GuardianState,
    contentPadding: PaddingValues,
    onToggle: (SettingType, Boolean) -> Unit,
    onDeleteData: () -> Unit
) {
    var confirmDelete by remember { mutableStateOf(false) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Settings", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(
                "Choose how Guardian protects you",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
            )
        }
        item {
            SettingRow(
                title = "All protection",
                detail = "Pause every active protection layer",
                checked = state.protectionEnabled,
                onCheckedChange = { onToggle(SettingType.All, it) }
            )
        }
        item {
            SettingRow(
                title = "Phone protection",
                detail = "Review incoming calls before you answer",
                checked = state.phoneProtection,
                onCheckedChange = { onToggle(SettingType.Phone, it) }
            )
        }
        item {
            SettingRow(
                title = "Message protection",
                detail = "Check notifications for suspicious patterns; enable access when prompted",
                checked = state.messageProtection,
                onCheckedChange = { onToggle(SettingType.Messages, it) }
            )
        }
        item {
            SettingRow(
                title = "Link protection",
                detail = "Review links before they open",
                checked = state.linkProtection,
                onCheckedChange = { onToggle(SettingType.Links, it) }
            )
        }
        item {
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            OutlinedButton(
                onClick = { confirmDelete = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Delete local data")
            }
        }
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete local data?") },
            text = { Text("This clears reconstructed demo events and incidents from this session.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDeleteData()
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SettingRow(
    title: String,
    detail: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun IncidentDialog(
    onDismiss: () -> Unit,
    onSave: (GuardianIncident) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report an incident") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("What happened?") },
                    singleLine = true
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = detail,
                    onValueChange = { detail = it },
                    label = { Text("Add details") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        GuardianIncident(
                            title = title.ifBlank { "Uncategorized incident" },
                            detail = detail.ifBlank { "No additional details provided." },
                            time = "Just now",
                            risk = "Review"
                        )
                    )
                }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}