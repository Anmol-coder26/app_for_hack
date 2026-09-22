package com.guardian.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

enum class RiskStatus(val label: String) {
    Low("LOW RISK"),
    Suspicious("SUSPICIOUS"),
    High("HIGH RISK")
}

data class RiskAssessment(
    val score: Int,
    val status: RiskStatus,
    val reasons: List<String>
)

interface ScamAnalyzer {
    fun analyze(transcript: String): RiskAssessment
}

/**
 * A replaceable, deterministic analyzer for the local MVP.
 *
 * It only scores indicators that are present in the transcript. It does not
 * manufacture a score when no speech has been recognized.
 */
class KeywordScamAnalyzer : ScamAnalyzer {
    private data class Indicator(
        val terms: List<String>,
        val points: Int,
        val reason: String
    )

    private val indicators = listOf(
        Indicator(
            terms = listOf("otp", "one time password", "pin", "password", "cvv", "verification code"),
            points = 28,
            reason = "Caller is requesting a secret code or credential"
        ),
        Indicator(
            terms = listOf("bank", "rbi", "police", "government", "customer care", "official", "income tax"),
            points = 18,
            reason = "Possible bank, company, or government impersonation"
        ),
        Indicator(
            terms = listOf("send money", "transfer money", "pay now", "upi", "account number", "wire transfer"),
            points = 25,
            reason = "Caller is requesting money or payment details"
        ),
        Indicator(
            terms = listOf("urgent", "immediately", "right now", "within one hour", "arrest", "legal action", "blocked"),
            points = 18,
            reason = "Urgency or threatening language detected"
        ),
        Indicator(
            terms = listOf("lottery", "prize", "reward", "cashback", "lucky draw", "you have won"),
            points = 16,
            reason = "Possible fake reward or lottery offer"
        ),
        Indicator(
            terms = listOf("click this link", "open this link", "install this app", "screen share", "remote access", "anydesk"),
            points = 24,
            reason = "Caller is requesting a risky link, app, or remote-access action"
        ),
        Indicator(
            terms = listOf("aadhaar", "aadhar", "pan card", "date of birth", "card number", "personal information"),
            points = 18,
            reason = "Caller is requesting sensitive personal information"
        )
    )

    override fun analyze(transcript: String): RiskAssessment {
        val normalized = transcript.lowercase(Locale.ROOT)
        val matches = indicators.filter { indicator ->
            indicator.terms.any(normalized::contains)
        }
        val score = matches.sumOf(Indicator::points).coerceIn(0, 99)
        val status = when {
            score >= 60 -> RiskStatus.High
            score >= 25 -> RiskStatus.Suspicious
            else -> RiskStatus.Low
        }
        return RiskAssessment(
            score = score,
            status = status,
            reasons = matches.map(Indicator::reason).take(4)
        )
    }
}

interface TranscriptListener {
    fun onTranscript(text: String, isFinal: Boolean)
    fun onTranscriptionError(message: String)
}

interface StreamingTranscriber {
    fun start(listener: TranscriptListener)
    fun stop()
}

/**
 * Uses Android's speech recognizer as a streaming adapter. It prefers the
 * on-device recognizer when the device exposes one and never writes audio to
 * storage. The analyzer receives text segments only.
 */
class AndroidSpeechTranscriber(private val context: Context) : StreamingTranscriber {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var recognizer: SpeechRecognizer? = null
    private var listener: TranscriptListener? = null
    private var running = false
    private var restartScheduled = false

    override fun start(listener: TranscriptListener) {
        this.listener = listener
        running = true
        recognizer?.destroy()
        recognizer = createRecognizer()
        recognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) = Unit
            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit

            override fun onError(error: Int) {
                if (!running) return
                if (error != SpeechRecognizer.ERROR_NO_MATCH &&
                    error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT
                ) {
                    this@AndroidSpeechTranscriber.listener?.onTranscriptionError(
                        "Speech recognition paused. Check microphone access and try again."
                    )
                }
                restartIfRunning()
            }

            override fun onResults(results: Bundle?) {
                val text = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()
                if (text.isNotBlank()) {
                    this@AndroidSpeechTranscriber.listener?.onTranscript(text, true)
                }
                restartIfRunning()
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val text = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()
                if (text.isNotBlank()) {
                    this@AndroidSpeechTranscriber.listener?.onTranscript(text, false)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
        startListening()
    }

    override fun stop() {
        running = false
        restartScheduled = false
        mainHandler.removeCallbacksAndMessages(null)
        recognizer?.cancel()
        recognizer?.destroy()
        recognizer = null
        listener = null
    }

    private fun createRecognizer(): SpeechRecognizer? {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S &&
                SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
            ) {
                SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
            } else {
                SpeechRecognizer.createSpeechRecognizer(context)
            }
        } catch (_: RuntimeException) {
            listener?.onTranscriptionError(
                "This device does not provide an available speech recognizer."
            )
            null
        }
    }

    private fun startListening() {
        val activeRecognizer = recognizer ?: return
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        }
        try {
            activeRecognizer.startListening(intent)
        } catch (_: RuntimeException) {
            listener?.onTranscriptionError("Speech recognition could not start.")
        }
    }

    private fun restartIfRunning() {
        if (running && !restartScheduled) {
            restartScheduled = true
            mainHandler.postDelayed(
                {
                    restartScheduled = false
                    if (running) startListening()
                },
                250L
            )
        }
    }
}