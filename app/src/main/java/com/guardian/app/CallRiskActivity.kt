package com.guardian.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class CallRiskActivity : ComponentActivity() {
    private val microphonePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startDetection() else {
                errorMessage = "Microphone permission is needed to analyze speaker audio."
            }
        }

    private var isDetecting by mutableStateOf(false)
    private var transcript by mutableStateOf("")
    private var assessment by mutableStateOf(KeywordScamAnalyzer().analyze(""))
    private var errorMessage by mutableStateOf<String?>(null)
    private var transcriber: StreamingTranscriber? = null
    private var completedTranscript = ""
    private val analyzer: ScamAnalyzer = KeywordScamAnalyzer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GuardianTheme {
                CallRiskScreen(
                    isDetecting = isDetecting,
                    transcript = transcript,
                    assessment = assessment,
                    errorMessage = errorMessage,
                    onStart = ::requestAndStartDetection,
                    onStop = ::stopDetection
                )
            }
        }
    }

    private fun requestAndStartDetection() {
        errorMessage = null
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            microphonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            startDetection()
        }
    }

    private fun startDetection() {
        stopDetection()
        completedTranscript = ""
        transcript = ""
        assessment = analyzer.analyze("")
        errorMessage = null
        isDetecting = true
        transcriber = AndroidSpeechTranscriber(this).also { speech ->
            speech.start(object : TranscriptListener {
                override fun onTranscript(text: String, isFinal: Boolean) {
                    val visibleText = if (isFinal) {
                        completedTranscript = "$completedTranscript $text".trim()
                        completedTranscript
                    } else {
                        "$completedTranscript $text".trim()
                    }
                    transcript = visibleText
                    assessment = analyzer.analyze(visibleText)
                }

                override fun onTranscriptionError(message: String) {
                    errorMessage = message
                }
            })
        }
    }

    private fun stopDetection() {
        transcriber?.stop()
        transcriber = null
        isDetecting = false
        transcript = ""
        completedTranscript = ""
        assessment = analyzer.analyze("")
    }

    override fun onDestroy() {
        stopDetection()
        super.onDestroy()
    }
}

@androidx.compose.runtime.Composable
private fun CallRiskScreen(
    isDetecting: Boolean,
    transcript: String,
    assessment: RiskAssessment,
    errorMessage: String?,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    val riskColor = when (assessment.status) {
        RiskStatus.Low -> Color(0xFF2F7D5A)
        RiskStatus.Suspicious -> Color(0xFFB7791F)
        RiskStatus.High -> MaterialTheme.colorScheme.error
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Live call check", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(
                "Put the call on speaker, then listen for warning signs.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = riskColor.copy(alpha = 0.12f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("SCAM RISK", fontWeight = FontWeight.SemiBold, letterSpacing = 1.5.sp)
                    Text(
                        "${assessment.score}%",
                        color = riskColor,
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        assessment.status.label,
                        color = riskColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (isDetecting) "Updating from recognized speech"
                        else "Start detection to analyze the conversation",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
        item {
            if (isDetecting) {
                Button(onClick = onStop, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Stop, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Stop live analysis")
                }
            } else {
                Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Mic, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Start live analysis")
                }
            }
        }
        item {
            Card {
                Column(Modifier.padding(16.dp)) {
                    Text("Before you start", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Turn on speakerphone and keep the microphone permission enabled. " +
                            "Guardian analyzes temporary speech text and does not save raw call audio.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }
        if (errorMessage != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.WarningAmber, contentDescription = null)
                        Text(
                            errorMessage!!,
                            modifier = Modifier.padding(start = 10.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
        item {
            Text("Detected reasons", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        }
        if (assessment.reasons.isEmpty()) {
            item {
                Text(
                    if (transcript.isBlank()) "No speech analyzed yet."
                    else "No suspicious indicator detected in the recognized speech.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(assessment.reasons) { reason ->
                Text("• $reason", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            Text("Live transcript", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        }
        item {
            Card {
                Text(
                    transcript.ifBlank { "Speech will appear here while analysis is active." },
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            OutlinedButton(onClick = onStop, modifier = Modifier.fillMaxWidth()) {
                Text("Stop and discard analysis")
            }
        }
    }
}